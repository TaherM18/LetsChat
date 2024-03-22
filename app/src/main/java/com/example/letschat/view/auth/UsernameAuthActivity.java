package com.example.letschat.view.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.example.letschat.view.MainActivity;
import com.example.letschat.R;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class UsernameAuthActivity extends AppCompatActivity {

    private ActivityUsernameAuthBinding binding;
    private String phoneNumber;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_username_auth);

        phoneNumber = getIntent().getExtras().getString("phone");
        getUsername();
    }

    private void setUsername() {
        String userName = binding.edtUsername.getText().toString();
        if (userName.isEmpty() || userName.length() < 3) {
            binding.edtUsername.setError("Username should have atleast 3 characters.");
            return;
        }

        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);
        
        if (userModel != null) {
            userModel.setUserName(userName);
        }
        else {
            userModel = new UserModel(userName,phoneNumber, Timestamp.now());
        }
        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {

                }
            }
        });
    }

    private void getUsername() {
        AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                AndroidUtil.setInProgress(binding.progressBar, binding.btnGo, false);
                if (task.isSuccessful()) {
                    userModel = task.getResult().toObject(UserModel.class);
                    if (userModel != null) {
                        binding.edtUsername.setText(userModel.getUserName());
                    }
                }
            }
        });
    }
}