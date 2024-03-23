package com.example.letschat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.model.CallModel;
import com.example.letschat.model.ChatModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerCallsAdapter extends RecyclerView.Adapter<RecyclerCallsAdapter.ViewHolder> {

    private Context context;
    private List<CallModel> callModelList;
    private int lastPosition = -1;

    private enum CallType {
        incoming,
        outgoing
    }

    public RecyclerCallsAdapter(Context context, List<CallModel> callModelList) {
        this.context = context;
        this.callModelList = callModelList;
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
        CallModel callModel = callModelList.get(position);

        Glide.with(context).load(callModel.getProfileUrl()).into(holder.imgProfile);
        holder.txtUsername.setText(callModel.getUsername());
        holder.txtTime.setText(callModel.getDatetime());
        setAnimation(holder.itemView, position);

        if (callModel.getCallType().equals("missed")) {
            holder.imgArrow.setImageDrawable(context.getDrawable(R.drawable.call_received_24));
            holder.imgArrow.getDrawable().setTint(context.getResources().getColor(android.R.color.holo_red_dark));
        }
        else if (callModel.getCallType().equals("incoming")) {
            holder.imgArrow.setImageDrawable(context.getDrawable(R.drawable.subdirectory_arrow_left_24));
            holder.imgArrow.getDrawable().setTint(context.getResources().getColor(android.R.color.holo_green_dark));
        }
        else { // outgoing
            holder.imgArrow.setImageDrawable(context.getDrawable(R.drawable.call_made_24));
            holder.imgArrow.getDrawable().setTint(context.getResources().getColor(android.R.color.holo_green_dark));

        }

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent chatIntent = new Intent(context, ChatActivity.class);
//                chatIntent.putExtra("userId", chatModelList.get(position).getUsername());
//                context.startActivity(chatIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return callModelList.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imgProfile;
        private TextView txtUsername, txtTime, txtContact;
        private ImageButton imgBtnCall;
        private ImageView imgArrow;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.civ_profile);
            txtUsername = itemView.findViewById(R.id.tv_username);
            imgArrow = itemView.findViewById(R.id.iv_arrow);
            imgBtnCall = itemView.findViewById(R.id.ib_call);
            txtTime = itemView.findViewById(R.id.tv_datetime);
//            txtContact = itemView.findViewById(R.id.tv_contact);
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
