package com.blogspot.yashas003.chitter.Adapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.Fragments.UserProfileFragment;
import com.blogspot.yashas003.chitter.Model.Users;
import com.blogspot.yashas003.chitter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    private List<Users> users_list;
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

    @Override
    public void onBindViewHolder(@NonNull final viewHolder viewHolder, int i) {

        final Users users = users_list.get(i);

        viewHolder.userName.setText(users.getUser_name());
        Picasso.get().load(users.getUser_image()).into(viewHolder.userImage);
        isFollowing(users.getUser_id(), firebaseUser.getUid(), viewHolder.followBtnText, viewHolder.followBtn);

        if (users.getUnique_name() != null && !users.getUnique_name().trim().isEmpty()) {
            viewHolder.uniqueName.setVisibility(View.VISIBLE);
            viewHolder.uniqueName.setText(users.getUnique_name());
        }

        viewHolder.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    } else {

                        FirebaseDatabase.getInstance().getReference()
                                .child("Follow").child(firebaseUser.getUid())
                                .child("following").child(users.getUser_id())
                                .removeValue();

                        FirebaseDatabase.getInstance().getReference()
                                .child("Follow").child(users.getUser_id())
                                .child("followers").child(firebaseUser.getUid())
                                .removeValue();
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

                    Bundle bundle = new Bundle();
                    bundle.putString("user_id", users.getUser_id());

                    Fragment userProfile = new UserProfileFragment();
                    userProfile.setArguments(bundle);
                    ((FragmentActivity) v
                            .getContext()).getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.fragment_container, userProfile)
                            .commit();
                } else {
                    Toast.makeText(v.getContext(), "You are not connected to the internet :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users_list.size();
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

    private boolean isOnline(View view) {
        ConnectivityManager cm = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
