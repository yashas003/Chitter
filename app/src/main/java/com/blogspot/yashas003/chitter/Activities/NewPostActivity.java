package com.blogspot.yashas003.chitter.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    FloatingActionButton addLocation;
    Bitmap compressedImageFile;
    FirebaseAuth mAuth;
    String user_id;
    Dialog dialog;
    ImageView newImage;
    EditText description;
    Toolbar toolbar;
    Uri postImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        toolbar = findViewById(R.id.newPostToolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolBarFont);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dialog = new Dialog(NewPostActivity.this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_bar);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        newImage = findViewById(R.id.new_image);
        newImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(NewPostActivity.this);
            }
        });

        description = findViewById(R.id.description);

        addLocation = findViewById(R.id.add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(NewPostActivity.this, "CHILL, Developer lazy enough to implement location feature :(  ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                try {
                    postImageUri = result.getUri();
                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e, Toast.LENGTH_SHORT).show();
                    return;
                }
                newImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = Objects.requireNonNull(result).getError();
                Toast.makeText(this, "Error:" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_post:
                postTheImage();
                break;
        }
        return true;
    }

    private void postTheImage() {

        final String desc = description.getText().toString();

        if (postImageUri != null) {

            dialog.show();
            final String randomName = UUID.randomUUID().toString();
            StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");

            filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                    final String downLoadUri = task.getResult().getDownloadUrl().toString();

                    if (task.isSuccessful()) {

                        File newImageFile = new File(postImageUri.getPath());
                        try {

                            compressedImageFile = new Compressor(NewPostActivity.this)
                                    .setQuality(1)
                                    .setMaxHeight(30)
                                    .setMaxWidth(30)
                                    .compressToBitmap(newImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();

                        UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                String downloadThumbUri = taskSnapshot.getDownloadUrl().toString();
                                String post_id = UUID.randomUUID().toString();

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("image_url", downLoadUri);
                                postMap.put("thumb", downloadThumbUri);
                                postMap.put("desc", desc);
                                postMap.put("user_id", user_id);
                                postMap.put("post_id", post_id);
                                postMap.put("time", FieldValue.serverTimestamp());

                                firebaseFirestore.collection("Posts").document(post_id).set(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            Toast.makeText(NewPostActivity.this, "Post successful :)", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            String e = task.getException().getMessage();
                                            Toast.makeText(NewPostActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                                        }
                                        dialog.dismiss();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(NewPostActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    } else {
                        dialog.dismiss();
                    }
                }
            });
        }
    }
}
