package com.example.letschat.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.databinding.StatusRowBinding;
import com.example.letschat.model.StatusImage;
import com.example.letschat.model.StatusModel;
import com.example.letschat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class RecyclerStoryAdapter extends FirestoreRecyclerAdapter<StatusModel, RecyclerStoryAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater layoutInflater;
    private FragmentManager fragmentManager;

    public RecyclerStoryAdapter(@NonNull FirestoreRecyclerOptions<StatusModel> options, Context context, FragmentManager fragmentManager) {
        super(options);
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull StatusModel model) {
        ArrayList<StatusImage> filteredStatusImages = new ArrayList<>();
        // Filter out images older than 24 hours
        long currentTimeInMillis = System.currentTimeMillis();
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

        try {
            for (StatusImage statusImage : model.getStatusImageList()) {
                long imageTimeInMillis = statusImage.getTimestamp().toDate().getTime();
                if (currentTimeInMillis - imageTimeInMillis <= twentyFourHoursInMillis) {
                    filteredStatusImages.add(statusImage);
                }
            }

            if (!filteredStatusImages.isEmpty()) {
                // Status contains image(s) updated within 24 hours
                StatusImage lastStatusImage = filteredStatusImages.get(filteredStatusImages.size() - 1);
                holder.binding.civStatus.setImageURI(Uri.parse(lastStatusImage.getImageUrl()));
                Glide.with(context).load(lastStatusImage.getImageUrl()).into(holder.binding.civStatus);

                holder.binding.circularStatusView.setPortionsCount(filteredStatusImages.size());

                holder.binding.tvUsername.setText( model.getUserName() );

                holder.binding.tvDatetime.setText( FirebaseUtil.formatTimestamp(model.getLastUpdated()) );

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<MyStory> myStories = new ArrayList<>();
                        for (StatusImage statusImage : filteredStatusImages) {
                            myStories.add(new MyStory(statusImage.getImageUrl()));
                        }

                        new StoryView.Builder(fragmentManager)
                                .setStoriesList(myStories) // Required
                                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                .setTitleText(model.getUserName()) // Default is Hidden
                                .setSubtitleText("") // Default is Hidden
                                .setTitleLogoUrl(model.getProfileImage()) // Default is Hidden
                                .setStoryClickListeners(new StoryClickListeners() {
                                    @Override
                                    public void onDescriptionClickListener(int position) {
                                        //your action
                                    }

                                    @Override
                                    public void onTitleIconClickListener(int position) {
                                        //your action
                                    }
                                }) // Optional Listeners
                                .build() // Must be called before calling show method
                                .show();
                    }
                });
            }
            else {
                // Status does not contain any image updated within 24 hours
            }

        }
        catch (Exception e) {

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.status_row, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        StatusRowBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = StatusRowBinding.bind(itemView);
        }
    }
}
