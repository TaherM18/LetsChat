package com.example.letschat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.databinding.RecentChatRowBinding;
import com.example.letschat.model.ChatroomModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.chat.ChatActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class RecyclerChatsAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecyclerChatsAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater layoutInflater;
    private int lastPosition = -1;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RecyclerChatsAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recent_chat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ChatroomModel model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                        boolean messageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                        if ( messageSentByMe ) {
                            holder.binding.tvMessage.setText("You: "+model.getLastMessage());
                        }
                        else {
                            holder.binding.tvMessage.setText(model.getLastMessage());
                        }
                        Glide.with(context).load(otherUserModel.getProfileImage()).into(holder.binding.civProfile);
                        holder.binding.tvUsername.setText(otherUserModel.getUserName());
                        holder.binding.tvDatetime.setText(FirebaseUtil.formatTimestamp(model.getLastMessageTimestamp()));
                        //holder.binding.tvMessageCount.setText(model.getMessageCount());

                        holder.binding.civProfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AndroidUtil.transitToViewImage(context, holder.binding.civProfile);
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                                context.startActivity(intent);
                            }
                        });

                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                return true;
                            }
                        });

                        setAnimation(holder.itemView);
                    }
                }
            });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        RecentChatRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RecentChatRowBinding.bind(itemView);
        }
    }

    private void setAnimation(View view) {
        Animation slideInLeft = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        view.startAnimation(slideInLeft);
    }
}
