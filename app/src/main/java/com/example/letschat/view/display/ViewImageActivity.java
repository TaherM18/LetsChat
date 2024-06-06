package com.example.letschat.view.display;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.common.Common;
import com.example.letschat.databinding.ActivityViewImageBinding;
import com.example.letschat.utils.FileUtil;
import com.example.letschat.view.profile.ProfileActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ViewImageActivity extends AppCompatActivity {

    private ActivityViewImageBinding binding;
    private String userName = "", imageName = "";
    private Uri imageUri = null;
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_image);

        // Set Custom ActionBar
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getStringExtra("userName") != null) {
            userName = getIntent().getStringExtra("userName");
            getSupportActionBar().setTitle(userName);
        }
        else {
            getSupportActionBar().setTitle("Image");
        }

        if ( getIntent().getStringExtra("imageUrl") != null ) {
            imageName = FileUtil.getFileName(ViewImageActivity.this,
                    Uri.parse(getIntent().getStringExtra("imageUrl")) );
        }
        else {
            // imageUrl is null
        }

        binding.zoomageView.setImageBitmap(Common.IMAGE_BITMAP);
        binding.zoomageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    // FUNCTIONS ===================================================================================

    private Uri saveImage() {
        requestPermissionsIfNecessary();

        ContentValues contentValues = new ContentValues();

        if ( userName.isEmpty() ) {
            // image is from chat media
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LetsChat Images");
        }
        else {
            // image is a profile photo
            String[] strArray = imageName.split("\\.");
            imageName = userName + "." + strArray[strArray.length-1];
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LetsChat Profile Photos");
        }

        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (imageUri != null) {
            try {
                Bitmap bmp = Common.IMAGE_BITMAP;
                OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
                if (outputStream != null) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                    FileUtil.scanFile(ViewImageActivity.this, imageUri);
                    Toast.makeText(this, "Image Downloaded", Toast.LENGTH_SHORT).show();

                    return imageUri;
                }
            } catch (IOException e) {
                Log.e("ViewImageActivity", "Error saving image:\n" + e.getMessage());
            }
        } else {
            Log.e("ViewImageActivity", "Error creating MediaStore entry");
        }
        return null;
    }

    private void shareImage(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a new intent with the action to send
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // Set the type to image
        shareIntent.setType("image/*");
        // Add the URI of the image to the intent
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        // Grant temporary read permission to the content URI
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Start the activity to share the image
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    private void requestPermissionsIfNecessary() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
        }
    }


    // OVERRIDES ===================================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_image_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedOptionId = item.getItemId();

        if (selectedOptionId == R.id.menu_share) {
            // Share image
            shareImage( saveImage() );
            return true;
        }
        else if (selectedOptionId == R.id.menu_download) {
            // Download image
            saveImage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // Permissions granted, proceed with file saving
            saveImage();
        }
    }
}