package com.blogspot.yashas003.chitter.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.yashas003.chitter.Activities.UsersProfileActivity;
import com.blogspot.yashas003.chitter.Model.Comments;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comments> mComments;

    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirestore;

    public CommentAdapter(Context mContext, List<Comments> mComments) {
        this.mContext = mContext;
        this.mComments = mComments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, viewGroup, false);
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final Comments comments = mComments.get(i);

        mFirestore.collection("Users").document(comments.getOwner()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    String userName = task.getResult().getString("unique_name");
                    String userImage = task.getResult().getString("user_image");

                    viewHolder.username.setText(userName);
                    Picasso.get().load(userImage).placeholder(R.mipmap.placeholder).into(viewHolder.userImage);
                }
            }
        });

        viewHolder.comment.setText(comments.getComment());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, UsersProfileActivity.class);
                intent.putExtra("user_id", comments.getOwner());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView userImage;
        TextView username, comment;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.comment_user_image);
            username = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.comment_user_comment);
        }
    }
}
