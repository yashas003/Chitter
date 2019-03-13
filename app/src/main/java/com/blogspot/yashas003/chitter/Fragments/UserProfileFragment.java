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
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.Adapters.StaggeredRecyclerViewAdapter;
import com.blogspot.yashas003.chitter.BuildConfig;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileFragment extends Fragment {
    static final int NUM_COLUMNS = 2;
    BottomNavigationView bottomNavigationView;
    Menu menu;
    MenuItem menuItem;

    AVLoadingIndicatorView progressBar;
    ConstraintLayout downloadProfileImage;
    ConstraintLayout shareProfileImage;
    ConstraintLayout likeProfileImage;
    CollapsingToolbarLayout collapseBar;
    RecyclerView postsRecyclerView;
    CircleImageView userProfileImage;
    TextView displayProfileName;
    TextView userProfileBio;
    TextView followBtnText;
    AppBarLayout appBarLayout;
    BoomMenuButton boomMenu;
    ImageView backgroundPic;
    ImageView saveProfileImage;
    CardView followBtn;
    Toolbar toolbar;
    Time time;

    String faceBook_username;
    String tweeter_username;
    String tweeter_userID;
    String instagram_username;
    String username;
    String user_id;
    String website;
    String image;
    String name;
    String bio;
    ArrayList<String> mImageUrls = new ArrayList<>();

    FirebaseFirestore mFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.findItem(R.id.ic_search);
        if (!menuItem.isChecked()) {
            menuItem.setChecked(true);
        }

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            user_id = bundle.getString("user_id");
        }

        toolbar = view.findViewById(R.id.profile_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        time = new Time();
        time.setToNow();

        collapseBar = view.findViewById(R.id.collapsingToolbar);
        collapseBar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapseBar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        backgroundPic = view.findViewById(R.id.back_picture);
        displayProfileName = view.findViewById(R.id.user_name);
        userProfileBio = view.findViewById(R.id.unique_name);
        followBtn = view.findViewById(R.id.follow_btn);
        followBtnText = view.findViewById(R.id.follow_btn_text);

        progressBar = view.findViewById(R.id.user_progress);
        progressBar.setVisibility(View.VISIBLE);

        appBarLayout = view.findViewById(R.id.appBarLayout);
        appBarLayout.setVisibility(View.GONE);

        postsRecyclerView = view.findViewById(R.id.posts_recyclerView);
        postsRecyclerView.setVisibility(View.GONE);

        userProfileImage = view.findViewById(R.id.user_image);
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileImage();
            }
        });

        boomMenu = view.findViewById(R.id.boom);
        boomMenu.setButtonEnum(ButtonEnum.SimpleCircle);
        boomMenu.setPiecePlaceEnum(PiecePlaceEnum.DOT_4_1);
        boomMenu.setButtonPlaceEnum(ButtonPlaceEnum.SC_4_1);

        boomMenu.addBuilder(new SimpleCircleButton.Builder().imagePadding(new Rect(35, 35, 35, 35)).normalColorRes(R.color.colorFacebook).normalImageRes(R.drawable.ic_facebook));
        boomMenu.addBuilder(new SimpleCircleButton.Builder().imagePadding(new Rect(35, 35, 35, 35)).normalColorRes(R.color.colorWebsite).normalImageRes(R.drawable.ic_link));
        boomMenu.addBuilder(new SimpleCircleButton.Builder().imagePadding(new Rect(35, 35, 35, 35)).normalColorRes(R.color.colorInstagram).normalImageRes(R.drawable.ic_instagram));
        boomMenu.addBuilder(new SimpleCircleButton.Builder().imagePadding(new Rect(35, 35, 35, 35)).normalColorRes(R.color.colorTwitter).normalImageRes(R.drawable.ic_twitter));
        boomMenu.setOnBoomListener(new OnBoomListener() {
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

    private void visitFacebook() {

        if (faceBook_username != null && !faceBook_username.trim().isEmpty()) {

            String facebookUrl = "https://www.facebook.com/" + faceBook_username;
            try {
                int versionCode = getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
                if (versionCode >= 3002850) {
                    Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } else {
                    Uri uri = Uri.parse("fb://page/" + faceBook_username);
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

        if (instagram_username != null && !instagram_username.trim().isEmpty()) {

            Uri uri = Uri.parse("http://instagram.com/_u/" + instagram_username);
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/" + instagram_username)));
            }
        } else {
            Toast.makeText(getActivity(), name + " does not have an Instagram :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void visitTwitter() {

        if ((tweeter_username != null && !tweeter_username.trim().isEmpty()) && (tweeter_userID != null && !tweeter_userID.trim().isEmpty())) {

            Intent intent;
            try {
                getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=" + tweeter_userID));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } catch (Exception e) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + tweeter_username));
            }
            this.startActivity(intent);
        } else {
            Toast.makeText(getActivity(), name + " does not have a Twitter :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProfileImage() {


        Dialog saveImageDialog = new Dialog(getActivity());
        saveImageDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        saveImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        saveImageDialog.setContentView(R.layout.save_image);
        saveImageDialog.show();

        saveProfileImage = saveImageDialog.findViewById(R.id.save_image);
        Picasso
                .get()
                .load(image)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.mipmap.placeholder)
                .into(saveProfileImage);

        likeProfileImage = saveImageDialog.findViewById(R.id.like);
        likeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Yet to implement this feature.", Toast.LENGTH_SHORT).show();
            }
        });

        shareProfileImage = saveImageDialog.findViewById(R.id.share);
        shareProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = ((BitmapDrawable) saveProfileImage.getDrawable()).getBitmap();
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

        downloadProfileImage = saveImageDialog.findViewById(R.id.download);
        downloadProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = ((BitmapDrawable) saveProfileImage.getDrawable()).getBitmap();
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
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mFirestore = FirebaseFirestore.getInstance();
        mFirestore
                .collection("Users")
                .document(user_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                appBarLayout.setVisibility(View.VISIBLE);
                                postsRecyclerView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);

                                website = task.getResult().getString("user_website");
                                faceBook_username = task.getResult().getString("facebook_username");
                                tweeter_username = task.getResult().getString("twitter_username");
                                tweeter_userID = task.getResult().getString("twitter_userID");
                                instagram_username = task.getResult().getString("instagram_username");

                                name = task.getResult().getString("user_name");
                                if (name != null && !name.trim().isEmpty()) {
                                    displayProfileName.setVisibility(View.VISIBLE);
                                    displayProfileName.setText(name);
                                } else {
                                    displayProfileName.setVisibility(View.GONE);
                                }

                                username = task.getResult().getString("unique_name");
                                if (username != null && !username.trim().isEmpty()) {
                                    collapseBar.setTitle(username);
                                } else {
                                    collapseBar.setTitle(" ");
                                }

                                bio = task.getResult().getString("user_bio");
                                if (bio != null && !bio.trim().isEmpty()) {
                                    userProfileBio.setVisibility(View.VISIBLE);
                                    userProfileBio.setText(bio);
                                } else {
                                    userProfileBio.setVisibility(View.GONE);
                                }

                                image = task.getResult().getString("user_image");
                                Picasso
                                        .get()
                                        .load(image)
                                        .placeholder(R.mipmap.placeholder)
                                        .into(userProfileImage, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                                Bitmap bitmap = ((BitmapDrawable) userProfileImage.getDrawable()).getBitmap();
                                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                                    @Override
                                                    public void onGenerated(@Nullable Palette palette) {

                                                        if (palette != null) {
                                                            Palette.Swatch colorVibrant = palette.getVibrantSwatch();
                                                            Palette.Swatch colorDominant = palette.getDominantSwatch();
                                                            Palette.Swatch colorMuted = palette.getMutedSwatch();

                                                            if (colorVibrant != null) {

                                                                collapseBar.setContentScrimColor(colorVibrant.getRgb());
                                                                followBtn.setCardBackgroundColor(colorVibrant.getRgb());
                                                                followBtnText.setTextColor(colorVibrant.getTitleTextColor());
                                                                collapseBar.setCollapsedTitleTextColor(colorVibrant.getTitleTextColor());
                                                            } else if (colorDominant != null) {

                                                                collapseBar.setContentScrimColor(colorDominant.getRgb());
                                                                followBtn.setCardBackgroundColor(colorDominant.getRgb());
                                                                followBtnText.setTextColor(colorDominant.getTitleTextColor());
                                                                collapseBar.setCollapsedTitleTextColor(colorDominant.getTitleTextColor());
                                                            } else if (colorMuted != null) {

                                                                collapseBar.setContentScrimColor(colorMuted.getRgb());
                                                                followBtn.setCardBackgroundColor(colorMuted.getRgb());
                                                                followBtnText.setTextColor(colorMuted.getTitleTextColor());
                                                                collapseBar.setCollapsedTitleTextColor(colorMuted.getTitleTextColor());
                                                            }
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                userProfileImage.setImageResource(R.mipmap.placeholder);
                                            }
                                        });
                                Picasso
                                        .get()
                                        .load(image)
                                        .placeholder(R.mipmap.placeholder)
                                        .error(R.mipmap.placeholder)
                                        .into(backgroundPic);
                            } else {
                                appBarLayout.setVisibility(View.VISIBLE);
                                postsRecyclerView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                Toast
                                        .makeText(getActivity(), "No data available to retrieve", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            appBarLayout.setVisibility(View.VISIBLE);
                            postsRecyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            String e = Objects.requireNonNull(task.getException()).getMessage();
                            Toast
                                    .makeText(getActivity(), "Database Error:" + e, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageBitmaps();
    }

    private void imageBitmaps() {

        mImageUrls.add("https://images.pexels.com/photos/313032/pexels-photo-313032.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1677275/pexels-photo-1677275.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1660966/pexels-photo-1660966.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/672630/pexels-photo-672630.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1649735/pexels-photo-1649735.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1661674/pexels-photo-1661674.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1649804/pexels-photo-1649804.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/965157/pexels-photo-965157.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1679011/pexels-photo-1679011.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1122462/pexels-photo-1122462.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1656770/pexels-photo-1656770.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/103651/pexels-photo-103651.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/928475/pexels-photo-928475.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/872795/pexels-photo-872795.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1649091/pexels-photo-1649091.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1262302/pexels-photo-1262302.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1282293/pexels-photo-1282293.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/313032/pexels-photo-313032.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1677275/pexels-photo-1677275.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1660966/pexels-photo-1660966.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/672630/pexels-photo-672630.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1649735/pexels-photo-1649735.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1661674/pexels-photo-1661674.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1649804/pexels-photo-1649804.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/965157/pexels-photo-965157.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1679011/pexels-photo-1679011.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1122462/pexels-photo-1122462.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1656770/pexels-photo-1656770.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/103651/pexels-photo-103651.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/928475/pexels-photo-928475.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/872795/pexels-photo-872795.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1649091/pexels-photo-1649091.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1262302/pexels-photo-1262302.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");
        mImageUrls.add("https://images.pexels.com/photos/1282293/pexels-photo-1282293.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500");

        StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter = new StaggeredRecyclerViewAdapter(getActivity(), mImageUrls);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        postsRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        postsRecyclerView.setAdapter(staggeredRecyclerViewAdapter);
    }
}
