package com.example.letschat.view.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.letschat.R;
import com.example.letschat.databinding.ActivityPhoneAuthBinding;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private ActivityPhoneAuthBinding binding;
    private static final String TAG = "PhoneAuthActivity";
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_auth);

        binding.progressBar.setVisibility(View.GONE);

        binding.countryCodePicker.registerCarrierNumberEditText(binding.edtPhone);

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.countryCodePicker.isValidFullNumber()) {
                    binding.edtPhone.setError("Invalid phone number");
                    return;
                }

//                FirebaseUtil.allUserCollectionReference().get()
//                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    QuerySnapshot querySnapshot = task.getResult();
//
//                                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
//                                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
//
//                                        if (binding.edtPhone.getText().toString().equals(userModel.getPhone())) {
//                                            // what to do now
//                                        }
//                                    }
//                                }
//                            }
//                        });

                Intent intent = new Intent(PhoneAuthActivity.this, OtpAuthActivity.class);
                intent.putExtra("phone", binding.countryCodePicker.getFullNumberWithPlus());
                startActivity(intent);
                finish();
            }
        });
    }
}