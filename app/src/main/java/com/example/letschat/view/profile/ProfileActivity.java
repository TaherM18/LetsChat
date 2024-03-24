package com.example.letschat.view.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.common.Common;
import com.example.letschat.databinding.ActivityProfileBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.display.ViewImageActivity;
import com.example.letschat.view.startup.SplashActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private BottomSheetDialog profileImgPickerBottomSheetDialog;
    private BottomSheetDialog userNameEditBottomSheetDialog;
    private Uri imageUri;
    private int IMAGE_GALLERY_REQUEST = 111;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialization
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profileImgPickerBottomSheetDialog = new BottomSheetDialog(this);
        userNameEditBottomSheetDialog = new BottomSheetDialog(this);

        // Setup custom Actionbar
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        // Get user data from firestore if firebase user is available
        if (firebaseUser != null) {
            getUserData();
        }


        // fabCamera onClick Event Listener
        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fabCameraView) {
                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick, null);

                // cardGallery onClick Event Listener
                view.findViewById(R.id.card_gallery).setOnClickListener((View v) -> {
                    openGallery();
                    profileImgPickerBottomSheetDialog.dismiss();
                });

                // cardCamera onClick Event Listener
                view.findViewById(R.id.card_camera).setOnClickListener((View v) -> {
                    openCamera();
                    profileImgPickerBottomSheetDialog.dismiss();
                });

                // Set view for BottomSheetDialog and show it
                profileImgPickerBottomSheetDialog.setContentView(view);
                profileImgPickerBottomSheetDialog.show();

                profileImgPickerBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
            }
        });


        // TextInput Name Event Listener
        binding.etName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View textInputLayoutView) {
                showEditNameBottomSheetDialog();
            }
        });

        // Profile Image onClick Event Listener
        binding.civProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.civProfile.invalidate();
                Drawable drawable = binding.civProfile.getDrawable();
                Common.IMAGE_BITMAP = ((BitmapDrawable)drawable.getCurrent()).getBitmap();
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this,
                                binding.civProfile, "image");

                Intent intent = new Intent(ProfileActivity.this, ViewImageActivity.class);
                startActivity(intent, activityOptionsCompat.toBundle());

            }
        });

        binding.btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogSignOut();
            }
        });

        // TextInput Bio Event Listener
        binding.ilBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void getUserData() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, true);
        FirebaseUtil.currentUserDocument().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, false);

                userModel = task.getResult().toObject(UserModel.class);
                if (userModel != null) {
                    binding.etName.setText(userModel.getUserName());
                    binding.etBio.setText(userModel.getBio());
                    binding.etPhone.setText(userModel.getPhone());
                    Glide.with(getApplicationContext()).load(userModel.getProfileImage()).into(binding.civProfile);
                }
                else {
                    AndroidUtil.showToast(getApplicationContext(), "userModel is empty");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            imageUri = data.getData();
            uploadToFirebaseStorage(imageUri);
        }
    }

    private void uploadToFirebaseStorage(Uri imgUri) {
        if (imgUri == null) {
            AndroidUtil.showToast(getApplicationContext(), "imageUri is null");
            return;
        }
        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, true);

        // Create a storage reference to "profile_images" folder
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_images")
                .child(FirebaseUtil.currentUserId());

        storageRef.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // Image Upload Completed  Successfully
                    AndroidUtil.showToast(ProfileActivity.this, "Image uploaded");
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Store the download URL in a Firestore document
                            updateProfile(uri.toString());
                        }
                    });
                }
            }
        });
    }

    private void updateName(String newName) {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, true);
        FirebaseUtil.currentUserDocument().update("userName", newName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, false);
                        if (task.isSuccessful()) {
                            AndroidUtil.showToast(ProfileActivity.this, "Name Updated");
                            // getUserData();    // commented to improve performance
                            binding.etName.setText(newName);
                        }
                        else {
                            AndroidUtil.showToast(ProfileActivity.this, "Failed to Update Name:\n"+task.getException().getMessage());
                        }
                    }
                });
    }

    private void updateProfile(String profileImgUrl) {
        FirebaseUtil.currentUserDocument().update("profileImage", profileImgUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, false);

                        if (task.isSuccessful()) {
                            AndroidUtil.showToast(ProfileActivity.this, "Profile Image Updated");
                            Glide.with(getApplicationContext()).load(profileImgUrl).into(binding.civProfile);
                        }
                        else {
                            AndroidUtil.showToast(ProfileActivity.this, "Failed to Update Name:\n"+task.getException().getMessage());
                        }
                    }
                });
    }

    private void showDialogSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Signout Alert")
                .setMessage("Are your sure you want to signout?")
                .setIcon(R.drawable.warning_24)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        dialog.cancel();
                        startActivity(new Intent(ProfileActivity.this, SplashActivity.class));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEditNameBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_name, null);

        TextInputEditText editTextName = view.findViewById(R.id.et_name);
        editTextName.setText(binding.etName.getText().toString());

        // Save Button onClick Event Listener
        view.findViewById(R.id.btn_save).setOnClickListener((View v) -> {
            if ( editTextName.getText().toString().isEmpty() ) {
                editTextName.setError("Name is required");
            }
            else {
                editTextName.setError(null);
                updateName(editTextName.getText().toString());
                userNameEditBottomSheetDialog.dismiss();
            }

        });

        // Cancel Button onClick Event Listener
        view.findViewById(R.id.btn_cancel).setOnClickListener((View v) -> {
            userNameEditBottomSheetDialog.dismiss();
        });

        // Set view for BottomSheetDialog and show it
        userNameEditBottomSheetDialog.setContentView(view);
        userNameEditBottomSheetDialog.show();

        userNameEditBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
    }
}