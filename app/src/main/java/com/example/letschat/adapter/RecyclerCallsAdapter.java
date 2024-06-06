package com.example.letschat.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.databinding.CallRowBinding;
import com.example.letschat.model.ContactModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;

import java.util.List;

public class RecyclerCallsAdapter extends RecyclerView.Adapter<RecyclerCallsAdapter.ViewHolder> {

    private Context context;
    private List<ContactModel> contactModelList;
    private int lastPosition = -1;

    public RecyclerCallsAdapter(Context context, List<ContactModel> contactModelList) {
        this.context = context;
        this.contactModelList = contactModelList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.call_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactModel model = contactModelList.get(position);

        if (model.getPhotoUri() != null) {
            Glide.with(context).load(model.getPhotoUri()).into(holder.binding.civProfile);
        }
        else {
            Glide.with(context).load(R.drawable.person_placeholder_360x360).into(holder.binding.civProfile);
        }
        holder.binding.tvUsername.setText(model.getName());
        holder.binding.tvDatetime.setText(AndroidUtil.formatDate(model.getDate()));
        setAnimation(holder.itemView, position);

        switch (model.getCallType()) {
            case CallLog.Calls.INCOMING_TYPE:
                holder.binding.ivArrow.setImageDrawable(context.getDrawable(R.drawable.call_received_24));
                holder.binding.ivArrow.getDrawable().setTint(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                holder.binding.ivArrow.setImageDrawable(context.getDrawable(R.drawable.call_made_24));
                holder.binding.ivArrow.getDrawable().setTint(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case CallLog.Calls.MISSED_TYPE:
                holder.binding.ivArrow.setImageDrawable(context.getDrawable(R.drawable.call_received_24));
                holder.binding.ivArrow.getDrawable().setTint(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                holder.binding.ivArrow.setImageDrawable(context.getDrawable(R.drawable.phone_24));
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+model.getNumber()));
                context.startActivity(callIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactModelList.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder {

        CallRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CallRowBinding.bind(itemView);
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
