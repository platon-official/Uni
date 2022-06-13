package com.example.uni.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.example.uni.R;
import com.example.uni.adapters.RecentChatAdapter;
import com.example.uni.databinding.ActivityMainBinding;
import com.example.uni.databinding.NavigationHeaderBinding;
import com.example.uni.listeners.ConversationListener;
import com.example.uni.models.ChatMessage;
import com.example.uni.models.User;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.Replace;
import com.example.uni.utilities.ShowLoading;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversationListener {
    private ActivityMainBinding binding;
    private List<ChatMessage> conversations;
    private RecentChatAdapter recentChatAdapter;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initFields();
        initFunc();
    }
    private void initFields(){
        conversations = new ArrayList<>();
        recentChatAdapter = new RecentChatAdapter(this, conversations, this);
        preferenceManager = new PreferenceManager(this);
    }
    private void initFunc(){
        setListeners();
        setAdapter();
        getToken();
        listenConversations();
    }
    private void setListeners(){
        binding.activityMainImageProfile.setOnClickListener(view -> {
            binding.activityMainDrawerLayout.openDrawer(GravityCompat.START);
        });
        binding.activityMainNavigationView.setNavigationItemSelectedListener(item -> {
            binding.activityMainDrawerLayout.closeDrawer(GravityCompat.START);
            if (item.getItemId() == R.id.settings){
                Replace.replaceActivity(this, new SettingsActivity(), false);
            } else if (item.getItemId() == R.id.friend){
                Replace.replaceActivity(this, new AddFriendActivity(), false);
            }
            return true;
        });
    }
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        recentChatAdapter.notifyDataSetChanged();
        Glide.with(this).load(preferenceManager.getString(Constants.IMAGE_PROFILE)).into(binding.activityMainImageProfile);
        NavigationHeaderBinding headerBinding = NavigationHeaderBinding.bind(binding.activityMainNavigationView.getHeaderView(0));
        headerBinding.headerUserName.setText(preferenceManager.getString(Constants.NAME));
        headerBinding.headerUserPhone.setText(preferenceManager.getString(Constants.PHONE_NUMBER));
        Glide.with(this).load(preferenceManager.getString(Constants.IMAGE_PROFILE)).into(headerBinding.headerImageProfile);
        headerBinding.headerImageProfile.setOnClickListener(view -> {
            binding.activityMainDrawerLayout.closeDrawer(GravityCompat.START);
            Replace.replaceActivity(this, new SettingsActivity(), false);
        });
    }
    private void setAdapter(){
        binding.activityMainRecyclerView.setAdapter(recentChatAdapter);
    }
    private void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        preferenceManager.putString(Constants.FCM_TOKEN, token);
        InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .document(preferenceManager.getString(Constants.USER_ID)).update(Constants.FCM_TOKEN, token);
    }
    private void setWarningVisibility(Boolean isVisible){
        if (isVisible){
            binding.activityMainWarningLayout.setVisibility(View.VISIBLE);
        }else {
            binding.activityMainWarningLayout.setVisibility(View.GONE);
        }
    }
    private void listenConversations(){
        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        ShowLoading.show(this);
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange: value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversations.size();i++){
                        String senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                        String receivedId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receivedId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            setWarningVisibility(conversations.isEmpty());
            Collections.sort(conversations, (Object1, Object2) -> Object2.dateObject.compareTo(Object1.dateObject));
            recentChatAdapter.notifyDataSetChanged();
            binding.activityMainRecyclerView.scrollToPosition(0);
            ShowLoading.dismissDialog();
        }
    });

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.USER, user);
        startActivity(intent);
    }
}