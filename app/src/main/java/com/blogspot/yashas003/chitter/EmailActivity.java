package com.blogspot.yashas003.chitter;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import java.util.Objects;

public class EmailActivity extends AppCompatActivity {
    FirebaseUser user;
    FirebaseAuth mAuth;
    TextView confirmBtn;
    TextView closeBtn;
    EditText dialogPassword;
    EditText newEmail;
    CardView saveEmailBtn;
    Toolbar toolbar;
    String enteredEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        toolbar = findViewById(R.id.edit_email_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        newEmail = findViewById(R.id.new_email);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {

            final String email = user.getEmail();
            newEmail.setText(email);
        }

        saveEmailBtn = findViewById(R.id.save_email);
        saveEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enteredEmail = newEmail.getText().toString();

                if (!TextUtils.isEmpty(enteredEmail)) {

                    final Dialog confirmPass = new Dialog(EmailActivity.this);
                    confirmPass.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                    confirmPass.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    confirmPass.setContentView(R.layout.confirm_password);
                    confirmPass.show();

                    dialogPassword = confirmPass.findViewById(R.id.new_password);

                    confirmBtn = confirmPass.findViewById(R.id.confirm_button);
                    confirmBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String Password = dialogPassword.getText().toString();

                            if (TextUtils.isEmpty(Password)) {
                                confirmPass.dismiss();
                                Toast.makeText(EmailActivity.this, "Password cannot be empty :(", Toast.LENGTH_SHORT).show();
                            } else {
                                confirmPass.dismiss();
                                updateNewEmail(Password);
                            }
                        }
                    });

                    closeBtn = confirmPass.findViewById(R.id.close_button);
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmPass.dismiss();
                        }
                    });

                } else {

                    Toast.makeText(EmailActivity.this, "E-mail address cannot be empty :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateNewEmail(String Password) {

        final Dialog dialog = new Dialog(EmailActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_bar);
        dialog.show();

        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), Password);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mAuth.fetchProvidersForEmail(enteredEmail)
                                    .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                                            if (task.isSuccessful()) {

                                                try {

                                                    if (task.getResult().getProviders().size() == 1) {

                                                        dialog.dismiss();
                                                        Toast.makeText(EmailActivity.this, "Email you've entered is already in use :(", Toast.LENGTH_SHORT).show();
                                                    } else {

                                                        user.updateEmail(enteredEmail)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {
                                                                            Toast.makeText(EmailActivity.this, "Email successfully updated :)", Toast.LENGTH_SHORT).show();
                                                                            dialog.dismiss();
                                                                            finish();
                                                                        } else {

                                                                            dialog.dismiss();
                                                                            String error1 = task.getException().getMessage();
                                                                            Toast.makeText(EmailActivity.this, "Error : " + error1, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                } catch (NullPointerException e) {
                                                    Toast.makeText(EmailActivity.this, "Error : " + e + ":(", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {

                                                dialog.dismiss();
                                                String error2 = task.getException().getMessage();
                                                Toast.makeText(EmailActivity.this, "Error : " + error2, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            dialog.dismiss();
                            Toast.makeText(EmailActivity.this, "Users reauthentication failed :(", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (user != null) {

            String email = user.getEmail();
            newEmail.setText(email);
        }
    }
}
