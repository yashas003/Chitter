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

public class SignUpActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    TextView login;
    ImageView signUp_image;
    EditText signUpEmailText;
    EditText signUpPassText;
    CardView signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        mAuth = FirebaseAuth.getInstance();
        signUp_image = findViewById(R.id.signUp_app_image);
        signUpEmailText = findViewById(R.id.signUp_email);
        signUpPassText = findViewById(R.id.signUp_password);

        login = findViewById(R.id.reg_login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        signUpBtn = findViewById(R.id.signUp_btn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(SignUpActivity.this);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.progress_bar);
                dialog.show();

                String signUpEmail = signUpEmailText.getText().toString();
                String signUpPass = signUpPassText.getText().toString();

                if (!TextUtils.isEmpty(signUpEmail) && !TextUtils.isEmpty(signUpPass)) {

                    mAuth.createUserWithEmailAndPassword(signUpEmail, signUpPass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        Intent settingsIntent = new Intent(SignUpActivity.this, SetupActivity.class);
                                        startActivity(settingsIntent);
                                        finish();
                                    } else {
                                        String e = Objects.requireNonNull(task.getException()).getMessage();
                                        Toast.makeText(SignUpActivity.this, "Error : " + e, Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                            });

                } else if (TextUtils.isEmpty(signUpEmail) && TextUtils.isEmpty(signUpPass)) {
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Fields cannot be empty!!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(signUpEmail)) {
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Email cannot be empty!!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(signUpPass)) {
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Password cannot be empty!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent signInIntent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(signInIntent);
        finish();
    }
}
