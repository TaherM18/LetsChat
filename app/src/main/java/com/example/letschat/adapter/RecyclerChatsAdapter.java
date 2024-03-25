package com.example.letschat.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.letschat.databinding.ChatRowBinding;
import com.example.letschat.model.ChatModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.view.chat.ChatActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerChatsAdapter extends RecyclerView.Adapter<RecyclerChatsAdapter.ViewHolder> {

    private Context context;
    private List<ChatModel> chatModelList;
    private LayoutInflater layoutInflater;
    private int lastPosition = -1;

    public RecyclerChatsAdapter(Context context, List<ChatModel> chatModelList) {
        this.context = context;
        this.chatModelList = chatModelList;
        this.layoutInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.chat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatModel chatModel = chatModelList.get(position);

        Glide.with(context).load(chatModel.getProfileUrl()).into(holder.binding.civProfile);
        holder.binding.tvUsername.setText(chatModel.getUsername());
        holder.binding.tvMessage.setText(chatModel.getMessage());
        holder.binding.tvDatetime.setText(chatModel.getDateTime());
        holder.binding.tvMessageCount.setText(chatModel.getMessageCount());

        setAnimation(holder.itemView, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(context, ChatActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                AndroidUtil.passUserModelAsIntent(intent, userModel);
//                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
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

        ChatRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ChatRowBinding.bind(itemView);
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
