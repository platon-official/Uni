package com.example.uni.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uni.databinding.ContainerMessageBinding;
import com.example.uni.databinding.ContainerReceivedMessageBinding;
import com.example.uni.models.ChatMessage;
import com.example.uni.utilities.Constants;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    private String receiverProfileImage;
    private final String senderId;
    private final Context context;

    public void setReceiverProfileImage(String imageUri){
        receiverProfileImage = imageUri;
    }
    public ChatAdapter(List<ChatMessage> chatMessages, String receiverProfileImage, String senderId, Context context) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constants.VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ContainerMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ContainerReceivedMessageBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == Constants.VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage, context);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return Constants.VIEW_TYPE_SENT;
        } else {
            return Constants.VIEW_TYPE_RECEIVED;
        }
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ContainerMessageBinding binding;

        public SentMessageViewHolder(ContainerMessageBinding containerMessageBinding){
            super(containerMessageBinding.getRoot());
            binding = containerMessageBinding;
        }
        public void setData(ChatMessage chatMessage){
            binding.containerMessageText.setText(chatMessage.message);
            binding.containerMessageDateTime.setText(chatMessage.dateTime);
        }
    }
    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ContainerReceivedMessageBinding containerReceivedMessageBinding){
            super(containerReceivedMessageBinding.getRoot());
            binding = containerReceivedMessageBinding;
        }
        private void setData(ChatMessage chatMessage, String receiverProfileImage, Context context){
            binding.containerMessageText.setText(chatMessage.message);
            binding.containerMessageDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null){
                Glide.with(context).load(receiverProfileImage).into(binding.containerImageProfile);
            }
        }
    }
}
