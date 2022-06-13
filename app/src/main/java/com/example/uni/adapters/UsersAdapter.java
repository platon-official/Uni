package com.example.uni.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uni.databinding.UserBinding;
import com.example.uni.listeners.UserListeners;
import com.example.uni.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{
    private final List<User> users;
    private final Context context;
    private final UserListeners userListeners;

    public UsersAdapter(List<User> users, Context context, UserListeners userListeners) {
        this.users = users;
        this.context = context;
        this.userListeners = userListeners;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserBinding userBinding = UserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(userBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        private final UserBinding binding;

        public UserViewHolder(UserBinding userBinding){
           super(userBinding.getRoot());
           binding = userBinding;
       }
       private void setUserData(User user){
            binding.userName.setText(user.name);
            binding.userPhoneNumber.setText(user.phone);
            Glide.with(context).load(user.image).into(binding.userImageProfile);
            binding.getRoot().setOnClickListener(view -> userListeners.onUserClicked(user));
       }
    }
}
