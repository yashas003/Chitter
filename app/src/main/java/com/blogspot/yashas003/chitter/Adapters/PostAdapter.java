package com.blogspot.yashas003.chitter.Adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Posts> post_list;
    private FirebaseFirestore firestore;

    public PostAdapter(List<Posts> post_list) {
        this.post_list = post_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_item, viewGroup, false);
        firestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final Posts posts = post_list.get(i);

        String description = posts.getDesc();
        if (description != null && !description.trim().isEmpty()) {
            viewHolder.description.setText(description);
        } else {
            viewHolder.description.setText("No comments!!");
            viewHolder.description.setTextColor(Color.GRAY);
        }

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(Color.WHITE);
        Picasso.get().load(posts.getImage_url()).placeholder(gradientDrawable).into(viewHolder.postImage);

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

        try {
            long millisec = posts.getTime().getTime();
            String date = DateFormat.format("hh:mm - dd.MM.yyyy", new Date(millisec)).toString();
            viewHolder.postDate.setText(date);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView description;
        TextView postDate;
        TextView postOwner;
        ImageView postImage;
        ImageView postUserImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.post_description);
            postDate = itemView.findViewById(R.id.post_date);
            postOwner = itemView.findViewById(R.id.post_username);
            postUserImage = itemView.findViewById(R.id.post_user_image);
            postImage = itemView.findViewById(R.id.post_image);
        }
    }
}
