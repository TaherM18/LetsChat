package com.example.letschat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.R;
import com.example.letschat.databinding.ChatMessageRowBinding;
import com.example.letschat.model.ChatModel;
import com.example.letschat.model.MessageModel;
import com.example.letschat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class RecyclerMessageAdapter extends FirestoreRecyclerAdapter<MessageModel, RecyclerMessageAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater layoutInflater;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RecyclerMessageAdapter(@NonNull FirestoreRecyclerOptions<MessageModel> options, Context context) {
        super(options);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.chat_message_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull MessageModel model) {
        if (model.getSenderId().equals(FirebaseUtil.currentUserId())) {
            // Current User's Message
            holder.binding.cardLeftMessage.setVisibility(View.GONE);
            holder.binding.cardRightMessage.setVisibility(View.VISIBLE);
            holder.binding.txtRightMessage.setText(model.getMessage());
            holder.binding.txtRightTime.setText(model.getTimestamp().toString());
        }
        else {
            // Other User's Message
            holder.binding.cardRightMessage.setVisibility(View.GONE);
            holder.binding.cardLeftMessage.setVisibility(View.VISIBLE);
            holder.binding.txtLeftMessage.setText(model.getMessage());
            holder.binding.txtLeftTime.setText(model.getTimestamp().toString());
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ChatMessageRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ChatMessageRowBinding.bind(itemView);
        }
    }

}

