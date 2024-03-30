package com.example.letschat.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.R;
import com.example.letschat.databinding.ChatMessageRecyclerRowBinding;
import com.example.letschat.databinding.MessageSenderRowBinding;
import com.example.letschat.model.ChatroomModel;
import com.example.letschat.model.MessageModel;
import com.example.letschat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;

import java.util.List;

import kotlin.jvm.functions.Function1;

public class RecyclerMessageAdapter extends FirestoreRecyclerAdapter<MessageModel, RecyclerMessageAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater layoutInflater;
    private String chatroomId;
    private final int SENDER_ROW = 1;
    private final int RECEIVER_ROW = 2;
    private int[] reactions = new int[]{
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
    };

    // Store information about the long-pressed item
    private int selectedItemPosition = -1;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RecyclerMessageAdapter(@NonNull FirestoreRecyclerOptions<MessageModel> options, Context context, String chatroomId) {
        super(options);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.chatroomId = chatroomId;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull MessageModel model) {

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, new Function1<Integer, Boolean>() {
            @Override
            public Boolean invoke(Integer index) {

                if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
                    if (index != -1) {
                        holder.binding.imgReactionRight.setImageResource(reactions[index]);
                        holder.binding.imgReactionRight.setVisibility(View.VISIBLE);
                        FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
                                .update("reaction", index);
                    }
                } else {
                    if (index != -1) {
                        holder.binding.imgReactionLeft.setImageResource(reactions[index]);
                        holder.binding.imgReactionLeft.setVisibility(View.VISIBLE);
                        FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
                                .update("reaction", index);
                    }
                }

                return true; // true is closing popup, false is requesting a new selection
            }
        });


        if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
            // Current User's Message
            holder.binding.layoutLeft.setVisibility(View.GONE);
            holder.binding.layoutRight.setVisibility(View.VISIBLE);
            holder.binding.txtMessageRight.setText(model.getMessage());
            holder.binding.txtTimeRight.setText(FirebaseUtil.formatTimestamp(model.getTimestamp()));
            if (model.getReaction() != -1) {
                holder.binding.imgReactionRight.setImageResource(reactions[model.getReaction()]);
                holder.binding.imgReactionRight.setVisibility(View.VISIBLE);
            }
        } else {
            // Other User's Message
            holder.binding.layoutRight.setVisibility(View.GONE);
            holder.binding.layoutLeft.setVisibility(View.VISIBLE);
            holder.binding.txtMessageLeft.setText(model.getMessage());
            holder.binding.txtTimeLeft.setText(FirebaseUtil.formatTimestamp(model.getTimestamp()));
            if (model.getReaction() != -1) {
                holder.binding.imgReactionLeft.setImageResource(reactions[model.getReaction()]);
                holder.binding.imgReactionLeft.setVisibility(View.VISIBLE);
            }
        }

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popup.onTouch(v, event);
                return true;
            }
        });

         // Register item view for the context menu
//        holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//            @Override
//            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                // Inflate the custom context menu layout
//                MenuInflater inflater = new MenuInflater(holder.itemView.getContext());
//                inflater.inflate(R.menu.reactions_context_menu, menu);
//
//                // Handle like click
//                menu.findItem(R.id.menu_like).setOnMenuItemClickListener(item -> {
//                    if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
//
//                        holder.binding.imgReactionRight.setImageResource(reactions[0]);
//                        holder.binding.imgReactionRight.setVisibility(View.VISIBLE);
//                        FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
//                                .update("reaction", 0);
//                    } else {
//                        holder.binding.imgReactionLeft.setImageResource(reactions[0]);
//                        holder.binding.imgReactionLeft.setVisibility(View.VISIBLE);
//                        FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
//                                .update("reaction", 0);
//                    }
//                    return true;
//                });
//                // Handle love click
//                menu.findItem(R.id.menu_love).setOnMenuItemClickListener(item -> {
//                    if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
//
//                        holder.binding.imgReactionRight.setImageResource(reactions[1]);
//                        holder.binding.imgReactionRight.setVisibility(View.VISIBLE);
//                        FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
//                                .update("reaction", 1);
//                    } else {
//                        holder.binding.imgReactionLeft.setImageResource(reactions[1]);
//                        holder.binding.imgReactionLeft.setVisibility(View.VISIBLE);
//                        FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
//                                .update("reaction", 1);
//                    }
//                    return true;
//                });
//            }
//        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ChatMessageRecyclerRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ChatMessageRecyclerRowBinding.bind(itemView);
        }
    }

}

