package com.example.wechat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.model.Chat;

import java.util.List;

public class RecyclerChatsAdapter extends RecyclerView.Adapter<RecyclerChatsAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chatList;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgProfile;
        private TextView txtUsername, txtMessage, txtTime, txtMessageCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
