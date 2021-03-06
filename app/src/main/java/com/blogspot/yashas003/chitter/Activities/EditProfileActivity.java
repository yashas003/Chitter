package com.blogspot.yashas003.chitter.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditProfileActivity extends AppCompatActivity {
    StorageReference mStore;
    FirebaseFirestore mFirestore;
    FirebaseUser user;
    FirebaseAuth mAuth;

    CircleImageView userImage;
    EditText userDisplayName;
    EditText userUniqueName;
    EditText userWebsite;
    EditText userBio;
    EditText fbUsername;
    EditText tweUsername;
    EditText tweUserID;
    EditText instaUsername;
    TextView userPhNumber;
    TextView userGender;
    TextView userEmail;
    TextView imagePicker;
    TextView facebookLink;
    TextView twitterLink;
    ImageView boy;
    ImageView girl;
    Toolbar toolbar;
    Dialog dialog;

    boolean isChanged = false;
    Uri mainImageURI = null;
    Bitmap compressedImageFile;
    String oldUniqueName;
    String unique_name;
    String user_id;
    String Male = "Male";
    String Female = "Female";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        toolbar = findViewById(R.id.edit_profile_toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolBarFont);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userUniqueName = findViewById(R.id.edit_user_username);
        userUniqueName.setFilters(new InputFilter[]{
                new InputFilter.AllCaps() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        return String.valueOf(source).toLowerCase().replace(" ", "");
                    }
                }
        });

        userImage = findViewById(R.id.edit_user_image);
        userDisplayName = findViewById(R.id.edit_user_display_name);
        userWebsite = findViewById(R.id.edit_user_website);
        userBio = findViewById(R.id.edit_user_bio);

        fbUsername = findViewById(R.id.facebook_username);
        tweUsername = findViewById(R.id.twitter_username);
        tweUserID = findViewById(R.id.twitter_userID);
        instaUsername = findViewById(R.id.instagram_username);

        mStore = FirebaseStorage.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        user = mAuth.getCurrentUser();

        dialog = new Dialog(EditProfileActivity.this);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_bar);

        facebookLink = findViewById(R.id.facebook_link);
        facebookLink.setMovementMethod(LinkMovementMethod.getInstance());

        twitterLink = findViewById(R.id.twitter_link);
        twitterLink.setMovementMethod(LinkMovementMethod.getInstance());

        imagePicker = findViewById(R.id.image_picker);
        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(EditProfileActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        pickImage();
                    }
                } else {
                    pickImage();
                }
            }
        });

        userGender = findViewById(R.id.gender);
        userGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog genderDialog = new Dialog(EditProfileActivity.this);
                genderDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                genderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                genderDialog.setContentView(R.layout.gender_dialog);
                genderDialog.show();

                boy = genderDialog.findViewById(R.id.maleImage);
                boy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        genderDialog.dismiss();
                        userGender.setText(Male);
                    }
                });

                girl = genderDialog.findViewById(R.id.femaleImage);
                girl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        genderDialog.dismiss();
                        userGender.setText(Female);
                    }
                });
            }
        });

        userEmail = findViewById(R.id.edit_user_email);
        userEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(EditProfileActivity.this, EmailActivity.class);
                startActivity(emailIntent);
            }
        });

        userPhNumber = findViewById(R.id.edit_user_phone_number);
        userPhNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phIntent = new Intent(EditProfileActivity.this, PhNoActivity.class);
                startActivity(phIntent);
            }
        });

        final Dialog dialog = new Dialog(EditProfileActivity.this);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_bar);
        dialog.show();

        mFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                String image = task.getResult().getString("user_image");

                                if (image != null) {

                                    mainImageURI = Uri.parse(image);
                                    Picasso.get().load(image).placeholder(R.mipmap.placeholder)
                                            .error(R.mipmap.placeholder).into(userImage);
                                }

                                String displayname = task.getResult().getString("user_name");
                                userDisplayName.setText(displayname);

                                oldUniqueName = task.getResult().getString("unique_name");
                                userUniqueName.setText(oldUniqueName);

                                String website = task.getResult().getString("user_website");
                                userWebsite.setText(website);

                                String bio = task.getResult().getString("user_bio");
                                userBio.setText(bio);

                                String gender = task.getResult().getString("user_gender");
                                userGender.setText(gender);

                                String fb_username = task.getResult().getString("facebook_username");
                                fbUsername.setText(fb_username);

                                String tweet_username = task.getResult().getString("twitter_username");
                                tweUsername.setText(tweet_username);

                                String tweet_userID = task.getResult().getString("twitter_userID");
                                tweUserID.setText(tweet_userID);

                                String insta_username = task.getResult().getString("instagram_username");
                                instaUsername.setText(insta_username);
                            }
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Could not retrieve the data :(", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (user != null) {

            String email = user.getEmail();
            userEmail.setText(email);

            String phNumber = user.getPhoneNumber();
            userPhNumber.setText(phNumber);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ContextCompat.checkSelfPermission(EditProfileActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
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

                mainImageURI = result.getUri();
                userImage.setImageURI(mainImageURI);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = Objects.requireNonNull(result).getError();
                Toast.makeText(this, "Error:" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.save_profile_settings:
                if (isOnline()) {
                    saveProfileSettings();
                } else {
                    Toast.makeText(this, "You are not connected to the internet :(", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    private void saveProfileSettings() {

        dialog.show();
        final String display_name = userDisplayName.getText().toString();
        unique_name = userUniqueName.getText().toString();

        if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(unique_name)) {

            if (!unique_name.contains(" ")) {

                if (!oldUniqueName.equals(unique_name)) {

                    final CollectionReference userRef = mFirestore.collection("Users");
                    final Query query = userRef.whereEqualTo("unique_name", unique_name);
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    String user = documentSnapshot.getString("unique_name");

                                    if (Objects.equals(user, unique_name)) {
                                        dialog.dismiss();
                                        Toast.makeText(EditProfileActivity.this, "Username already exist!! Try different username :(", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            if (task.getResult().size() == 0) {

                                if (isChanged) {

                                    StorageReference image_path = mStore.child("profile_images").child(user_id + ".jpg");

                                    // Image Compression
                                    File newImageFile = new File(mainImageURI.getPath());
                                    try {

                                        compressedImageFile = new Compressor(EditProfileActivity.this)
                                                .setQuality(100)
                                                .setMaxHeight(380)
                                                .setMaxWidth(380)
                                                .compressToBitmap(newImageFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] profileImage = baos.toByteArray();

                                    // Storing Image
                                    image_path.putBytes(profileImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                                        @Override
                                        public void onComplete(Task<UploadTask.TaskSnapshot> task) {

                                            if (task.isSuccessful()) {

                                                storeData(task, display_name, unique_name);
                                            } else {

                                                dialog.dismiss();
                                                storeData(null, display_name, unique_name);
                                                String e = Objects.requireNonNull(task.getException()).getMessage();
                                                Toast.makeText(EditProfileActivity.this, "Image Error:" + e, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {

                                    storeData(null, display_name, unique_name);
                                }
                            }
                        }
                    });
                } else {
                    if (isChanged) {

                        StorageReference image_path = mStore.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onComplete(Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    storeData(task, display_name, unique_name);
                                } else {

                                    dialog.dismiss();
                                    storeData(null, display_name, unique_name);
                                    String e = Objects.requireNonNull(task.getException()).getMessage();
                                    Toast.makeText(EditProfileActivity.this, "Image Error:" + e, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {

                        storeData(null, display_name, unique_name);
                    }
                }
            } else {
                dialog.dismiss();
                Toast.makeText(this, "Username should not have spaces :(", Toast.LENGTH_SHORT).show();
            }
        } else {

            dialog.dismiss();
            Toast.makeText(this, "Users must have a display name and username :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeData(Task<UploadTask.TaskSnapshot> task, String user_name, String unique_name) {

        Uri download_uri;

        if (task != null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = mainImageURI;
        }

        String user_website = userWebsite.getText().toString();
        String user_Bio = userBio.getText().toString();
        String user_gender = userGender.getText().toString();
        String facebook_username = fbUsername.getText().toString();
        String twitter_username = tweUsername.getText().toString();
        String twitter_userID = tweUserID.getText().toString();
        String instagram_username = instaUsername.getText().toString();

        Map<String, String> userMap = new HashMap<>();
        userMap.put("user_id", user_id);
        userMap.put("user_image", download_uri.toString());
        userMap.put("user_name", user_name);
        userMap.put("unique_name", unique_name.toLowerCase());
        userMap.put("user_website", user_website);
        userMap.put("user_bio", user_Bio);
        userMap.put("user_gender", user_gender);
        userMap.put("facebook_username", facebook_username);
        userMap.put("twitter_username", twitter_username);
        userMap.put("twitter_userID", twitter_userID);
        userMap.put("instagram_username", instagram_username);

        mFirestore
                .collection("Users")
                .document(user_id)
                .set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(EditProfileActivity.this, "Profile updated :)", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {

                            Toast.makeText(EditProfileActivity.this, "Error while saving the data :(", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
