package com.example.uni.activities;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.uni.databinding.ActivityUserInfoBinding;
import com.example.uni.models.User;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;

public class UserInfoActivity extends BaseActivity {
    private ActivityUserInfoBinding binding;
    private User receiverUser;
    private String imageProfile;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.USER);
    }
    private void initFunc(){
        setListeners();
        setUserInfo();
    }
    private void setListeners(){
        binding.userActivityButtonBack.setOnClickListener(view -> onBackPressed());
        binding.userActivityHeaderImageProfile.setOnClickListener(view -> {
            Intent imageUser = new Intent(this, ImageUserActivity.class);
            imageUser.putExtra(Constants.ARGUMENT_IMAGE_PROFILE, imageProfile);
            imageUser.putExtra(Constants.ARGUMENT_NAME, userName);
            startActivity(imageUser);
        });
    }
    private void setUserInfo(){
        InitFirebase.firebaseFirestore.collection(Constants.USERS).document(receiverUser.id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    imageProfile = documentSnapshot.getString(Constants.IMAGE_PROFILE);
                    userName = documentSnapshot.getString(Constants.NAME);
                    binding.userActivityTitle.setText(userName);
                    Glide.with(this).load(imageProfile).into(binding.userActivityHeaderImageProfile);
                    binding.userActivityButtonPhoneNumberText.setText(documentSnapshot.getString(Constants.USERNAME));
                    binding.userActivityButtonBioText.setText(documentSnapshot.getString(Constants.BIO));
                });
    }
}