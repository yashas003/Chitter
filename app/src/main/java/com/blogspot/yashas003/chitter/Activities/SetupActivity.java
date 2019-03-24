package com.blogspot.yashas003.chitter.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetupActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    FirebaseAuth firebaseAuth;

    FloatingActionButton imagePicker;
    ImageView setupImage;
    EditText setupName;
    EditText uniqueName;
    CardView setupBtn;
    Dialog dialog;

    boolean isChanged = false;
    Uri mainImageURI = null;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        dialog = new Dialog(SetupActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_bar);
        dialog.show();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.display_name);
        uniqueName = findViewById(R.id.user_name);
        uniqueName.setFilters(new InputFilter[]{
                new InputFilter.AllCaps() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        return String.valueOf(source).toLowerCase().replace(" ", "");
                    }
                }
        });

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        dialog.dismiss();
                        String displayName = task.getResult().getString("user_name");
                        setupName.setText(displayName);

                        String userName = task.getResult().getString("unique_name");
                        uniqueName.setText(userName);

                        String image = task.getResult().getString("user_image");

                        if (image != null) {

                            mainImageURI = Uri.parse(image);
                            Picasso
                                    .get()
                                    .load(image)
                                    .placeholder(R.mipmap.placeholder)
                                    .error(R.mipmap.placeholder)
                                    .into(setupImage);
                        }
                    } else {
                        dialog.dismiss();
                        Toast.makeText(SetupActivity.this, "No data available to retrive", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    dialog.dismiss();
                    String e = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(SetupActivity.this, "Database Error:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        });

        setupBtn = findViewById(R.id.setup_btn);
        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline()) {

                    dialog.show();
                    final String user_name = setupName.getText().toString();
                    final String unique_name = uniqueName.getText().toString();

                    if (!TextUtils.isEmpty(user_name) && !TextUtils.isEmpty(unique_name) && mainImageURI != null) {

                        if (!unique_name.contains(" ")) {

                            CollectionReference userRef = firebaseFirestore.collection("Users");
                            Query query = userRef.whereEqualTo("unique_name", unique_name);
                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                            String user = documentSnapshot.getString("unique_name");

                                            if (Objects.equals(user, unique_name)) {
                                                dialog.dismiss();
                                                Toast.makeText(SetupActivity.this, "Username already exist!! Try different username :(", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    if (task.getResult().size() == 0) {

                                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                                        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                                            @Override
                                            public void onComplete(Task<UploadTask.TaskSnapshot> task) {

                                                if (task.isSuccessful()) {
                                                    storeFirestore(task, user_name, unique_name);
                                                } else {
                                                    dialog.dismiss();
                                                    String e = Objects.requireNonNull(task.getException()).getMessage();
                                                    Toast.makeText(SetupActivity.this, "Image Error:" + e, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            dialog.dismiss();
                            Toast.makeText(SetupActivity.this, "Username should not have spaces :(", Toast.LENGTH_SHORT).show();
                        }
                    } else if (mainImageURI == null) {
                        dialog.dismiss();
                        Toast.makeText(SetupActivity.this, "Profile picture is mandatory :(", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(user_name) && TextUtils.isEmpty(unique_name)) {
                        dialog.dismiss();
                        Toast.makeText(SetupActivity.this, "All fields are mandatory!!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SetupActivity.this, "You are not connected to the internet :(", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imagePicker = findViewById(R.id.pickImage);
        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        pickImage();
                    }
                } else {
                    pickImage();
                }
            }
        });
    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task, String user_name, String unique_name) {

        Uri download_uri;

        if (task != null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = mainImageURI;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("user_id", user_id);
        userMap.put("user_name", user_name);
        userMap.put("unique_name", unique_name.toLowerCase());
        userMap.put("user_image", download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(Task<Void> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(SetupActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
                    Intent setUpIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(setUpIntent);
                    finish();
                } else {
                    dialog.dismiss();
                    String e = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(SetupActivity.this, "Database Error:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast
                    .makeText(this, "Permission denied", Toast.LENGTH_SHORT)
                    .show();
        } else {
            pickImage();
        }
    }

    private void pickImage() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mainImageURI = Objects.requireNonNull(result).getUri();
                setupImage.setImageURI(mainImageURI);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = Objects.requireNonNull(result).getError();
                Toast.makeText(this, "Error:" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}

