package com.example.uni.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import com.example.uni.R;
import com.example.uni.databinding.ActivityChangeUsernameBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.ShowDialog;
import com.example.uni.utilities.ShowLoading;
import com.example.uni.utilities.ShowToast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

public class ChangeUsernameActivity extends AppCompatActivity {
    private ActivityChangeUsernameBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeUsernameBinding.inflate(getLayoutInflater());
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
        binding.activityChangeUsernameNew.requestFocus();
        setMaxLength();
        setCount();
    }
    private void setListeners(){
        binding.activityChangeUsernameNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setCount();
            }
        });
        binding.activityChangeUsernameButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChangeUsernameButtonCheck.setOnClickListener(view -> {
            if (binding.activityChangeUsernameNew.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.username_can_not_be_empty));
            }else if (!binding.activityChangeUsernameNew.getText().toString().trim().startsWith(Constants.USERNAME_SIGN)){
                ShowDialog.show(this, getResources().getString(R.string.username_must_start_with) + " '" + Constants.USERNAME_SIGN + "'");
            }else {
                changeUsername();
            }
        });
    }
    private void changeUsername(){
        ShowLoading.show(this);
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.USERNAME, binding.activityChangeUsernameNew.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.this_username_is_already_linked_to_the_account));
                    } else {
                        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                                .document(preferenceManager.getString(Constants.USER_ID)).update(Constants.USERNAME, binding.activityChangeUsernameNew.getText().toString().trim())
                                .addOnSuccessListener(unused -> {
                                    ShowLoading.dismissDialog();
                                    preferenceManager.putString(Constants.USERNAME, binding.activityChangeUsernameNew.getText().toString().trim());
                                    ShowToast.show(this, getResources().getString(R.string.username_updated_successfully), false);
                                    onBackPressed();
                                }).addOnFailureListener(e -> {
                                    ShowLoading.dismissDialog();
                                    ShowDialog.show(this, getResources().getString(R.string.error));
                        });
                    }
                });
    }
    private void setUserInfo(){
        binding.activityChangeUsernameNew.setText(preferenceManager.getString(Constants.USERNAME));
    }
    private void setMaxLength(){
        binding.activityChangeUsernameNew.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Integer.parseInt(Constants.USERNAME_MAX_LENGTH))});
    }
    @SuppressLint("SetTextI18n")
    private void setCount(){
        binding.activityChangeUsernameCount.setText(binding.activityChangeUsernameNew.getText().toString().trim().length() + getResources().getString(R.string.delimiter) + Constants.USERNAME_MAX_LENGTH);
    }
}