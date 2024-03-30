package com.example.letschat.view.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.letschat.databinding.ActivityProfileAuthBinding;
import com.example.letschat.view.MainActivity;
import com.example.letschat.R;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.profile.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

public class ProfileAuthActivity extends AppCompatActivity {

    private ActivityProfileAuthBinding binding;
    private String phoneNumber;
    private UserModel userModel = null;
    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);

        phoneNumber = getIntent().getExtras().getString("phone");
        getUserData();

        binding.civProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        binding.btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = binding.edtUsername.getText().toString();
                if (userName.isEmpty() || userName.length() < 3) {
                    binding.edtUsername.setError("Username should have atleast 3 characters.");
                    return;
                }
                uploadToFirebaseStorage();
            }
        });
    }

    private void setUserData(String imgUrl) {
        String userId = FirebaseAuth.getInstance().getUid();
        String userName = binding.edtUsername.getText().toString();
        String profileImageUrl = imgUrl;
        Timestamp timeStamp = Timestamp.now();

        UserModel userModel = new UserModel(userId, userName, phoneNumber, profileImageUrl, timeStamp);

        FirebaseUtil.currentUserDocument()
                .set(userModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            AndroidUtil.showToast(getApplicationContext(), "Failed to set user data");
                        }
                    }
                });
    }

    private void openGallery() {
        Intent iGallery = new Intent();
        iGallery.setType("image/*");
        iGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(iGallery, "Select Image"), IMAGE_GALLERY_REQUEST);
    }

    private void openCamera() {

    }

    private void uploadToFirebaseStorage() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);

        if (imageUri == null) {
            // profile image not picked
            setUserData("");
            return;
        }

        // Create a storage reference to "profile_images" folder
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_images")
                .child(FirebaseUtil.currentUserId());

        storageRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // Image Upload Completed  Successfully
                    AndroidUtil.showToast(getApplicationContext(), "Image uploaded");
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // set selected uri to firebase uri
                            imageUri = uri;
                            // Store the download URL in a Firestore document
                            setUserData(uri.toString());
                        }
                    });
                }
            }
        });
    }

    private void getUserData() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);
        FirebaseUtil.currentUserDocument().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);

                if (task.isSuccessful()) {
                    userModel = task.getResult().toObject(UserModel.class);

                    if (userModel != null) {
                        // current user is present in firestore
                        binding.edtUsername.setText(userModel.getUserName());

                        if ( ! userModel.getProfileImage().isEmpty() ) {
                            Glide.with(ProfileAuthActivity.this).load(userModel.getProfileImage()).into(binding.civProfile);
                            imageUri = Uri.parse(userModel.getProfileImage());
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            imageUri = data.getData();
            binding.civProfile.setImageURI(imageUri);
        }
    }
}