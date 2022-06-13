package com.example.uni.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.uni.R;
import com.example.uni.adapters.ChatAdapter;
import com.example.uni.databinding.ActivityChatBinding;
import com.example.uni.models.ChatMessage;
import com.example.uni.models.User;
import com.example.uni.retrofit.Api;
import com.example.uni.retrofit.ApiService;
import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.example.uni.utilities.ShowToast;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private String conversationId;
    private Boolean isReceiverAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFields();
        initFunc();
    }
    private void initFields(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.USER);
        preferenceManager = new PreferenceManager(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                receiverUser.image,
                preferenceManager.getString(Constants.USER_ID),
                this
        );
        conversationId = null;
        isReceiverAvailable = false;
    }
    private void initFunc() {
        setListeners();
        setUserInfo();
        setAdapter();
        setSendButton();
        listenMessages();
    }
    private void setUserInfo(){
        binding.activityChatUserName.setText(receiverUser.name);
        Glide.with(this).load(receiverUser.image).into(binding.activityChatImageProfile);
    }
    @Override
    protected void onResume() {
        super.onResume();
        listenAvailable();
    }
    private void listenAvailable(){
        InitFirebase.firebaseFirestore.collection(Constants.USERS).document(
              receiverUser.id
        ).addSnapshotListener(this, ((value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.AVAILABLE) != null){
                    int available = Objects.requireNonNull(
                            value.getLong(Constants.AVAILABLE)
                    ).intValue();
                    isReceiverAvailable = available == 1;
                }
                receiverUser.token = value.getString(Constants.FCM_TOKEN);
                if (receiverUser.image == null){
                    receiverUser.image = value.getString(Constants.IMAGE_PROFILE);
                    chatAdapter.setReceiverProfileImage(receiverUser.image);
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable){
                binding.activityChatOnline.setVisibility(View.VISIBLE);
            } else {
                binding.activityChatOnline.setVisibility(View.GONE);
            }
        }));
    }
    private void setAdapter(){
        binding.activityChatsRecyclerView.setAdapter(chatAdapter);
    }
    private void setSendButton(){
        if (binding.activityChatsMessage.getText().toString().trim().isEmpty()){
            binding.activityChatsButtonSend.setVisibility(View.INVISIBLE);
        }else {
            binding.activityChatsButtonSend.setVisibility(View.VISIBLE);
        }
    }
    private void setListeners(){
        binding.activityChatUser.setOnClickListener(view -> {
            Intent userActivity = new Intent(this, UserInfoActivity.class);
            userActivity.putExtra(Constants.USER, receiverUser);
            startActivity(userActivity);
        });
        binding.activityChatsMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setSendButton();
            }
        });
        binding.activityChatsButtonBack.setOnClickListener(view -> onBackPressed());
        binding.activityChatsButtonSend.setOnClickListener(view -> sendMessage());
    }
    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
        message.put(Constants.RECEIVER_ID, receiverUser.id);
        message.put(Constants.MESSAGE, binding.activityChatsMessage.getText().toString().trim());
        message.put(Constants.TIMESTAMP, new Date());
        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT).add(message);
        if (conversationId != null){
            updateConversion(binding.activityChatsMessage.getText().toString().trim());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID));
            conversation.put(Constants.SENDER_NAME, preferenceManager.getString(Constants.NAME));
            conversation.put(Constants.SENDER_IMAGE, preferenceManager.getString(Constants.IMAGE_PROFILE));
            conversation.put(Constants.RECEIVER_ID, receiverUser.id);
            conversation.put(Constants.RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.LAST_MESSAGE, binding.activityChatsMessage.getText().toString().trim());
            conversation.put(Constants.TIMESTAMP, new Date());
            addConversation(conversation);
        }
        if (!isReceiverAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.USER_ID, preferenceManager.getString(Constants.USER_ID));
                data.put(Constants.NAME, preferenceManager.getString(Constants.NAME));
                data.put(Constants.FCM_TOKEN, preferenceManager.getString(Constants.FCM_TOKEN));
                data.put(Constants.MESSAGE, binding.activityChatsMessage.getText().toString().trim());
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_DATA, data);
                body.put(Constants.REMOTE_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            } catch (Exception e) {
                ShowToast.show(this, getResources().getString(R.string.error), true);
            }
        }
        binding.activityChatsMessage.setText(null);
    }
    private void sendNotification(String messageBody){
        Api.getClient().create(ApiService.class).sendMessages(
                Constants.getRemoteHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                ShowToast.show(ChatActivity.this, getResources().getString(R.string.error) + error.getString("error"), true);
                            }
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                } else {
                    ShowToast.show(ChatActivity.this, getResources().getString(R.string.error) + response.code(), true);
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                ShowToast.show(ChatActivity.this, getResources().getString(R.string.error), true);
            }
        });
    }
    private void setWarningVisibility(Boolean isVisible){
        if (isVisible){
            binding.activityChatWarningLayout.setVisibility(View.VISIBLE);
        } else{
            binding.activityChatWarningLayout.setVisibility(View.GONE);
        }
    }
    private void listenMessages(){
        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER_ID, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        InitFirebase.firebaseFirestore.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.RECEIVER_ID, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            Integer count = chatMessages.size();
            for (DocumentChange documentChange: value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.MESSAGE);
                    chatMessage.dateTime = getDateTime(documentChange.getDocument().getDate(Constants.TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            if (chatMessages.isEmpty()){
                setWarningVisibility(true);
            } else {
                setWarningVisibility(false);
            }
            Collections.sort(chatMessages, (Object1, Object2) -> Object1.dateObject.compareTo(Object2.dateObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.activityChatsRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        }
        if (conversationId == null){
            checkForConversation();
        }
    };
    private String getDateTime(Date date){
        return new SimpleDateFormat("dd MMMM, yyyy · hh:mm a", Locale.getDefault()).format(date);
    }
    private void checkForConversation(){
        if (chatMessages.size() != 0){
            checkForConversationRemotely(
                    preferenceManager.getString(Constants.USER_ID),
                    receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.USER_ID)
            );
        }
    }
    private void checkForConversationRemotely(String senderId, String receiverId){
        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .whereEqualTo(Constants.SENDER_ID, senderId)
                .whereEqualTo(Constants.RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        conversationId = documentSnapshot.getId();
                    }
                });
    }
    private void addConversation(HashMap<String, Object> conversation){
        InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference =
                InitFirebase.firebaseFirestore.collection(Constants.CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.LAST_MESSAGE,
                message,
                Constants.TIMESTAMP,
                new Date()
        );
    }
}