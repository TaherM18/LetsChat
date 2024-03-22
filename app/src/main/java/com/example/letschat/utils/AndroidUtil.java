package com.example.letschat.utils;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

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
}