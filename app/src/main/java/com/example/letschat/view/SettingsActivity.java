package com.example.letschat.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.databinding.ActivitySettingsBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.profile.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialization
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        setSupportActionBar(binding.materialToolbar);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        AndroidUtil.setProgressBar(binding.progressBar, true);

        if (firebaseUser != null) {
            // User is signed in
            getUserData();
        }

        // Event Listeners
        binding.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });
    }

    private void getUserData() {
        firestore.collection(FirebaseUtil.collectionName).document(FirebaseUtil.currentUserId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        AndroidUtil.setProgressBar(binding.progressBar, false);
                        if (task.isSuccessful()) {
                            userModel = task.getResult().toObject(UserModel.class);
                            if (userModel != null) {
                                binding.tvUsername.setText(userModel.getUserName());
                                binding.tvBio.setText("TODO");
                                Glide.with(SettingsActivity.this)
                                        .load(userModel.getProfileImage()).into(binding.circleImageView);
                            } else {
                                Toast.makeText(SettingsActivity.this, "userModel is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            AndroidUtil.showToast(getApplicationContext(),
                                    "Failure: " + task.getException().getMessage());
                        }
                    }
                });
    }
}