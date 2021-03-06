package com.blogspot.yashas003.chitter.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.Activities.UsersProfileActivity;
import com.blogspot.yashas003.chitter.Model.Users;
import com.blogspot.yashas003.chitter.R;
import com.blogspot.yashas003.chitter.Utils.PicassoCache;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    private List<Users> users_list;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public UserAdapter(List<Users> users_list) {
        this.users_list = users_list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_item, viewGroup, false);
        return new viewHolder(view);
    }

    private static void hideKeyboard(Context context) {

        try {
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if ((((Activity) context).getCurrentFocus() != null) && (((Activity) context).getCurrentFocus().getWindowToken() != null)) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return users_list.size();
    }

    private boolean isOnline(View view) {
        ConnectivityManager cm = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void isFollowing(final String userID, final String firebaseUser, final TextView btnText, final CardView btn) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userID).exists()) {

                    if (userID.equals(firebaseUser)) {
                        btn.setVisibility(View.GONE);
                    } else {
                        btn.setVisibility(View.VISIBLE);
                        btnText.setText("Following");
                    }
                } else {
                    if (userID.equals(firebaseUser)) {
                        btn.setVisibility(View.GONE);
                    } else {
                        btn.setVisibility(View.VISIBLE);
                        btnText.setText("Follow");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addNotification(String user_id) {

        if (!firebaseUser.getUid().equals(user_id)) {

            String date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Notifications")
                    .child(user_id)
                    .child(firebaseUser.getUid());

            HashMap<String, Object> notifyMap = new HashMap<>();
            notifyMap.put("user_id", firebaseUser.getUid());
            notifyMap.put("text", "Started following you.");
            notifyMap.put("post_id", "");
            notifyMap.put("time", date);
            notifyMap.put("is_post", false);

            databaseReference.setValue(notifyMap);
        }
    }

    private void removeNotification(String user_id) {

        databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(user_id).child(firebaseUser.getUid());
        databaseReference.removeValue();
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder viewHolder, int i) {

        final Users users = users_list.get(i);

        viewHolder.userName.setText(users.getUser_name());

        PicassoCache
                .getPicassoInstance(viewHolder.userImage.getContext())
                .load(users.getUser_image())
                .placeholder(R.mipmap.placeholder)
                .into(viewHolder.userImage);

        isFollowing(users.getUser_id(), firebaseUser.getUid(), viewHolder.followBtnText, viewHolder.followBtn);

        if (users.getUnique_name() != null && !users.getUnique_name().trim().isEmpty()) {
            viewHolder.uniqueName.setVisibility(View.VISIBLE);
            viewHolder.uniqueName.setText(users.getUnique_name());
        }

        viewHolder.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(v.getContext());

                if (isOnline(v)) {

                    if (viewHolder.followBtnText.getText().toString().equals("Follow")) {

                        FirebaseDatabase.getInstance().getReference()
                                .child("Follow").child(firebaseUser.getUid())
                                .child("following").child(users.getUser_id())
                                .setValue(true);

                        FirebaseDatabase.getInstance().getReference()
                                .child("Follow").child(users.getUser_id())
                                .child("followers").child(firebaseUser.getUid())
                                .setValue(true);

                        addNotification(users.getUser_id());
                    } else {

                        FirebaseDatabase.getInstance().getReference()
                                .child("Follow").child(firebaseUser.getUid())
                                .child("following").child(users.getUser_id())
                                .removeValue();

                        FirebaseDatabase.getInstance().getReference()
                                .child("Follow").child(users.getUser_id())
                                .child("followers").child(firebaseUser.getUid())
                                .removeValue();

                        removeNotification(users.getUser_id());
                    }
                } else {
                    Toast.makeText(v.getContext(), "You are not connected to the internet :(", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline(v)) {

                    hideKeyboard(v.getContext());
                    Intent userProfile = new Intent(v.getContext(), UsersProfileActivity.class);
                    userProfile.putExtra("user_id", users.getUser_id());
                    v.getContext().startActivity(userProfile);
                } else {
                    Toast.makeText(v.getContext(), "You are not connected to the internet :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class viewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        TextView userName;
        TextView uniqueName;
        CardView followBtn;
        TextView followBtnText;

        viewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.user_name);
            uniqueName = itemView.findViewById(R.id.unique_name);
            followBtn = itemView.findViewById(R.id.btn_follow);
            followBtnText = itemView.findViewById(R.id.btn_follow_text);
        }
    }
}
