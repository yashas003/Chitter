package com.blogspot.yashas003.chitter.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.blogspot.yashas003.chitter.Adapters.UserAdapter;
import com.blogspot.yashas003.chitter.Model.Users;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FollowersActivity extends AppCompatActivity {
    String id;
    String title;
    Toolbar toolbar;
    ProgressBar loader;

    List<String> idList;
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    List<Users> usersList;

    FirebaseFirestore firestore;
    DatabaseReference reference;
    Task<QuerySnapshot> query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        toolbar = findViewById(R.id.followers_toolbar);
        toolbar.setTitleTextAppearance(this, R.style.FollowersToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loader = findViewById(R.id.loader);
        firestore = FirebaseFirestore.getInstance();

        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(usersList);

        recyclerView = findViewById(R.id.followers_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(FollowersActivity.this));
        recyclerView.setAdapter(userAdapter);

        idList = new ArrayList<>();

        switch (title) {
            case "likes":
                getLikes();
                break;

            case "following":
                getFollowing();
                break;

            case "followers":
                getFollowers();
                break;
        }
    }

    private void getLikes() {

        reference = FirebaseDatabase.getInstance().getReference("Likes").child(id);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getFollowing() {

        reference = FirebaseDatabase.getInstance().getReference("Follow").child(id).child("following");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getFollowers() {

        reference = FirebaseDatabase.getInstance().getReference("Follow").child(id).child("followers");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showUsers() {

        query = firestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    usersList.clear();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Users users = doc.toObject(Users.class);

                        for (String id : idList) {
                            if (users.getUser_id().equals(id)) {

                                usersList.add(users);
                            }
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                    loader.setVisibility(View.GONE);
                }
            }
        });
    }
}
