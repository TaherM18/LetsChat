package com.example.wechat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.ChatActivity;
import com.example.wechat.R;
import com.example.wechat.model.ChatModel;

import java.util.List;

public class RecyclerChatsAdapter extends RecyclerView.Adapter<RecyclerChatsAdapter.ViewHolder> {

    private Context context;
    private List<ChatModel> chatModelList;
    private int lastPosition = -1;

    public RecyclerChatsAdapter(Context context, List<ChatModel> chatModelList) {
        this.context = context;
        this.chatModelList = chatModelList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.chats_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.imgProfile.setImageURI();
        holder.txtUsername.setText(chatModelList.get(position).getUsername());
        holder.txtMessage.setText(chatModelList.get(position).getMessage());
        holder.txtTime.setText(chatModelList.get(position).getTime());
        holder.txtMessageCount.setText(String.valueOf(chatModelList.get(position).getMessageCount()));

        setAnimation(holder.itemView, position);

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(context, ChatActivity.class);
                chatIntent.putExtra("userId", chatModelList.get(position).getUsername());
                context.startActivity(chatIntent);
            }
        });

        holder.constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgProfile;
        private TextView txtUsername, txtMessage, txtTime, txtMessageCount;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtMessageCount = itemView.findViewById(R.id.txtMessageCount);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
        }
    }

    private void setAnimation(View view, int position) {
//        if (position > lastPosition) {
//            lastPosition = position;
            Animation slideInLeft = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
//            Animation slideInLeft = AnimationUtils.loadAnimation(context, R.anim.recycler_anim);
            view.startAnimation(slideInLeft);
//        }
    }
}
