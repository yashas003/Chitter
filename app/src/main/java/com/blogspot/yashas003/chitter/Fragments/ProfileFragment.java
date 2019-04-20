package com.blogspot.yashas003.chitter.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.Activities.EditProfileActivity;
import com.blogspot.yashas003.chitter.Activities.FollowersActivity;
import com.blogspot.yashas003.chitter.Activities.NewPostActivity;
import com.blogspot.yashas003.chitter.Activities.SavedPostsActivity;
import com.blogspot.yashas003.chitter.Activities.SettingsActivity;
import com.blogspot.yashas003.chitter.Adapters.GridViewAdapter;
import com.blogspot.yashas003.chitter.BuildConfig;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;

public class ProfileFragment extends Fragment {
    static final int NUM_COLUMNS = 2;
    BottomNavigationView bottomNavigationView;
    Menu menu;
    MenuItem menuItem;

    GridViewAdapter gridViewAdapter;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    FloatingActionButton savedPosts;
    CollapsingToolbarLayout ctl;
    ConstraintLayout buttonContainer;
    ConstraintLayout downloadImage;
    ConstraintLayout shareImage;
    ConstraintLayout noPost;
    RecyclerView recyclerView;
    CircleImageView userImage;
    ImageView saveImage;
    ImageView backPic;
    TextView firstPicture;
    TextView displayName;
    TextView following;
    TextView followers;
    TextView postCount;
    TextView userBio;
    TextView btnText;
    ProgressBar spinner;
    CardView editBtn;
    AppBarLayout abl;
    Toolbar toolbar;

    FirebaseFirestore firebaseFirestore;
    DatabaseReference reference;
    FirebaseAuth mAuth;

    String username;
    String user_id;
    String image;
    String name;
    String bio;

    ArrayList<Posts> mImageUrls = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.findItem(R.id.ic_profile);
        if (!menuItem.isChecked()) {
            menuItem.setChecked(true);
        }

