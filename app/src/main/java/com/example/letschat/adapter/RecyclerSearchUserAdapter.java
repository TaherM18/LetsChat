package com.example.letschat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.databinding.ContactRowBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.view.chat.ChatActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecyclerSearchUserAdapter extends FirestoreRecyclerAdapter<UserModel, RecyclerSearchUserAdapter.ViewHolder> {

    Context context;
    LayoutInflater layoutInflater;

    public RecyclerSearchUserAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull UserModel model) {
        holder.binding.txtContactName.setText(model.getUserName());
        holder.binding.txtContactNumber.setText(model.getPhone());
        Glide.with(context).load(model.getProfileImage()).into(holder.binding.civProfile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AndroidUtil.passUserModelAsIntent(intent, model);
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.contact_row, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ContactRowBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ContactRowBinding.bind(itemView);
        }
    }
}
