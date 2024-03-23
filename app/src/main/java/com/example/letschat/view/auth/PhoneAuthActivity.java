package com.example.letschat.view.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.letschat.R;
import com.example.letschat.databinding.ActivityPhoneAuthBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;

public class PhoneAuthActivity extends AppCompatActivity {

    private ActivityPhoneAuthBinding binding;
    private static final String TAG = "PhoneAuthActivity";
    private String phoneNumber;
    private Long timeoutSeconds = 60L;
    private String verificationCode;
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

//                if () {
                    // TODO: search for a document containing given phone number
                    //  if it is available then redirect to MainActivity
//                }
                Intent intent = new Intent(PhoneAuthActivity.this, OtpAuthActivity.class);
                intent.putExtra("phone", binding.countryCodePicker.getFullNumberWithPlus());
                startActivity(intent);
                finish();
            }
        });
    }
}