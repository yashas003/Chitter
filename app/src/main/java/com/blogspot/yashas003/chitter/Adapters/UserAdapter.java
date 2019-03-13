package com.blogspot.yashas003.chitter.Adapters;

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

import com.blogspot.yashas003.chitter.Fragments.UserProfileFragment;
import com.blogspot.yashas003.chitter.Model.Users;
import com.blogspot.yashas003.chitter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    private List<Users> users_list;

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
    public void onBindViewHolder(@NonNull viewHolder viewHolder, int i) {

        final Users users = users_list.get(i);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        viewHolder.userName.setText(users.getUser_name());
        Picasso.get().load(users.getUser_image()).into(viewHolder.userImage);

        if (users.getUnique_name() != null && !users.getUnique_name().trim().isEmpty()) {
            viewHolder.uniqueName.setVisibility(View.VISIBLE);
            viewHolder.uniqueName.setText(users.getUnique_name());
        }

        if (users.getUser_id().equals(firebaseUser.getUid())) {
            viewHolder.followBtn.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
}
