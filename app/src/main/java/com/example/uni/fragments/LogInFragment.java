package com.example.uni.fragments;

import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.uni.R;
import com.example.uni.activities.RecoveryCodeActivity;
import com.example.uni.databinding.FragmentLogInBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.Replace;
import com.example.uni.utilities.ShowDialog;
import com.example.uni.utilities.ShowLoading;
import com.google.firebase.firestore.DocumentSnapshot;

public class LogInFragment extends Fragment {
    private FragmentLogInBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(getLayoutInflater());
        initFields();
        initFunc();
        return binding.getRoot();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(requireActivity());
    }
    private void initFunc(){
        setListeners();
        preferenceManager.clear();
        setMaxLength();
    }
    private void setListeners(){
        binding.logInFragmentButtonForgotPassword.setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.action_logInFragment_to_forgotPasswordFragment);
        });
        binding.logInFragmentPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        binding.logInFragmentButtonCreateNewAccount.setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.action_logInFragment_to_signUpFragment);
        });
        binding.logInFragmentButtonCheck.setOnClickListener(view -> {
            if (binding.logInFragmentPhoneNumber.getText().toString().trim().isEmpty()) {
                ShowDialog.show(requireActivity(), getResources().getString(R.string.phone_number_cant_be_empty));
            }else if (!binding.logInFragmentPhoneNumber.getText().toString().trim().startsWith("+") || !Patterns.PHONE.matcher(binding.logInFragmentPhoneNumber.getText().toString().trim()).matches()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.wrong_phone_number_format));
            }else if (binding.logInFragmentPassword.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.phone_number_cant_be_empty));
            }else if (binding.logInFragmentPassword.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.password_is_too_short));
            }else {
                logIn();
            }
        });
    }
    private void logIn(){
        ShowLoading.show(requireActivity());
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.PHONE_NUMBER, binding.logInFragmentPhoneNumber.getText().toString().trim())
                .whereEqualTo(Constants.PASSWORD, binding.logInFragmentPassword.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.IS_SIGNED_IN, true);
                        preferenceManager.putBoolean(Constants.STATUS, true);
                        preferenceManager.putString(Constants.USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.NAME, documentSnapshot.getString(Constants.NAME));
                        preferenceManager.putString(Constants.IMAGE_PROFILE, documentSnapshot.getString(Constants.IMAGE_PROFILE));
                        preferenceManager.putString(Constants.PASSWORD, documentSnapshot.getString(Constants.PASSWORD));
                        preferenceManager.putString(Constants.BIO, documentSnapshot.getString(Constants.BIO));
                        preferenceManager.putString(Constants.PHONE_NUMBER, documentSnapshot.getString(Constants.PHONE_NUMBER));
                        preferenceManager.putString(Constants.RECOVERY_CODE, documentSnapshot.getString(Constants.RECOVERY_CODE));
                        ShowLoading.dismissDialog();
                        Replace.replaceActivity(requireActivity(), new RecoveryCodeActivity(), true);
                    } else {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.we_couldnt_find_your_account));
                    }
                }).addOnFailureListener(e -> {
                    ShowDialog.show(requireActivity(), getResources().getString(R.string.error));
                });
    }
    private void setMaxLength(){
        binding.logInFragmentPhoneNumber.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.PHONE_MAX_LENGTH))});
        binding.logInFragmentPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.PASSWORD_MAX_LENGTH))});
    }
}