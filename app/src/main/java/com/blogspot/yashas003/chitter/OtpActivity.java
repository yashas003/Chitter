package com.blogspot.yashas003.chitter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    AVLoadingIndicatorView loader;
    FirebaseUser user;
    CardView verifyBtn;
    Toolbar toolbar;
    EditText editText;
    String verificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            Toast.makeText(OtpActivity.this, "Verification OTP sent :)", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                verifyCode(code);
            } else {

                Toast.makeText(OtpActivity.this, "Failed to send the OTP\nPlease try any other number :(", Toast.LENGTH_LONG).show();
                Intent callBackIntent = new Intent(OtpActivity.this, PhNoActivity.class);
                startActivity(callBackIntent);
                finish();
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorGray));
        } else if (Build.VERSION.SDK_INT >= 23) {
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        toolbar = findViewById(R.id.otpToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        loader = findViewById(R.id.loader);
        editText = findViewById(R.id.otpCode);

        String Number = getIntent().getStringExtra("PhoneNumber");
        sendVerificationCode(Number);

        verifyBtn = findViewById(R.id.verify);
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = editText.getText().toString().trim();

                if (code.length() == 6) {

                    verifyCode(code);
                } else {

                    Toast.makeText(OtpActivity.this, "That's not the code we sent you :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendVerificationCode(String number) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

    private void verifyCode(String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        String phoneNumber = user.getPhoneNumber();

        if (phoneNumber == null) {

            user.linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        finish();
                        Toast.makeText(OtpActivity.this, "Number linked to your profile :)", Toast.LENGTH_SHORT).show();
                    } else {

                        Toast.makeText(OtpActivity.this, "verification failed!! Invalid Otp:(", Toast.LENGTH_SHORT).show();
                    }
                    loader.setVisibility(View.GONE);
                }
            });
        } else {

            user.updatePhoneNumber(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        finish();
                        Toast.makeText(OtpActivity.this, "Number verified and updated :)", Toast.LENGTH_SHORT).show();
                    } else {

                        Toast.makeText(OtpActivity.this, "verification failed!! Invalid Otp:(", Toast.LENGTH_SHORT).show();
                    }
                    loader.setVisibility(View.GONE);
                }
            });
        }
    }
}