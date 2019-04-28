package com.blogspot.yashas003.chitter.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.Activities.CommentsActivity;
import com.blogspot.yashas003.chitter.Activities.FollowersActivity;
import com.blogspot.yashas003.chitter.Activities.UsersProfileActivity;
import com.blogspot.yashas003.chitter.BuildConfig;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.blogspot.yashas003.chitter.Utils.TimeSinceAgo;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.support.constraint.Constraints.TAG;

public class SinglePostAdapter extends RecyclerView.Adapter<SinglePostAdapter.ViewHolder> {

    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference imageRef;
    private StorageReference thumbRef;
    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

    private Context mContext;
    private List<Posts> post_list;

    public SinglePostAdapter(Context mContext, List<Posts> post_list) {
        this.mContext = mContext;
        this.post_list = post_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, viewGroup, false);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final Posts posts = post_list.get(i);

        //Visit User Profile========================================================================
        viewHolder.postUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitUserProfile(posts.getUser_id());
            }
        });

        viewHolder.postOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitUserProfile(posts.getUser_id());
            }
        });

        //Setting posts description=================================================================
        String description = posts.getDesc();
        if (description != null && !description.isEmpty()) {
            viewHolder.description.setVisibility(View.VISIBLE);
            viewHolder.description.setText(description);
        } else {
            viewHolder.description.setVisibility(View.GONE);
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
            String date = TimeSinceAgo.getTimeAgo(millisec, viewHolder.postDate.getContext());
            viewHolder.postDate.setText(date);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }

        //Getting likes count=======================================================================
        isLiked(posts.getPost_id(), viewHolder.likeBtn);


        //Setting posts likes count=================================================================
        likesCount(posts.getPost_id(), viewHolder.likesCount);

        //Setting saved posts=======================================================================
        isSaved(posts.getPost_id(), viewHolder.saveBtn);

        //Double Tap to like the post===============================================================
        viewHolder.postImage.setOnTouchListener(new View.OnTouchListener() {

            private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    doubleTapToLike(viewHolder.likeBtn, posts.getPost_id(), posts.getUser_id(), posts.getImage_url());

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

        //Single Tap to like the post===============================================================
        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeThePost(viewHolder.likeBtn, posts.getPost_id(), posts.getUser_id(), posts.getImage_url());
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

        //Saving the post===========================================================================
        viewHolder.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (viewHolder.saveBtn.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Saves").child(firebaseUser.getUid()).child(posts.getPost_id()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Saves").child(firebaseUser.getUid()).child(posts.getPost_id()).removeValue();
                }
            }
        });

        //Show who liked the post===================================================================
        viewHolder.likesCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent followersIntent = new Intent(v.getContext(), FollowersActivity.class);
                followersIntent.putExtra("id", posts.getPost_id());
                followersIntent.putExtra("title", "likes");
                v.getContext().startActivity(followersIntent);
            }
        });

        //Commenting the post=======================================================================
        viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comment = new Intent(v.getContext(), CommentsActivity.class);
                comment.putExtra("post_id", posts.getPost_id());
                comment.putExtra("owner_id", posts.getUser_id());
                v.getContext().startActivity(comment);
            }
        });

        viewHolder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comment = new Intent(v.getContext(), CommentsActivity.class);
                comment.putExtra("post_id", posts.getPost_id());
                comment.putExtra("owner_id", posts.getUser_id());
                v.getContext().startActivity(comment);
            }
        });

        //getting comments==========================================================================
        getComments(posts.getPost_id(), viewHolder.comment);

        //Post menu=================================================================================
        viewHolder.postManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showMore(posts.getUser_id(), posts.getPost_id(), posts.getImage_url(), posts.getThumb(), v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    private boolean isOnline(View view) {
        ConnectivityManager cm = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void showMore(final String user_id, final String post_id, final String image_url, final String thumb, View v) {

        final Dialog singlePostManage = new Dialog(v.getContext());
        Objects.requireNonNull(singlePostManage.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        singlePostManage.requestWindowFeature(Window.FEATURE_NO_TITLE);
        singlePostManage.setContentView(R.layout.post_manage_dialog);
        singlePostManage.show();

        TextView deletePost = singlePostManage.findViewById(R.id.ic_delete_post);
        TextView reportPost = singlePostManage.findViewById(R.id.ic_report_post);
        TextView copyLink = singlePostManage.findViewById(R.id.ic_copy_link);

        if (user_id.equals(firebaseUser.getUid())) {
            reportPost.setVisibility(View.GONE);
        } else {
            deletePost.setVisibility(View.GONE);
        }

        deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final Dialog progress = new Dialog(v.getContext());
                Objects.requireNonNull(progress.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                progress.requestWindowFeature(Window.FEATURE_NO_TITLE);
                progress.setContentView(R.layout.progress_bar);
                progress.show();

                singlePostManage.dismiss();
                imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(image_url);
                imageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            thumbRef = FirebaseStorage.getInstance().getReferenceFromUrl(thumb);
                            thumbRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        deleteFirestoreData(v, post_id, user_id, progress);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singlePostManage.dismiss();

                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(null, image_url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(v.getContext(), "Link copied to the clip board :)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFirestoreData(final View v, final String post_id, final String user_id, final Dialog progress) {

        firestore.collection("Posts").document(post_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    progress.dismiss();
                    notifyDataSetChanged();
                    ((Activity) mContext).finish();

                    firebaseDatabase.getReference().child("Likes").child(post_id).removeValue();
                    firebaseDatabase.getReference().child("Comments").child(post_id).removeValue();
                    firebaseDatabase.getReference().child("Notifications").child(user_id).child(post_id).removeValue();
                    firebaseDatabase.getReference().child("Saves").child(firebaseUser.getUid()).child(post_id).removeValue();
                    firebaseDatabase.getReference().child("Notifications").child(user_id).child("comment" + post_id).removeValue();
                    firebaseDatabase.getReference().child("Notifications").child(user_id).child(firebaseUser.getUid()).removeValue();

                    Toast.makeText(v.getContext(), "Post deleted :)", Toast.LENGTH_SHORT).show();
                } else {

                    String e = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(v.getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void doubleTapToLike(View likeBtn, String post_id, String user_id, String image_url) {

        if (isOnline(likeBtn)) {
            if (likeBtn.getTag().equals("like")) {
                addNotification(user_id, post_id, image_url);
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post_id).child(firebaseUser.getUid()).setValue(true);
            }
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

    private void likeThePost(View likeBtn, String post_id, String user_id, String image_url) {

        if (isOnline(likeBtn)) {

            if (likeBtn.getTag().equals("like")) {

                addNotification(user_id, post_id, image_url);
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post_id).child(firebaseUser.getUid()).setValue(true);
            } else {

                removeNotification(user_id, post_id);
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post_id).child(firebaseUser.getUid()).removeValue();
            }
        } else {
            Toast.makeText(likeBtn.getContext(), "You are not connected to the internet :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void isSaved(final String postId, final ImageView saveBtn) {

        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Saves").child(firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(postId).exists()) {
                    saveBtn.setImageResource(R.drawable.ic_bookmark_filled);
                    saveBtn.setTag("saved");
                } else {
                    saveBtn.setImageResource(R.drawable.ic_bookmark);
                    saveBtn.setTag("save");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getComments(String post_id, final TextView comments) {

        databaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(post_id);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 0) {
                    comments.setText("No comments!!");
                } else {
                    comments.setText("View all " + dataSnapshot.getChildrenCount() + " comments");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void visitUserProfile(String user_id) {

        Intent intent = new Intent(mContext, UsersProfileActivity.class);
        intent.putExtra("user_id", user_id);
        mContext.startActivity(intent);
    }

    private void addNotification(String user_id, String post_id, String image_url) {

        if (!firebaseUser.getUid().equals(user_id)) {

            String date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Notifications")
                    .child(user_id)
                    .child(post_id);

            HashMap<String, Object> notifyMap = new HashMap<>();
            notifyMap.put("user_id", firebaseUser.getUid());
            notifyMap.put("text", "liked your post.");
            notifyMap.put("post_id", post_id);
            notifyMap.put("image_url", image_url);
            notifyMap.put("time", date);
            notifyMap.put("is_post", true);

            databaseReference.setValue(notifyMap);
        }
    }

    private void removeNotification(String user_id, String post_id) {

        databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(user_id).child(post_id);
        databaseReference.removeValue();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView description;
        TextView postDate;
        TextView postOwner;
        TextView likesCount;
        TextView comment;

        ImageView postUserImage;
        ImageView commentBtn;
        ImageView postManage;
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
            postManage = itemView.findViewById(R.id.post_manage);
            showLike = itemView.findViewById(R.id.like_picture);
            comment = itemView.findViewById(R.id.post_comment);
            postImage = itemView.findViewById(R.id.post_image);
            shareBtn = itemView.findViewById(R.id.share_post);
            postDate = itemView.findViewById(R.id.post_date);
            likeBtn = itemView.findViewById(R.id.like_post);
            saveBtn = itemView.findViewById(R.id.save_post);
        }
    }
}
