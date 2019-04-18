package com.blogspot.yashas003.chitter.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.yashas003.chitter.Adapters.NotificationAdapter;
import com.blogspot.yashas003.chitter.Model.Notifications;
import com.blogspot.yashas003.chitter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NotificationFragment extends Fragment {
    Menu menu;
    MenuItem menuItem;
    BottomNavigationView bottomNavigationView;

    Toolbar toolbar;
    RecyclerView notifyRecycler;
    List<Notifications> notificationsList;
    NotificationAdapter notificationAdapter;

    DatabaseReference reference;

    FirebaseUser firebaseUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        bottomNavigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.findItem(R.id.ic_notifications);
        if (!menuItem.isChecked()) {
            menuItem.setChecked(true);
        }

        toolbar = view.findViewById(R.id.notification_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        toolbar.setTitleTextAppearance(getActivity(), R.style.ToolBarFont);
        activity.setSupportActionBar(toolbar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        notificationsList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationsList);

        notifyRecycler = view.findViewById(R.id.notification_recyclerView);
        notifyRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        notifyRecycler.setAdapter(notificationAdapter);
        notifyRecycler.setHasFixedSize(true);

        readNotification();
        return view;
    }

    private void readNotification() {

        notificationsList.clear();
        reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notifications notifications = snapshot.getValue(Notifications.class);
                    notificationsList.add(notifications);
                }
                Collections.reverse(notificationsList);
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