        toolbar = view.findViewById(R.id.profile_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        ctl = view.findViewById(R.id.collapsingToolbar);
        ctl.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        ctl.setCollapsedTitleTextAppearance(R.style.ToolBarFont);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        recyclerView = view.findViewById(R.id.posts_recyclerView);
        following = view.findViewById(R.id.user_following);
        followers = view.findViewById(R.id.user_followers);
        backPic = view.findViewById(R.id.back_picture);
        displayName = view.findViewById(R.id.user_name);
        userBio = view.findViewById(R.id.unique_name);
        btnText = view.findViewById(R.id.button_text);
        noPost = view.findViewById(R.id.no_post);

        postCount = view.findViewById(R.id.user_posts);
        postCount.setText("0");

        spinner = view.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        abl = view.findViewById(R.id.appBarLayout);
        abl.setVisibility(View.GONE);

        userImage = view.findViewById(R.id.user_image);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileImage();
            }
        });

        firstPicture = view.findViewById(R.id.first_picture);
        firstPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getActivity(), NewPostActivity.class);
                startActivity(addIntent);
            }
        });

        savedPosts = view.findViewById(R.id.saved_posts);
        savedPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent savedPost = new Intent(getActivity(), SavedPostsActivity.class);
                startActivity(savedPost);
            }
        });

        editBtn = view.findViewById(R.id.edit_profile_btn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent edit = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(edit);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String followersCount = followers.getText().toString();
                if (!followersCount.equals("0")) {
                    Intent followersIntent = new Intent(getActivity(), FollowersActivity.class);
                    followersIntent.putExtra("id", user_id);
                    followersIntent.putExtra("title", "followers");
                    startActivity(followersIntent);
                }
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String followingCount = following.getText().toString();
                if (!followingCount.equals("0")) {
                    Intent followersIntent = new Intent(getActivity(), FollowersActivity.class);
                    followersIntent.putExtra("id", user_id);
                    followersIntent.putExtra("title", "following");
                    startActivity(followersIntent);
                }
            }
        });

        gridViewAdapter = new GridViewAdapter(mImageUrls);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(gridViewAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        getPosts();
                        getFollowing();
                        getFollowers();

                        abl.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);

                        name = task.getResult().getString("user_name");
                        if (name != null && !name.trim().isEmpty()) {
                            displayName.setVisibility(View.VISIBLE);
                            displayName.setText(name);
                        } else {
                            displayName.setVisibility(View.GONE);
                        }

                        username = task.getResult().getString("unique_name");
                        if (username != null && !username.trim().isEmpty()) {
                            ctl.setTitle(username);
                        } else {
                            ctl.setTitle(" ");
                        }

                        bio = task.getResult().getString("user_bio");
                        if (bio != null && !bio.trim().isEmpty()) {
                            userBio.setVisibility(View.VISIBLE);
                            userBio.setText(bio);
                        } else {
                            userBio.setVisibility(View.GONE);
                        }

                        image = task.getResult().getString("user_image");
                        Picasso.get().load(image).placeholder(R.mipmap.placeholder).into(userImage, new Callback() {
                            @Override
                            public void onSuccess() {

                                Bitmap bitmap = ((BitmapDrawable) userImage.getDrawable()).getBitmap();
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@Nullable Palette palette) {

                                        if (palette != null) {
                                            Palette.Swatch colorVibrant = palette.getVibrantSwatch();
                                            Palette.Swatch colorDominant = palette.getDominantSwatch();
                                            Palette.Swatch colorMuted = palette.getMutedSwatch();

                                            if (colorVibrant != null) {

                                                ctl.setContentScrimColor(colorVibrant.getRgb());
                                                editBtn.setCardBackgroundColor(colorVibrant.getRgb());
                                                btnText.setTextColor(colorVibrant.getTitleTextColor());
                                                ctl.setCollapsedTitleTextColor(colorVibrant.getTitleTextColor());
                                            } else if (colorDominant != null) {

                                                ctl.setContentScrimColor(colorDominant.getRgb());
                                                editBtn.setCardBackgroundColor(colorDominant.getRgb());
                                                btnText.setTextColor(colorDominant.getTitleTextColor());
                                                ctl.setCollapsedTitleTextColor(colorDominant.getTitleTextColor());
                                            } else if (colorMuted != null) {

                                                ctl.setContentScrimColor(colorMuted.getRgb());
                                                editBtn.setCardBackgroundColor(colorMuted.getRgb());
                                                btnText.setTextColor(colorMuted.getTitleTextColor());
                                                ctl.setCollapsedTitleTextColor(colorMuted.getTitleTextColor());
                                            }
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                userImage.setImageResource(R.mipmap.placeholder);
                            }
                        });
                        Picasso.get().load(image).placeholder(R.mipmap.backpic).into(backPic);
                    } else {
                        abl.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                    }
                } else {
                    abl.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                    String e = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(getActivity(), "Database Error:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getFollowing() {

        reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(user_id).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                following.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getFollowers() {

        reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(user_id).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followers.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getPosts() {

        firebaseFirestore.collection("Posts").orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        int postsCount = 0;
                        postCount.setText("0");
                        if (e != null) {
                            Log.e(TAG, "onEvent: ", e);
                        } else {
                            mImageUrls.clear();
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Posts posts = doc.getDocument().toObject(Posts.class);

                                    if (posts.getUser_id().equals(user_id)) {

                                        postsCount++;
                                        postCount.setText("" + postsCount);
                                        mImageUrls.add(posts);
                                        noPost.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                }
                                gridViewAdapter.notifyDataSetChanged();
                            }
                        }
                        if (mImageUrls.isEmpty()) {
                            noPost.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void showProfileImage() {

        Dialog saveImageDialog = new Dialog(getActivity());
        saveImageDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        saveImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveImageDialog.setContentView(R.layout.save_image);
        saveImageDialog.show();

        buttonContainer = saveImageDialog.findViewById(R.id.button_container);
        buttonContainer.setVisibility(View.VISIBLE);

        saveImage = saveImageDialog.findViewById(R.id.save_image);
        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.placeholder).into(saveImage);

        shareImage = saveImageDialog.findViewById(R.id.share);
        shareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = ((BitmapDrawable) saveImage.getDrawable()).getBitmap();
                try {
                    File cachePath = new File(getActivity().getCacheDir(), "images");
                    cachePath.mkdirs();
                    FileOutputStream stream = new FileOutputStream(cachePath + "/image.jpg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    stream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                File imagePath = new File(getActivity().getCacheDir(), "images");
                File newFile = new File(imagePath, "image.jpg");
                Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", newFile);

                if (contentUri != null) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setDataAndType(contentUri, getActivity().getContentResolver().getType(contentUri));
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "Share using"));
                }
            }
        });

        downloadImage = saveImageDialog.findViewById(R.id.download);
        downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        saveImageToGallery();
                    }
                } else {
                    saveImageToGallery();
                }
            }
        });
    }

    private void saveImageToGallery() {

        Bitmap bitmap = ((BitmapDrawable) saveImage.getDrawable()).getBitmap();
        String date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/Chitter");
        myDir.mkdirs();
        String imageName = name + "-" + date + ".jpg";
        File file = new File(myDir, imageName);

        try {
            Toast.makeText(getActivity(), "Image saved to gallery :)", Toast.LENGTH_SHORT).show();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(getContext(), new String[]{String.valueOf(file)}, null, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings_btn:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return false;

            default:
                return false;
        }
    }
}
