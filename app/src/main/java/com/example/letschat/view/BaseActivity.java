package com.example.letschat.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {

    protected FirebaseFirestore firestore;
    private Date appStartTime;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener accelerometerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize FirebaseFirestore instance
        firestore = FirebaseFirestore.getInstance();

//        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        accelerometerListener = new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                float x = event.values[0];
//                float y = event.values[1];
//                float z = event.values[2];
//
//                // Detect shake
//                float shakeThreshold = 12.0f;
//                float gX = x / SensorManager.GRAVITY_EARTH;
//                float gY = y / SensorManager.GRAVITY_EARTH;
//                float gZ = z / SensorManager.GRAVITY_EARTH;
//                float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);
//
//                if (gForce > shakeThreshold) {
//                    // TODO: inspect its use
//                    onShakeDetected();
//                }
//            }
//
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            }
//        };

    }

    private void onShakeDetected() {
        // Refresh chat list or send a distress signal
        Toast.makeText(this, "Shake detected! Refreshing chat list...", Toast.LENGTH_SHORT).show();
        // Add the functionality you want to trigger on shake
    }


    private void logAppUsage(boolean isAppStart) {
        // Log app usage to Firestore
        Map<String, Object> usageData = new HashMap<>();
        usageData.put("userId", FirebaseUtil.currentUserId());
        usageData.put("timestamp", FieldValue.serverTimestamp());
        usageData.put("isAppStart", isAppStart);
        usageData.put("startTime", appStartTime.getTime());

        firestore.collection("app_usage")
                .add(usageData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("AppUsage", "App usage logged successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("AppUsage", "Error logging app usage:", e);
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        appStartTime = new Date();
        logAppUsage(true);
        FirebaseUtil.currentUserDocument().update("online", true);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        sensorManager.unregisterListener(accelerometerListener);
//    }

    @Override
    protected void onStop() {
        super.onStop();
        logAppUsage(false);
        FirebaseUtil.currentUserDocument().update("online", false);
    }
}