package com.example.uni.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegisterBinding binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}