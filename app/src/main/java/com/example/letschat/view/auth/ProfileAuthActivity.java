package com.example.letschat.view.auth;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.letschat.R;
import com.example.letschat.databinding.ActivityProfileAuthBinding;
import com.example.letschat.model.MessageModel;
import com.example.letschat.utils.FileUtil;
import com.example.letschat.view.MainActivity;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.chat.ChatActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileAuthActivity extends AppCompatActivity {

    private ActivityProfileAuthBinding binding;
    private ActivityResultLauncher<Intent> galleryLauncher, cameraLauncher;
    private BottomSheetDialog bottomSheetDialog = null;
    private String phoneNumber = "", profileImageUrl = "";
    private UserModel userModel = null;
    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomSheetDialog = new BottomSheetDialog(this);
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);

        phoneNumber = getIntent().getExtras().getString("phone");

        getUserData();

        // Initialize the activity result launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                // Handle the selected image URI
                                selectedImageUri = data.getData();
                                Glide.with(ProfileAuthActivity.this)
                                        .load(selectedImageUri)
                                        .apply(RequestOptions.centerCropTransform())
                                        .into(binding.civProfile);
                            }
                            else {
                                Toast.makeText(ProfileAuthActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Initialize the ActivityResultLauncher for camera capture
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                // Handle the selected image URI
                                selectedImageUri = data.getData();
                                Glide.with(ProfileAuthActivity.this)
                                        .load(selectedImageUri)
                                        .apply(RequestOptions.centerCropTransform())
                                        .into(binding.civProfile);
                            }
                            else {
                                Toast.makeText(ProfileAuthActivity.this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // EVENT LISTENERS =========================================================================

        binding.civProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetPick();
            }
        });

        binding.btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = binding.edtUsername.getText().toString();
                if (userName.length() < 3) {
                    binding.edtUsername.setError("Username should have atleast 3 characters.");
                    return;
                }
                binding.edtUsername.setError(null);
                uploadToFirebaseStorage();
            }
        });
    }

    // FUNCTIONS ===================================================================================

    private void setUserData(String imgUrl) {

        FirebaseUtil.currentUserDocument().get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if ( task.isSuccessful() ) {
                            String userId = FirebaseAuth.getInstance().getUid();
                            String userName = binding.edtUsername.getText().toString();
                            UserModel userModel = task.getResult().toObject(UserModel.class);

                            if (userModel == null) {
                                // User doesn't exits
                                userModel = new UserModel(userId, userName, phoneNumber, imgUrl);
                            }
                            else {
                                userModel.setUserName(userName);
                                userModel.setPhone(phoneNumber);
                                userModel.setProfileImage(imgUrl);
                                userModel.setUpdatedTimestamp(Timestamp.now());
                            }
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
                    }
                });



    }

    private void openGallery() {
        Intent iGallery = new Intent();
        iGallery.setType("image/*");
        iGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(iGallery, "Select Image"), IMAGE_GALLERY_REQUEST);
    }

    private void launchGallery() {
        ImagePicker.with(this)
                .cropSquare()
                .galleryOnly()	//User can only select image from Gallery
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        galleryLauncher.launch(intent);
                        return null;
                    }
                });
    }

    private void launchCamera() {
        ImagePicker.with(this)
                .cropSquare()
                .cameraOnly()	//User can only capture image using Camera
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        cameraLauncher.launch(intent);
                        return null;
                    }
                });
    }

    private void openBottomSheetPick() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick, null);

        // cardGallery onClick Event Listener
        view.findViewById(R.id.card_gallery).setOnClickListener((View v) -> {
            launchGallery();
            bottomSheetDialog.dismiss();
        });

        // cardCamera onClick Event Listener
        view.findViewById(R.id.card_camera).setOnClickListener((View v) -> {
            launchCamera();
            bottomSheetDialog.dismiss();
        });

        // Set view for BottomSheetDialog and show it
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
    }

    private void uploadToFirebaseStorage() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);

        if (selectedImageUri == null && profileImageUrl.isEmpty()) {
            // profile image not picked & not set in firebase
            setUserData("");
            return;
        } else if (selectedImageUri == null && !profileImageUrl.isEmpty()) {
            // profile image not picked & is set in firebase
            setUserData(profileImageUrl);
            return;
        } else if (selectedImageUri != null) {
            // Create a storage reference to "profile_images" folder
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("profile_images")
                    .child(FirebaseUtil.currentUserId());

            storageRef.putFile(selectedImageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                // Image Upload Completed  Successfully
                                AndroidUtil.showToast(getApplicationContext(), "Image uploaded");
                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // Store the download URL in a Firestore document
                                        setUserData(uri.toString());
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private void getUserData() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);
        FirebaseUtil.currentUserDocument().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);

                        if (task.isSuccessful()) {
                            userModel = task.getResult().toObject(UserModel.class);

                            if (userModel != null) {
                                // current user is present in firestore

                                binding.edtUsername.setText(userModel.getUserName());

                                if (!userModel.getProfileImage().isEmpty()) {
                                    // profile image is set
                                    Glide.with(getApplicationContext())
                                            .load(userModel.getProfileImage())
                                            .into(binding.civProfile);
                                    profileImageUrl = userModel.getProfileImage();
                                }
                            }
                        }
                    }
                });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK
//                && data != null && data.getData() != null) {
//
//            selectedImageUri = data.getData();
//            binding.civProfile.setImageURI(selectedImageUri);
//        }
//    }
}