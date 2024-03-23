package com.example.letschat.view.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.letschat.R;
import com.example.letschat.databinding.ActivityOtpAuthBinding;
import com.example.letschat.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OtpAuthActivity extends AppCompatActivity {

    private ActivityOtpAuthBinding binding;
    private String phoneNumber;
    private Long timeoutSeconds = 60L;
    private String verificationCode;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_auth);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp_auth);

        phoneNumber = getIntent().getStringExtra("phone");

//        Map<String, String> data = new HashMap<>();
//        FirebaseFirestore.getInstance().collection("test").add(data);

        sendOtp(phoneNumber, false);

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOtp = binding.pinOTP.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
                signIn(credential);
            }
        });

        binding.btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtp(phoneNumber, true);
            }
        });
    }

    private void sendOtp(String phoneNumber, boolean isResend) {
        setInProgress(true);
        startResendTimer();

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signIn(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        AndroidUtil.showToast(getApplicationContext(), "Verification Failed:\n"+e.getMessage());
                        setInProgress(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationCode = s;
                        resendingToken = forceResendingToken;
                        AndroidUtil.showToast(getApplicationContext(), "OTP sent");
                        setInProgress(false);
                    }
                });

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }
        else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnVerify.setVisibility(View.GONE);
        }
        else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnVerify.setVisibility(View.VISIBLE);
        }
    }

    private void signIn(PhoneAuthCredential phoneAuthCredential) {
        // login and go to next activity
        setInProgress(true);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), ProfileAuthActivity.class);
                    intent.putExtra("phone", phoneNumber);
                    startActivity(intent);
                    finish();
                }
                else {
                    AndroidUtil.showToast(getApplicationContext(), "OTP Verification failed");
                }
            }
        });
    }

    private void startResendTimer() {
        binding.btnResend.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;

                // Post a Runnable to update UI components on the main UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.btnResend.setText(String.format("Resend OTP in %ds", timeoutSeconds));
                    }
                });

                if (timeoutSeconds <= 0) {
                    timeoutSeconds = 60L;
                    timer.cancel();

                    // Post a Runnable to enable the button on the main UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.btnResend.setEnabled(true);
                        }
                    });
                }
            }
        }, 0, 1000);
    }

}