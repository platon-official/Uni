package com.example.uni.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni.databinding.ActivityRecoveryCodeBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.Replace;

public class RecoveryCodeActivity extends AppCompatActivity {
    private ActivityRecoveryCodeBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecoveryCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        setCode();
    }
    private void setListeners(){
        binding.activityRecoveryCodeButtonStartMessaging.setOnClickListener(view -> Replace.replaceActivity(this, new MainActivity(), true));
    }
    private void setCode(){
        binding.activityRecoveryCodeText.setText(preferenceManager.getString(Constants.RECOVERY_CODE));
    }
}