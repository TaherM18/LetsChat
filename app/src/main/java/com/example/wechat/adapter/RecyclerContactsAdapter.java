package com.example.wechat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.ChatActivity;
import com.example.wechat.R;
import com.example.wechat.model.ContactModel;
import java.util.ArrayList;

public class RecyclerContactsAdapter extends RecyclerView.Adapter<RecyclerContactsAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ContactModel> contactModelList;

//    Constructor
    public RecyclerContactsAdapter(Context context, ArrayList<ContactModel> contactModelList) {
        this.context = context;
        this.contactModelList = contactModelList;
    }

    private void setAnimation(View view, int position) {
//        if (position > lastPosition) {
//            lastPosition = position;
        Animation slideInLeft = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
//            Animation slideInLeft = AnimationUtils.loadAnimation(context, R.anim.recycler_anim);
        view.startAnimation(slideInLeft);
//        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.contacts_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ContactModel model = contactModelList.get(position);
        holder.textViewName.setText(model.getName());
        holder.textViewNumber.setText(model.getNumber());

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ChatActivity.class);
                context.startActivity(i);
            }
        });

//        holder.constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return false;
//            }
//        });

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return contactModelList.size();
    }




//    Inner Class for View Holder
    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnCreateContextMenuListener {
        private TextView textViewName, textViewNumber;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.txt_contact_name);
            textViewNumber = itemView.findViewById(R.id.txt_contact_number);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select an Option");
            menu.add(this.getAdapterPosition(), 121, 0, "Call");
            menu.add(this.getAdapterPosition(), 122, 1, "Message");
        }
    }
}
