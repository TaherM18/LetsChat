package com.example.wechat.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.R;
import com.example.wechat.model.ContactModel;
import java.util.ArrayList;

public class RecyclerContactsAdapter extends RecyclerView.Adapter<RecyclerContactsAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ContactModel> contactModelArrayList;

//    Constructor
    public RecyclerContactsAdapter(Context context, ArrayList<ContactModel> contactModelArrayList) {
        this.context = context;
        this.contactModelArrayList = contactModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.contacts_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ContactModel model = contactModelArrayList.get(position);
        holder.textViewName.setText(model.getName());
        holder.textViewNumber.setText(model.getNumber());
    }

    @Override
    public int getItemCount() {
        return contactModelArrayList.size();
    }

//    Inner Class for View Holder
    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnCreateContextMenuListener {
        private TextView textViewName, textViewNumber;
        ConstraintLayout lyt_constraint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select an Option");
            menu.add(this.getAdapterPosition(), 121, 0, "Call");
            menu.add(this.getAdapterPosition(), 122, 1, "Message");
        }
    }
}
