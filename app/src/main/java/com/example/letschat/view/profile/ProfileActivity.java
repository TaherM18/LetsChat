package com.example.letschat.view.profile;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.letschat.R;
import com.example.letschat.common.Common;
import com.example.letschat.databinding.ActivityProfileBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.display.ViewImageActivity;
import com.example.letschat.view.startup.SplashActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding = null;
    private BottomSheetDialog bottomSheetDialog = null;
    private int IMAGE_GALLERY_REQUEST = 111;
    private UserModel userModel = null;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialization
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bottomSheetDialog = new BottomSheetDialog(this);

        // Setup custom Actionbar
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boolean isEdit = getIntent().getBooleanExtra("edit", false);
        if (isEdit) {
            launchImagePicker();
        }

        // Get user data from firestore
        getUserData();

        // Initialize the activity result launcher
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                // Handle the selected image URI
                                uploadToFirebaseStorage(data.getData());
                            }
                        }
                    }
                });


        // fabCamera onClick Event Listener
        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fabCameraView) {
                launchImagePicker();

//                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick, null);
//
//                // cardGallery onClick Event Listener
//                view.findViewById(R.id.card_gallery).setOnClickListener((View v) -> {
//                    // openGallery();
//                    // launchGallery();
//                    bottomSheetDialog.dismiss();
//                });
//
//                // cardCamera onClick Event Listener
//                view.findViewById(R.id.card_camera).setOnClickListener((View v) -> {
//                    openCamera();
//                    bottomSheetDialog.dismiss();
//                });
//
//                // Set view for BottomSheetDialog and show it
//                bottomSheetDialog.setContentView(view);
//                bottomSheetDialog.show();
//
//                bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        dialog.dismiss();
//                    }
//                });
            }
        });


        // TextInput Name Event Listener
        binding.edtName.setOnClickListener(new View.OnClickListener() {
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
                Common.IMAGE_BITMAP = ((BitmapDrawable) drawable.getCurrent()).getBitmap();
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
        binding.edtBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditBioBottomSheetDialog();
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
                    binding.edtName.setText(userModel.getUserName());
                    binding.edtBio.setText(userModel.getBio());
                    binding.edtPhone.setText(userModel.getPhone());
                    Glide.with(getApplicationContext()).load(userModel.getProfileImage()).into(binding.civProfile);
                } else {
                    AndroidUtil.showToast(getApplicationContext(), "userModel is empty");
                }
            }
        });
    }

    private void openGallery() {
        // No use as startActivityForResult is deprecated
        Intent iGallery = new Intent();
        iGallery.setType("image/*");
        iGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(iGallery, "Select Image"), IMAGE_GALLERY_REQUEST);
    }

    private void launchImagePicker() {
        ImagePicker.with(ProfileActivity.this).cropSquare().compress(512)
                .maxResultSize(512, 512)
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        pickImageLauncher.launch(intent);
                        return null;
                    }
                });
    }


    private void uploadToFirebaseStorage(Uri imgUri) {
        if (imgUri == null) {
            AndroidUtil.showToast(getApplicationContext(), "imageUri is null");
            return;
        }
        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, true);

        // Create a storage reference to "profile_images" folder
        FirebaseUtil.getProfileImgStoragereference().putFile(imgUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Image Upload Completed  Successfully
                            AndroidUtil.showToast(ProfileActivity.this, "Image uploaded");
                            FirebaseUtil.getProfileImgStoragereference().getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                // Store the download URL in a Firestore document
                                                updateProfile( task.getResult() );
                                            }
                                            else {
                                                AndroidUtil.showToast(ProfileActivity.this,
                                                        "Failed to get download url");
                                            }
                                        }
                                    });
                        }
                        else {
                            AndroidUtil.showToast(ProfileActivity.this, "Failed to upload image");
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
                            binding.edtName.setText(newName);
                        } else {
                            AndroidUtil.showToast(ProfileActivity.this, "Failed to Update Name:\n" + task.getException().getMessage());
                        }
                    }
                });
    }

    private void updateBio(String newBio) {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, true);
        FirebaseUtil.currentUserDocument().update("bio", newBio)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, false);
                        if (task.isSuccessful()) {
                            AndroidUtil.showToast(ProfileActivity.this, "Bio Updated");
                            binding.edtBio.setText(newBio);
                        } else {
                            AndroidUtil.showToast(ProfileActivity.this, "Failed to Update Bio:\n" + task.getException().getMessage());
                        }
                    }
                });
    }

    private void updateProfile(Uri imageUri) {
        FirebaseUtil.currentUserDocument().update("profileImage", imageUri)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, false);

                        if (task.isSuccessful()) {
                            AndroidUtil.showToast(ProfileActivity.this, "Profile Image Updated");

                            AndroidUtil.setImageWithGlide(getApplicationContext(), imageUri, binding.civProfile);
                        } else {
                            AndroidUtil.showToast(ProfileActivity.this,
                                    "Failed to Update Name:\n" + task.getException().getMessage());
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
                        // Signout from Firebase Auth
                        FirebaseAuth.getInstance().signOut();

                        dialog.cancel();
                        Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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
        editTextName.setText(binding.edtName.getText().toString());

        // Save Button onClick Event Listener
        view.findViewById(R.id.btn_save).setOnClickListener((View v) -> {
            if (editTextName.getText().toString().isEmpty()) {
                editTextName.setError("Name is required");
            } else {
                editTextName.setError(null);
                updateName(editTextName.getText().toString());
                bottomSheetDialog.dismiss();
            }

        });

        // Cancel Button onClick Event Listener
        view.findViewById(R.id.btn_cancel).setOnClickListener((View v) -> {
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

    private void showEditBioBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_bio, null);

        TextInputEditText editTextBio = view.findViewById(R.id.edt_bio);
        editTextBio.setText(binding.edtBio.getText().toString());

        // Save Button onClick Event Listener
        view.findViewById(R.id.btn_save).setOnClickListener((View v) -> {
            if (editTextBio.getText().toString().isEmpty()) {
                editTextBio.setError("Bio is required");
            } else {
                editTextBio.setError(null);
                updateBio(editTextBio.getText().toString());
                bottomSheetDialog.dismiss();
            }

        });

        // Cancel Button onClick Event Listener
        view.findViewById(R.id.btn_cancel).setOnClickListener((View v) -> {
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
}