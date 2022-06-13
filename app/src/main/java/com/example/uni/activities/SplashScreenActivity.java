package com.example.uni.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni.databinding.ActivitySplashScreenBinding;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.Replace;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    private ActivitySplashScreenBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
        InitFirebase.init();
    }
    private void initFunc(){
        setVersion();
        create();
    }
    private void setVersion(){
        binding.splashScreenActivityVersion.setText(Constants.APP_VERSION);
    }
    private void create(){
        new Handler().postDelayed(() -> {
            if (preferenceManager.getBoolean(Constants.IS_SIGNED_IN)){
                Replace.replaceActivity(this, new MainActivity(), true);
            } else {
                Replace.replaceActivity(this, new RegisterActivity(), true);
            }
        }, Constants.DELAY_MILLS);
    }
}