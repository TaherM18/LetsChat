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
    private UserModel userModel;
    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);

        phoneNumber = getIntent().getExtras().getString("phone");

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

    private void setUserData() {
        String userId = FirebaseAuth.getInstance().getUid();
        String userName = binding.edtUsername.getText().toString();
        String imageUrl = imageUri.toString();
        Timestamp timeStamp = Timestamp.now();

        UserModel userModel = new UserModel(userId, userName, phoneNumber, imageUrl, timeStamp);

        FirebaseDatabase.getInstance().getReference()
                .child(FirebaseUtil.collectionName)
                .child(FirebaseUtil.currentUserId())
                .setValue(userModel)
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

//        FirebaseFirestore.getInstance().collection(FirebaseUtil.collectionName)
//                .add(userModel)
//                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentReference> task) {
//                        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);
//
//                        if (task.isSuccessful()) {
//                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                        } else {
//                            AndroidUtil.showToast(getApplicationContext(), "Failed to set user data");
//                        }
//                    }
//                });
    }

    private void setUsername() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);
        String username = binding.edtUsername.getText().toString();
        if (username.isEmpty()) {
            binding.edtUsername.setError("Username is required");
            return;
        }
        userModel = new UserModel();
        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        if (imageUri == null) {
            //Drawable personPlaceholder = getResources().getDrawable(R.drawable.person_placeholder_360x360);
            AndroidUtil.showToast(getApplicationContext(), "imageUri is null");
            return;
        }
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);

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
                            // String imageUrl = uri.toString();
                            imageUri = uri;
                            // Store the download URL in a Firestore document
                            setUserData();
                        }
                    });
                }
            }
        });
    }

    private void getUserData() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);

                if (task.isSuccessful()) {
                    userModel = task.getResult().toObject(UserModel.class);
                    if (userModel != null) {
                        binding.edtUsername.setText(userModel.getUserName());
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