package com.example.uni.activities;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.uni.databinding.ActivityImageUserBinding;
import com.example.uni.utilities.Constants;

public class ImageUserActivity extends BaseActivity {
    private ActivityImageUserBinding binding;
    private String userName;
    private String imageProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        userName = (String) getIntent().getSerializableExtra(Constants.ARGUMENT_NAME);
        imageProfile = (String) getIntent().getSerializableExtra(Constants.ARGUMENT_IMAGE_PROFILE);
    }
    private void initFunc(){
        setListeners();
        setUserInfo();
    }
    private void setListeners(){
        binding.activityImageUserProfileButtonBack.setOnClickListener(view -> onBackPressed());
    }
    private void setUserInfo(){
        Glide.with(this).load(imageProfile).into(binding.activityImageUserImageProfile);
        binding.activityImageUserProfileTitle.setText(userName);
    }
}
