package com.blogspot.yashas003.chitter.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.BuildConfig;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private List<Posts> post_list;

    public PostAdapter(List<Posts> post_list) {
        this.post_list = post_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_item, viewGroup, false);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final Posts posts = post_list.get(i);
        viewHolder.setIsRecyclable(false);
        getItemId(i);

        //Setting posts description=================================================================
        String description = posts.getDesc();
        if (description != null && !description.isEmpty()) {
            viewHolder.description.setText(description);
        } else {
            viewHolder.description.setText("No comments!!");
            viewHolder.description.setTextColor(Color.GRAY);
        }


        //Setting posts Picture=====================================================================
        Picasso.get().load(posts.getThumb()).placeholder(R.mipmap.postback)
                .into(viewHolder.postImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.get().load(posts.getImage_url()).placeholder(viewHolder.postImage.getDrawable())
                                .into(viewHolder.postImage);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });


        //Setting posts owner details===============================================================
        firestore.collection("Users").document(posts.getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    String userName = task.getResult().getString("user_name");
                    String userImage = task.getResult().getString("user_image");

                    viewHolder.postOwner.setText(userName);
                    Picasso.get().load(userImage).placeholder(R.mipmap.placeholder).into(viewHolder.postUserImage);
                }
            }
        });


        //Setting posted date=======================================================================
        try {
            long millisec = posts.getTime().getTime();
            String date = DateFormat.format("hh:mm a - dd.MM.yyyy", new Date(millisec)).toString();
            viewHolder.postDate.setText(date);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }


        //Getting likes count=======================================================================
        isLiked(posts.PostId, viewHolder.likeBtn);


        //Setting posts likes count===============================================================
        likesCount(posts.PostId, viewHolder.likesCount);


        //Double Tap to like the post===============================================================
        viewHolder.postImage.setOnTouchListener(new View.OnTouchListener() {

            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    doubleTapToLike(viewHolder.likeBtn, posts.PostId);

                    //Double Tap animation============================================================
                    showLikeAnimation(viewHolder.showLike);
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


        //Like the post=============================================================================
        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeThePost(viewHolder.likeBtn, posts.PostId);
            }
        });


        //Sharing the post Image====================================================================
        viewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable) viewHolder.postImage.getDrawable()).getBitmap();
                sharePostImage(bitmap, v);
            }
        });
    }

    private void doubleTapToLike(View likeBtn, String post_id) {

        if (isOnline(likeBtn)) {
            FirebaseDatabase.getInstance().getReference().child("Likes").child(post_id).child(firebaseUser.getUid()).setValue(true);
        } else {
            Toast.makeText(likeBtn.getContext(), "You are not connected to the internet :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLikeAnimation(final SparkButton button) {
        button.setVisibility(View.VISIBLE);
        button.playAnimation();

        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(View.GONE);
            }
        }, 700);
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    private void sharePostImage(Bitmap bitmap, View view) {

        try {
            File cachePath = new File(view.getContext().getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.jpg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        File imagePath = new File(view.getContext().getCacheDir(), "images");
        File newFile = new File(imagePath, "image.jpg");
        Uri contentUri = FileProvider.getUriForFile(view.getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", newFile);

        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setDataAndType(contentUri, view.getContext().getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.setType("image/*");
            view.getContext().startActivity(Intent.createChooser(shareIntent, "Share using"));
        }
    }

    private void isLiked(String post_id, final ImageView like_btn) {

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Likes").child(post_id);
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    like_btn.setImageResource(R.drawable.ic_likes);
                    like_btn.setTag("liked");
                } else {
                    like_btn.setImageResource(R.drawable.ic_likes_outline);
                    like_btn.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void likesCount(String post_id, final TextView likesCount) {

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Likes").child(post_id);
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                likesCount.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void likeThePost(View likeBtn, String post_id) {

        if (isOnline(likeBtn)) {

            if (likeBtn.getTag().equals("like")) {
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post_id).child(firebaseUser.getUid()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post_id).child(firebaseUser.getUid()).removeValue();
            }
        } else {
            Toast.makeText(likeBtn.getContext(), "You are not connected to the internet :(", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isOnline(View view) {
        ConnectivityManager cm = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView description;
        TextView postDate;
        TextView postOwner;
        TextView likesCount;

        ImageView postUserImage;
        ImageView commentBtn;
        ImageView postImage;
        ImageView shareBtn;
        ImageView saveBtn;
        ImageView likeBtn;

        SparkButton showLike;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            postUserImage = itemView.findViewById(R.id.post_user_image);
            description = itemView.findViewById(R.id.post_description);
            postOwner = itemView.findViewById(R.id.post_username);
            commentBtn = itemView.findViewById(R.id.comment_post);
            likesCount = itemView.findViewById(R.id.likes_count);
            showLike = itemView.findViewById(R.id.like_picture);
            postImage = itemView.findViewById(R.id.post_image);
            shareBtn = itemView.findViewById(R.id.share_post);
            postDate = itemView.findViewById(R.id.post_date);
            likeBtn = itemView.findViewById(R.id.like_post);
            saveBtn = itemView.findViewById(R.id.save_post);
        }
    }
}

