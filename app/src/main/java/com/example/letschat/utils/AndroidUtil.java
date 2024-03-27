package com.example.letschat.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.letschat.common.Common;
import com.example.letschat.model.UserModel;
import com.example.letschat.view.display.ViewImageActivity;
import com.example.letschat.view.profile.ProfileActivity;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class AndroidUtil {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void setInProgress(ProgressBar progressBar, MaterialButton button, boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }

    public static void setProgressBar(ProgressBar progressBar, boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    public static String getFileExtension(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public static void passUserModelAsIntent(Intent intent, UserModel userModel) {
        intent.putExtra("userId", userModel.getUserId());
        intent.putExtra("userName", userModel.getUserName());
        intent.putExtra("phone", userModel.getPhone());
        intent.putExtra("profileImage", userModel.getProfileImage());
        intent.putExtra("fcmToken", userModel.getFcmToken());
    }

    public static UserModel getUserModelFromIntent(Intent intent) {
        UserModel userModel = new UserModel();
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setUserName(intent.getStringExtra("userName"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setProfileImage(intent.getStringExtra("profileImage"));
        userModel.setFcmToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

    public static void setImageWithGlide(Context context, Uri imageUri, CircleImageView circleImageView) {
        Glide.with(context).load(imageUri).apply(RequestOptions.centerCropTransform()).into(circleImageView);
    }

    public static void transitToViewImage(Context context, CircleImageView circleImageView) {
        circleImageView.invalidate();
        Drawable drawable = circleImageView.getDrawable();
        Common.IMAGE_BITMAP = ((BitmapDrawable)drawable.getCurrent()).getBitmap();
        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                        circleImageView, "image");

        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        context.startActivity(intent, activityOptionsCompat.toBundle());
    }
}
