package com.example.uni.fragments;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.uni.R;
import com.example.uni.activities.MainActivity;
import com.example.uni.databinding.FragmentForgotPasswordBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.Replace;
import com.example.uni.utilities.ShowDialog;
import com.example.uni.utilities.ShowLoading;
import com.google.firebase.firestore.DocumentSnapshot;

public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(getLayoutInflater());
        initFields();
        initFunc();
        return binding.getRoot();
    }

    private void initFields(){
        preferenceManager = new PreferenceManager(requireActivity());
    }
    private void initFunc(){
        setListeners();
        setMaxLength();
    }

    private void setListeners(){
        binding.fragmentForgotPasswordButtonBack.setOnClickListener(view -> requireActivity().onBackPressed());
        binding.fragmentForgotPasswordButtonCheck.setOnClickListener(view -> {
            if (binding.fragmentForgotPasswordUsername.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.username_can_not_be_empty));
            } else if (!binding.fragmentForgotPasswordUsername.getText().toString().trim().startsWith(Constants.USERNAME_SIGN)){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.username_must_start_with) + " '" + Constants.USERNAME_SIGN + "'");
            } else if (binding.fragmentForgotPasswordRecoveryCode.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.recovery_code_can_not_be_empty));
            }else if (binding.fragmentForgotPasswordRecoveryCode.getText().toString().trim().length() < Integer.parseInt(Constants.RECOVERY_CODE_MAX_LENGTH)) {
                ShowDialog.show(requireActivity(), getString(R.string.recovery_code_must_contain_8_digits));
            } else {
                logIn();
            }
        });
    }
    private void logIn(){
        ShowLoading.show(requireActivity());
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.USERNAME, binding.fragmentForgotPasswordUsername.getText().toString().trim())
                .whereEqualTo(Constants.RECOVERY_CODE, binding.fragmentForgotPasswordRecoveryCode.getText().toString().trim())
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
                        preferenceManager.putString(Constants.USERNAME, documentSnapshot.getString(Constants.USERNAME));
                        preferenceManager.putString(Constants.RECOVERY_CODE, documentSnapshot.getString(Constants.RECOVERY_CODE));
                        ShowLoading.dismissDialog();
                        Replace.replaceActivity(requireActivity(), new MainActivity(), true);
                    } else {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.we_could_not_find_this_account));
                    }
                });
    }
    private void setMaxLength(){
        binding.fragmentForgotPasswordUsername.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.USERNAME_MAX_LENGTH))});
        binding.fragmentForgotPasswordRecoveryCode.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.RECOVERY_CODE_MAX_LENGTH))});
    }
}