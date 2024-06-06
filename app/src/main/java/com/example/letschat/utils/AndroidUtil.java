package com.example.letschat.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.letschat.common.Common;
import com.example.letschat.model.UserModel;
import com.example.letschat.view.chat.ChatActivity;
import com.example.letschat.view.display.ViewImageActivity;
import com.example.letschat.view.profile.ProfileActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

    public static void transitToViewImage(Context context, CircleImageView circleImageView, String imageUrl, String userName) {
        circleImageView.invalidate();
        Drawable drawable = circleImageView.getDrawable();
        Common.IMAGE_BITMAP = ((BitmapDrawable)drawable.getCurrent()).getBitmap();

        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                        circleImageView, "image");

        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("userName", userName);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        context.startActivity(intent, activityOptionsCompat.toBundle());
    }

    public static void transitToViewImage(Context context, ImageView imageView, String imageUrl) {
        imageView.invalidate();
        Drawable drawable = imageView.getDrawable();
        Common.IMAGE_BITMAP = ((BitmapDrawable)drawable.getCurrent()).getBitmap();

        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                        imageView, "image");

        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        context.startActivity(intent, activityOptionsCompat.toBundle());
    }

    public static String formatDate(long miliseconds) {
        // Convert the miliseconds to a Date
        Date date = new Date(miliseconds);

        // Get the current date and time
        Calendar currentDate = Calendar.getInstance();

        // Convert the timestamp date to Calendar
        Calendar timestampDate = Calendar.getInstance();
        timestampDate.setTime(date);

        // Check if the timestamp is from today
        if (currentDate.get(Calendar.YEAR) == timestampDate.get(Calendar.YEAR) &&
                currentDate.get(Calendar.MONTH) == timestampDate.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) == timestampDate.get(Calendar.DAY_OF_MONTH)) {
            // If the timestamp is from today, format as time
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return timeFormat.format(date);
        } else {
            // If the timestamp is not from today, format as date
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a | dd/MM/yyyy", Locale.getDefault());
            return dateFormat.format(date);
        }
    }

    public static void setLayoutBackgroundFromUri(Context context, ConstraintLayout layout, Uri uri) {
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Drawable drawable = new BitmapDrawable(context.getResources(), resource);
                        layout.setBackground(drawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle any cleanup if necessary
                    }
                });
    }
}
