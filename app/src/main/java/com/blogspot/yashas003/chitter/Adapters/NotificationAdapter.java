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

import com.blogspot.yashas003.chitter.Activities.PostDetailActivity;
import com.blogspot.yashas003.chitter.Activities.UsersProfileActivity;
import com.blogspot.yashas003.chitter.Model.Notifications;
import com.blogspot.yashas003.chitter.R;
import com.blogspot.yashas003.chitter.Utils.PicassoCache;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notifications> mNotifications;

    private FirebaseFirestore firestore;

    public NotificationAdapter(Context mContext, List<Notifications> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, viewGroup, false);
        firestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final Notifications notifications = mNotifications.get(i);
        viewHolder.notifiedParameter.setText(notifications.getText());

        firestore.collection("Users").document(notifications.getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    String user_image = task.getResult().getString("user_image");
                    Picasso.get().load(user_image).placeholder(R.mipmap.placeholder).into(viewHolder.notifyUserImage);

                    String user_name = task.getResult().getString("user_name");
                    viewHolder.notifyUserName.setText(user_name);
                }
            }
        });

        if (!notifications.getPost_id().equals("")) {

            PicassoCache
                    .getPicassoInstance(mContext)
                    .load(notifications.getImage_url())
                    .placeholder(R.mipmap.postback)
                    .into(viewHolder.notifiedImage);
        }

        if (notifications.isIs_post()) {
            viewHolder.notifiedImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder.notifiedImage.setVisibility(View.GONE);
        }

        viewHolder.notifyUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileIntent = new Intent(mContext, UsersProfileActivity.class);
                profileIntent.putExtra("user_id", notifications.getUser_id());
                mContext.startActivity(profileIntent);
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (notifications.isIs_post()) {

                    Intent postDetail = new Intent(mContext, PostDetailActivity.class);
                    postDetail.putExtra("post_id", notifications.getPost_id());
                    mContext.startActivity(postDetail);
                } else {

                    Intent profileIntent = new Intent(mContext, UsersProfileActivity.class);
                    profileIntent.putExtra("user_id", notifications.getUser_id());
                    mContext.startActivity(profileIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView notifyUserImage;
        TextView notifiedParameter;
        ImageView notifiedImage;
        TextView notifyUserName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            notifyUserImage = itemView.findViewById(R.id.notification_user_image);
            notifiedParameter = itemView.findViewById(R.id.notification_parameter);
            notifiedImage = itemView.findViewById(R.id.notification_notified_image);
            notifyUserName = itemView.findViewById(R.id.notification_user_name);
        }
    }
}
