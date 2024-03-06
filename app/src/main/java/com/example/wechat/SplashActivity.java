package com.example.wechat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SplashActivity extends AppCompatActivity {

    private ImageView imgLogo;
    private TextView txtFrom, txtCompany;
    private ProgressBar progressBar;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        Hooks
        imgLogo = findViewById(R.id.imgLogo);
        txtFrom = findViewById(R.id.txtFrom);
        txtCompany = findViewById(R.id.txtCompany);
        progressBar = findViewById(R.id.progress);

//        Animation
        Animation fadeIn = AnimationUtils.loadAnimation(SplashActivity.this, android.R.anim.fade_in);
        imgLogo.setAnimation(fadeIn);
        txtFrom.setAnimation(fadeIn);
        txtCompany.setAnimation(fadeIn);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int jumpTime = 0;
                try {
                    while (jumpTime < progressBar.getMax()) {
                        Thread.sleep(100);
                        jumpTime += 10;
                        progressBar.setProgress(jumpTime);
                    }
                    Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
                    startActivity(intent);
                    finish();
                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Error", e.getMessage());
                }
            }
        });

        thread.start();
    }

}