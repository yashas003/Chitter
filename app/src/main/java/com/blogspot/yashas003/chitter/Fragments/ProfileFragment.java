package com.blogspot.yashas003.chitter.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
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
import com.blogspot.yashas003.chitter.Activities.SettingsActivity;
import com.blogspot.yashas003.chitter.Adapters.StaggeredRecyclerViewAdapter;
import com.blogspot.yashas003.chitter.BuildConfig;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.OnBoomListener;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

    StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    ProgressBar spinner;
    ConstraintLayout downloadImage;
    ConstraintLayout shareImage;
    ConstraintLayout likeImage;
    CollapsingToolbarLayout ctl;
    RecyclerView recyclerView;
    CircleImageView userImage;
    TextView displayName;
    TextView userBio;
    TextView btnText;
    TextView noPosts;
    AppBarLayout abl;
    BoomMenuButton bmb;
    ImageView backPic;
    ImageView saveImage;
    CardView editBtn;
    Toolbar toolbar;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    String fb_username;
    String tweet_username;
    String tweet_userID;
    String insta_username;
    String username;
    String user_id;
    String website;
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
        ctl.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        backPic = view.findViewById(R.id.back_picture);
        displayName = view.findViewById(R.id.user_name);
        userBio = view.findViewById(R.id.unique_name);
        btnText = view.findViewById(R.id.button_text);
        recyclerView = view.findViewById(R.id.posts_recyclerView);
        noPosts = view.findViewById(R.id.no_posts);

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

        editBtn = view.findViewById(R.id.edit_profile_btn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent edit = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(edit);
            }
        });

        bmb = view.findViewById(R.id.boom);
        bmb.setButtonEnum(ButtonEnum.SimpleCircle);
        bmb.setPiecePlaceEnum(PiecePlaceEnum.DOT_4_1);
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.SC_4_1);

        bmb.addBuilder(new SimpleCircleButton.Builder().imagePadding(new Rect(35, 35, 35, 35)).normalColorRes(R.color.colorFacebook).normalImageRes(R.drawable.ic_facebook));
        bmb.addBuilder(new SimpleCircleButton.Builder().imagePadding(new Rect(35, 35, 35, 35)).normalColorRes(R.color.colorWebsite).normalImageRes(R.drawable.ic_link));
        bmb.addBuilder(new SimpleCircleButton.Builder().imagePadding(new Rect(35, 35, 35, 35)).normalColorRes(R.color.colorInstagram).normalImageRes(R.drawable.ic_instagram));
        bmb.addBuilder(new SimpleCircleButton.Builder().imagePadding(new Rect(35, 35, 35, 35)).normalColorRes(R.color.colorTwitter).normalImageRes(R.drawable.ic_twitter));
        bmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {

                switch (index) {
                    case 0:
                        visitFacebook();
                        break;

                    case 1:
                        visitWebsite();
                        break;

                    case 2:
                        visitInstagram();
                        break;

                    case 3:
                        visitTwitter();
                        break;
                }
            }

            @Override
            public void onBackgroundClick() {
            }

            @Override
            public void onBoomWillHide() {
            }

            @Override
            public void onBoomDidHide() {
            }

            @Override
            public void onBoomWillShow() {
            }

            @Override
            public void onBoomDidShow() {
            }
        });

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

                        staggeredRecyclerViewAdapter = new StaggeredRecyclerViewAdapter(mImageUrls);
                        staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(staggeredGridLayoutManager);
                        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
                        getPosts();

                        abl.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);

                        website = task.getResult().getString("user_website");
                        fb_username = task.getResult().getString("facebook_username");
                        tweet_username = task.getResult().getString("twitter_username");
                        tweet_userID = task.getResult().getString("twitter_userID");
                        insta_username = task.getResult().getString("instagram_username");

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
                        Picasso
                                .get()
                                .load(image)
                                .placeholder(R.mipmap.placeholder)
                                .into(userImage, new Callback() {
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
                        Picasso
                                .get()
                                .load(image)
                                .placeholder(R.mipmap.backpic)
                                .into(backPic);
                    } else {
                        abl.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        Toast
                                .makeText(getActivity(), "No data available to retrieve", Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    abl.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                    String e = Objects.requireNonNull(task.getException()).getMessage();
                    Toast
                            .makeText(getActivity(), "Database Error:" + e, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void getPosts() {

        firebaseFirestore.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(TAG, "onEvent: ", e);
                } else {
                    mImageUrls.clear();
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            Posts posts = doc.getDocument().toObject(Posts.class);

                            if (posts.getUser_id().equals(user_id)) {
                                mImageUrls.add(posts);
                                recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                noPosts.setVisibility(View.VISIBLE);
                            }
                        }
                        Collections.reverse(mImageUrls);
                        staggeredRecyclerViewAdapter.notifyDataSetChanged();
                    }
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

        saveImage = saveImageDialog.findViewById(R.id.save_image);
        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.placeholder).into(saveImage);

        likeImage = saveImageDialog.findViewById(R.id.like);
        likeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Yet to implement this feature.", Toast.LENGTH_SHORT).show();
            }
        });

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
                    startActivity(Intent.createChooser(shareIntent, "Share Using"));
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

    private void visitFacebook() {

        if (fb_username != null && !fb_username.trim().isEmpty()) {

            String facebookUrl = "https://www.facebook.com/" + fb_username;
            try {
                int versionCode = getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
                if (versionCode >= 3002850) {
                    Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } else {
                    Uri uri = Uri.parse("fb://page/" + fb_username);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
            } catch (PackageManager.NameNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
            }
        } else {
            Toast.makeText(getActivity(), name + " does not have a Facebook :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void visitWebsite() {

        if (website != null && !website.trim().isEmpty()) {

            String patter = "^(http|https|ftp)://.*$";

            if (website.matches(patter)) {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(website));
                startActivity(i);
            } else {

                String url = "https://";
                website = url + website;

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(website));
                startActivity(i);
            }
        } else {
            Toast.makeText(getActivity(), name + " does not have a website :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void visitInstagram() {

        if (insta_username != null && !insta_username.trim().isEmpty()) {

            Uri uri = Uri.parse("http://instagram.com/_u/" + insta_username);
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/" + insta_username)));
            }
        } else {
            Toast.makeText(getActivity(), name + " does not have an Instagram :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void visitTwitter() {

        if ((tweet_username != null && !tweet_username.trim().isEmpty()) && (tweet_userID != null && !tweet_userID.trim().isEmpty())) {

            Intent intent;
            try {
                getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=" + tweet_userID));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } catch (Exception e) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + tweet_username));
            }
            this.startActivity(intent);
        } else {
            Toast.makeText(getActivity(), name + " does not have a Twitter :(", Toast.LENGTH_SHORT).show();
        }
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
