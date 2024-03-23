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

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private BottomSheetDialog bottomSheetDialog;
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private int IMAGE_GALLERY_REQUEST = 111;
    private UserModel currentUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialization
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        bottomSheetDialog = new BottomSheetDialog(this);
        progressDialog = new ProgressDialog(this);

        // Setup custom Actionbar
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        // Get user data from firestore if firebase user is available
        if (firebaseUser != null) {
            getUserInfo();
        }

        // fabCamera onClick Event Listener
        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fabCameraView) {
                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick, null);

                // cardGallery onClick Event Listener
                view.findViewById(R.id.card_gallery).setOnClickListener((View v) -> {
                    openGallery();
                    bottomSheetDialog.dismiss();
                });

                // cardCamera onClick Event Listener
                view.findViewById(R.id.card_camera).setOnClickListener((View v) -> {
                    openCamera();
                    bottomSheetDialog.dismiss();
                });

                // Set view for BottomSheetDialog and show it
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        bottomSheetDialog = null;
                    }
                });
            }
        });

        // TextInput Name Event Listener
        binding.ilName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View textInputLayoutView) {
                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_name, null);

                TextInputEditText editTextName = view.findViewById(R.id.et_name);

                // Save Button onClick Event Listener
                view.findViewById(R.id.btn_save).setOnClickListener((View v) -> {
                    if (!TextUtils.isEmpty(editTextName.getText().toString())) {
                        editTextName.setError(null);
                        updateName(editTextName.getText().toString());
                        bottomSheetDialog.dismiss();
                    }
                    else {
                        editTextName.setError("Name is required");
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
                        bottomSheetDialog = null;
                    }
                });
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

    private void getUserInfo() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                AndroidUtil.setInProgress(binding.progressBar, binding.btnSignOut, false);

                currentUserModel = task.getResult().toObject(UserModel.class);
                binding.etName.setText(currentUserModel.getUserName());
//                binding.etBio.setText(currentUserModel.getBio());
//                binding.etPhone.setText(currentUserModel.getUserPhone());
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

            uploadToFirebase(imageUri);

//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                binding.civProfile.setImageBitmap(bitmap);
//            } catch (Exception e) {
//                Toast.makeText(this, "OnActivityResult: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadToFirebase(Uri imageUri) {
        if (imageUri != null) {
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StorageReference riversRef = FirebaseStorage.getInstance().getReference()
                    .child("profileImages/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            riversRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();

                            final String downloadUriString = String.valueOf(downloadUri);

                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("imageProfile", downloadUriString);

                            progressDialog.dismiss();

                            firestore.collection("Users").document(firebaseUser.getUid()).update(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(ProfileActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                                            getUserInfo();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Upload Failure: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateName(String newName) {
        firestore.collection("Users").document(firebaseUser.getUid()).update("userName", newName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProfileActivity.this, "Name Update Success", Toast.LENGTH_SHORT).show();
                        getUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Name Update Failure:\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDialogSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
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
}