package com.example.letschat.view.startup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.letschat.R;
import com.example.letschat.view.auth.PhoneAuthActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnAgree;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnAgree = findViewById(R.id.btn_agree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(WelcomeActivity.this, PhoneAuthActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}