package com.example.uni.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import com.example.uni.R;
import com.example.uni.databinding.ActivityChangeBioBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.ShowDialog;
import com.example.uni.utilities.ShowLoading;
import com.example.uni.utilities.ShowToast;

public class ChangeBioActivity extends BaseActivity {
    private ActivityChangeBioBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeBioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        setUserInfo();
        binding.activityChangeBioNew.requestFocus();
        setMaxLength();
        setCount();
    }
    private void setUserInfo(){
        binding.activityChangeBioNew.setText(preferenceManager.getString(Constants.BIO));
    }
    private void setListeners(){
        binding.activityChangeBioButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangeBioButtonCheck.setOnClickListener(view -> {
            if (binding.activityChangeBioNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.bio_cant_be_empty));
            } else {
                ShowLoading.show(this);
                InitFirebase.firebaseFirestore.collection(Constants.USERS)
                        .document(preferenceManager.getString(Constants.USER_ID))
                        .update(Constants.BIO, binding.activityChangeBioNew.getText().toString().trim())
                        .addOnSuccessListener(unused -> {
                            ShowLoading.dismissDialog();
                            preferenceManager.putString(Constants.BIO, binding.activityChangeBioNew.getText().toString().trim());
                            ShowToast.show(this, getResources().getString(R.string.your_bio_updated), false);
                            onBackPressed();
                        }).addOnFailureListener(e -> {
                            ShowLoading.dismissDialog();
                            ShowDialog.show(this, getResources().getString(R.string.error));
                });
            }
        });
        binding.activityChangeBioNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                setCount();
            }
        });
    }
    private void setMaxLength(){
        binding.activityChangeBioNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.BIO_MAX_LENGTH))});
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityChangeBioCount.setText(binding.activityChangeBioNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.BIO_MAX_LENGTH);
    }
}