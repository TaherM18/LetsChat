package com.example.letschat.view.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.letschat.databinding.ActivitySetUserInfoBinding;
import com.example.letschat.view.MainActivity;
import com.example.letschat.R;
import com.example.letschat.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SetUserInfoActivity extends AppCompatActivity {

    private ActivitySetUserInfoBinding binding;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_user_info);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_user_info);
        progressDialog = new ProgressDialog(this);

        // Event Listeners
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(binding.etName.getText().toString())) {
                    Toast.makeText(SetUserInfoActivity.this, "Username required", Toast.LENGTH_SHORT).show();
                }
                else {
                    doUpdate();
                }
            }
        });

        binding.civProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pick image
            }
        });
    }

    private void doUpdate() {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            UserModel user = new UserModel(
                    firebaseUser.getUid(),
                    binding.etName.getText().toString(),
                    firebaseUser.getPhoneNumber(),
                    "");
            firestore.collection("Users").document(firebaseUser.getUid()).set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(SetUserInfoActivity.this, "Update Success", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.w("SetUserInfoActivity", e.getMessage());
                            Toast.makeText(SetUserInfoActivity.this, "Update Failed\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "You need to login first", Toast.LENGTH_SHORT).show();
        }
    }
}