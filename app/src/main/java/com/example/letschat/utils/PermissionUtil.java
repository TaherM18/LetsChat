package com.example.letschat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    // Function to request all declared permissions
    public static void requestAllDeclaredPermissions(Activity activity, int requestCode) {
        // Get all declared permissions from the manifest
        String[] allPermissions = getAllPermissions(activity);

        // Check which permissions are not granted
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : allPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // Request all necessary permissions
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toArray(new String[0]), requestCode);
        }
    }

    // Function to get all permissions declared in the manifest
    private static String[] getAllPermissions(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS)
                    .requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}
