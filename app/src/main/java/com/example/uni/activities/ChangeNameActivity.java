package com.example.uni.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import com.example.uni.R;
import com.example.uni.databinding.ActivityChangeNameBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.ShowDialog;
import com.example.uni.utilities.ShowLoading;
import com.example.uni.utilities.ShowToast;

public class ChangeNameActivity extends BaseActivity {
    private ActivityChangeNameBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeNameBinding.inflate(getLayoutInflater());
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
        binding.activityChangeNameNew.requestFocus();
        setMaxLength();
        setCount();
    }
    private void setUserInfo(){
        binding.activityChangeNameNew.setText(preferenceManager.getString(Constants.NAME));
    }
    private void setMaxLength(){
        binding.activityChangeNameNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.NAME_MAX_LENGTH))});
    }
    private void setListeners(){
        binding.activityChangeNameNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                setCount();
            }
        });
        binding.activityChangeNameButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangeNameButtonCheck.setOnClickListener(view -> {
            if (binding.activityChangeNameNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.name_can_not_be_empty));
            } else {
                changeName();
            }
        });
    }
    private void changeName(){
        ShowLoading.show(this);
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .document(preferenceManager.getString(Constants.USER_ID)).update(Constants.NAME, binding.activityChangeNameNew.getText().toString().trim())
                .addOnSuccessListener(unused -> {
                    ShowLoading.dismissDialog();
                    preferenceManager.putString(Constants.NAME, binding.activityChangeNameNew.getText().toString().trim());
                    ShowToast.show(this, getResources().getString(R.string.name_updated_successfully), false);
                    onBackPressed();
                }).addOnFailureListener(e -> {
            ShowLoading.dismissDialog();
            ShowDialog.show(this, getResources().getString(R.string.error));
        });
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityChangeNameCount.setText(binding.activityChangeNameNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.NAME_MAX_LENGTH);
    }
}
