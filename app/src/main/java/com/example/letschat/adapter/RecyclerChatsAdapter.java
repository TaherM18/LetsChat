package com.example.letschat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.model.ChatModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
        View view = layoutInflater.inflate(R.layout.chat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatModel chatModel = chatModelList.get(position);

        Glide.with(context).load(chatModel.getProfileUrl()).into(holder.imgProfile);
        holder.txtUsername.setText(chatModel.getUsername());
        holder.txtMessage.setText(chatModel.getMessage());
        holder.txtTime.setText(chatModel.getDateTime());
        holder.txtMessageCount.setText(chatModel.getMessageCount());

        setAnimation(holder.itemView, position);

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent chatIntent = new Intent(context, ChatActivity.class);
//                chatIntent.putExtra("userId", chatModelList.get(position).getUsername());
//                context.startActivity(chatIntent);
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

        private CircleImageView imgProfile;
        private TextView txtUsername, txtMessage, txtTime, txtMessageCount;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.civ_profile);
            txtUsername = itemView.findViewById(R.id.tv_username);
            txtMessage = itemView.findViewById(R.id.tv_message);
            txtTime = itemView.findViewById(R.id.tv_datetime);
            txtMessageCount = itemView.findViewById(R.id.tv_message_count);
            constraintLayout = itemView.findViewById(R.id.constraint_layout);
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
