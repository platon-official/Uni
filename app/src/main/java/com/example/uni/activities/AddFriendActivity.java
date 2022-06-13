package com.example.uni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Patterns;

import com.example.uni.R;
import com.example.uni.adapters.UsersAdapter;
import com.example.uni.databinding.ActivityAddFriendBinding;
import com.example.uni.listeners.UserListeners;
import com.example.uni.models.User;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.ShowDialog;
import com.example.uni.utilities.ShowLoading;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends BaseActivity implements UserListeners {
    private ActivityAddFriendBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        preferenceManager = new PreferenceManager(this);
        users = new ArrayList<>();
    }
    private void initFunc(){
        setListeners();
    }

    private void setListeners() {
        binding.addFriendActivityFriendPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        binding.addFriendActivityButtonBack.setOnClickListener(view -> onBackPressed());
        binding.addFriendActivityButtonFind.setOnClickListener(view -> {
            if (binding.addFriendActivityFriendPhoneNumber.getText().toString().trim().isEmpty()){
                ShowDialog.show(this, getResources().getString(R.string.phone_number_cannot_be_empty));
            } else if (!binding.addFriendActivityFriendPhoneNumber.getText().toString().trim().startsWith("+") || !Patterns.PHONE.matcher(binding.addFriendActivityFriendPhoneNumber.getText().toString().trim()).matches()) {
                ShowDialog.show(this, getResources().getString(R.string.wrong_phone_number_format));
            } else {
                findFriend();
            }
        });
    }
    private void findFriend(){
        ShowLoading.show(this);
        users.clear();
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.PHONE_NUMBER, binding.addFriendActivityFriendPhoneNumber.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            if (preferenceManager.getString(Constants.USER_ID).equals(queryDocumentSnapshot.getId())){
                                ShowLoading.dismissDialog();
                            } else {
                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.NAME);
                                user.phone = queryDocumentSnapshot.getString(Constants.PHONE_NUMBER);
                                user.image = queryDocumentSnapshot.getString(Constants.IMAGE_PROFILE);
                                user.token = queryDocumentSnapshot.getString(Constants.FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                users.add(user);
                                ShowLoading.dismissDialog();
                            }
                        }
                        if (users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this, this);
                            binding.addFriendActivityRecyclerView.setAdapter(usersAdapter);
                        }
                    } else {
                        ShowLoading.dismissDialog();
                        ShowDialog.show(this, getResources().getString(R.string.we_couldnt_find_this_account));
                    }
                }).addOnFailureListener(e -> {
                    ShowLoading.dismissDialog();
                    ShowDialog.show(this, getResources().getString(R.string.error));
        });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.USER, user);
        startActivity(intent);
        finish();
    }
}