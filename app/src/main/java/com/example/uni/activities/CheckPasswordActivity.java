package com.example.uni.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.example.uni.R;
import com.example.uni.databinding.ActivityCheckPasswordBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.Replace;
import com.example.uni.utilities.ShowDialog;
import com.example.uni.utilities.ShowLoading;

import java.util.Random;

public class CheckPasswordActivity extends BaseActivity {
    private ActivityCheckPasswordBinding binding;
    private PreferenceManager preferenceManager;
    private Boolean type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
        type = getIntent().getBooleanExtra(Constants.ARGUMENT_PASSWORD, false);
    }
    private void initFunc(){
        setListeners();
        binding.activityCheckPasswordNew.requestFocus();
    }
    private void setListeners(){
        binding.activityCheckPasswordButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityCheckPasswordButtonCheck.setOnClickListener(view -> {
            if (binding.activityCheckPasswordNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.password_cannot_be_empty));
            }else if (binding.activityCheckPasswordNew.getText().toString().trim().length() < Constants.MINIMUM_PASSWORD_LENGTH){
                ShowDialog.show(this, getResources().getString(R.string.password_is_too_short));
            } else {
                checkPassword();
            }
        });
    }
    @SuppressLint("DefaultLocale")
    public static String getRecoveryCode() {
        Random random = new Random();
        int number = random.nextInt(99999999);

        return String.format("%08d", number);
    }
    private void checkPassword(){
        if (binding.activityCheckPasswordNew.getText().toString().trim().equals(preferenceManager.getString(Constants.PASSWORD))){
            if (type){
                Replace.replaceActivity(this, new ChangePasswordActivity(), true);
            }else {
                String recoveryCode = getRecoveryCode();
                ShowLoading.show(this);
                InitFirebase.firebaseFirestore.collection(Constants.USERS)
                        .document(preferenceManager.getString(Constants.USER_ID)).update(Constants.RECOVERY_CODE, recoveryCode)
                        .addOnSuccessListener(unused -> {
                            ShowLoading.dismissDialog();
                            preferenceManager.putString(Constants.RECOVERY_CODE, recoveryCode);
                            Replace.replaceActivity(this, new RecoveryCodeActivity(), true);
                        }).addOnFailureListener(e -> {
                            ShowLoading.dismissDialog();
                            ShowDialog.show(this, getResources().getString(R.string.error));
                        });
            }
        } else {
            ShowDialog.show(this, getResources().getString(R.string.incorrect_password));
        }
    }
}