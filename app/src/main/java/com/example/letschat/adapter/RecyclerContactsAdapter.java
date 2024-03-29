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
import com.example.letschat.model.ChatModel;
import com.example.letschat.model.ContactModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.chat.ChatActivity;

import java.util.List;

public class RecyclerContactsAdapter extends RecyclerView.Adapter<RecyclerContactsAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userModelList;
    private LayoutInflater layoutInflater;

    public RecyclerContactsAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.contact_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);

        Glide.with(context).load(userModel.getProfileImage()).into(holder.binding.civProfile);
        holder.binding.txtContactName.setText(userModel.getUserName());
        holder.binding.txtContactNumber.setText(userModel.getPhone());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AndroidUtil.passUserModelAsIntent(intent, userModel);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ContactRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ContactRowBinding.bind(itemView);
        }
    }
}
