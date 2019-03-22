package com.blogspot.yashas003.chitter.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    TextView forgot_pass;
    TextView login_name;
    ImageView login_image;
    TextView signUp;
    EditText loginEmailText;
    EditText loginPassText;
    CardView loginBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        mAuth = FirebaseAuth.getInstance();
        forgot_pass = findViewById(R.id.forgot_password);
        login_name = findViewById(R.id.login_app_title);
        login_image = findViewById(R.id.login_app_image);
        loginEmailText = findViewById(R.id.login_email);
        loginPassText = findViewById(R.id.login_password);

        signUp = findViewById(R.id.login_reg_btn);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginBtn = findViewById(R.id.start_page_login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(LoginActivity.this);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.progress_bar);
                dialog.show();

                String loginEmail = loginEmailText.getText().toString();
                String loginPassword = loginPassText.getText().toString();

                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)) {
                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        sendToMainActivity();
                                    } else {
                                        String e = Objects.requireNonNull(task.getException()).getMessage();
                                        Toast.makeText(LoginActivity.this, "Error : " + e, Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                            });
                } else if (TextUtils.isEmpty(loginEmail) && TextUtils.isEmpty(loginPassword)) {
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Email & password cannot be empty!!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(loginEmail)) {
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Email cannot be empty!!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(loginPassword)) {
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Password cannot be empty!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
}

