package com.example.uni.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.uni.R;
import com.example.uni.activities.RecoveryCodeActivity;
import com.example.uni.databinding.FragmentSignUpBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.Replace;
import com.example.uni.utilities.ShowDialog;
import com.example.uni.utilities.ShowLoading;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Random;

public class SignUpFragment extends Fragment {
    private FragmentSignUpBinding binding;
    private PreferenceManager preferenceManager;
    private Uri imageUri;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(getLayoutInflater());
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
        preferenceManager.putString(Constants.IMAGE_PROFILE, Constants.DEFAULT_IMAGE_PROFILE);
    }
    private void setListeners(){
        binding.signUpFragmentPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        binding.logInFragmentButtonBackToLogIn.setOnClickListener(view -> {
            requireActivity().onBackPressed();
        });
        binding.signUpFragmentImageProfileLayout.setOnClickListener(view -> showDialogImage());
        binding.signUpFragmentButtonCheck.setOnClickListener(view -> {
            if (binding.signUpFragmentName.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.name_cant_be_empty));
            }else if(binding.signUpFragmentPhoneNumber.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.phone_number_cant_be_empty));
            }else if (!binding.signUpFragmentPhoneNumber.getText().toString().trim().startsWith("+") || !Patterns.PHONE.matcher(binding.signUpFragmentPhoneNumber.getText().toString().trim()).matches()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.wrong_phone_number_format));
            }else if (binding.signUpFragmentPassword.getText().toString().trim().isEmpty()){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.phone_number_cant_be_empty));
            }else if (binding.signUpFragmentPassword.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.password_is_too_short));
            }else if (!binding.signUpFragmentPassword.getText().toString().trim().equals(binding.signUpFragmentConfirmPassword.getText().toString().trim())){
                ShowDialog.show(requireActivity(), getResources().getString(R.string.passwords_must_match));
            }else {
                signUp();
            }
        });
    }
    private void setMaxLength(){
        binding.signUpFragmentName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.NAME_MAX_LENGTH))});
        binding.signUpFragmentPhoneNumber.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.PHONE_MAX_LENGTH))});
        binding.signUpFragmentPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.PASSWORD_MAX_LENGTH))});
        binding.signUpFragmentConfirmPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.PASSWORD_MAX_LENGTH))});
    }
    @SuppressLint("DefaultLocale")
    public static String getRecoveryCode() {
        Random random = new Random();
        int number = random.nextInt(99999999);

        return String.format("%08d", number);
    }
    private void signUp() {
        ShowLoading.show(requireActivity());
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.PHONE_NUMBER, binding.signUpFragmentPhoneNumber.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.this_phone_number_is_already_linked_to_the_account));
                    } else {
                        String recoveryCode = getRecoveryCode();
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.NAME, binding.signUpFragmentName.getText().toString().trim());
                        user.put(Constants.PHONE_NUMBER, binding.signUpFragmentPhoneNumber.getText().toString().trim());
                        user.put(Constants.PASSWORD, binding.signUpFragmentPassword.getText().toString().trim());
                        user.put(Constants.IMAGE_PROFILE, preferenceManager.getString(Constants.IMAGE_PROFILE));
                        user.put(Constants.BIO, Constants.DEFAULT_BIO);
                        user.put(Constants.RECOVERY_CODE, recoveryCode);
                        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                                .add(user)
                                .addOnSuccessListener(documentReference -> {
                                    preferenceManager.putString(Constants.USER_ID, documentReference.getId());
                                    preferenceManager.putBoolean(Constants.IS_SIGNED_IN, true);
                                    preferenceManager.putBoolean(Constants.STATUS, true);
                                    preferenceManager.putString(Constants.NAME, binding.signUpFragmentName.getText().toString().trim());
                                    preferenceManager.putString(Constants.PHONE_NUMBER, binding.signUpFragmentPhoneNumber.getText().toString().trim());
                                    preferenceManager.putString(Constants.PASSWORD, binding.signUpFragmentPassword.getText().toString().trim());
                                    preferenceManager.putString(Constants.BIO, Constants.DEFAULT_BIO);
                                    preferenceManager.putString(Constants.RECOVERY_CODE, recoveryCode);
                                    ShowLoading.dismissDialog();
                                    Replace.replaceActivity(requireActivity(), new RecoveryCodeActivity(), true);
                                })
                                .addOnFailureListener(e -> {
                                    ShowLoading.dismissDialog();
                                    ShowDialog.show(requireActivity(), getResources().getString(R.string.error));
                                });
                    }
                });
    }
    private void showDialogImage(){
        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.dialog_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button showDialogImagePhotoCamera = dialog.findViewById(R.id.showDialogImageButtonCamera);
        Button showDialogImageGallery = dialog.findViewById(R.id.showDialogImageButtonGallery);

        showDialogImagePhotoCamera.setOnClickListener(view -> dialog.dismiss());
        showDialogImageGallery.setOnClickListener(view -> {
            dialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        dialog.setCancelable(true);
        dialog.create();
        dialog.show();
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    if (result.getData() != null){
                        imageUri = result.getData().getData();
                        updateFirebase();
                    }
                }
            }
    );
    private void updateFirebase(){
        if (imageUri != null){
            ShowLoading.show(requireActivity());
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PACKAGE + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final String downloadUri = String.valueOf(uriTask.getResult());
                        preferenceManager.putString(Constants.IMAGE_PROFILE, downloadUri);
                        binding.signUpFragmentImageProfileHelp.setVisibility(View.GONE);
                        Glide.with(this).load(imageUri).into(binding.signUpFragmentImageProfile);
                        ShowLoading.dismissDialog();
                    })
                    .addOnFailureListener(e -> {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(requireActivity(), getResources().getString(R.string.error));
                    });
        }
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = requireActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}