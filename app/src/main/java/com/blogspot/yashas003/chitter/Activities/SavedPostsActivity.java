package com.blogspot.yashas003.chitter.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blogspot.yashas003.chitter.Adapters.GridViewAdapter;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class SavedPostsActivity extends AppCompatActivity {
    GridViewAdapter gridViewAdapter;
    ArrayList<Posts> savedPost = new ArrayList<>();
    RecyclerView savedListView;
    List<String> savedPostList;
    TextView noSavedPosts;
    ProgressBar spinner;
    Toolbar toolbar;
    String user_id;

    FirebaseFirestoreSettings firestoreSettings;
    DatabaseReference mreference;
    FirebaseFirestore mfirestore;
    FirebaseUser mfirebaseUser;
    FirebaseAuth mAuth;
    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        toolbar = findViewById(R.id.saved_posts_toolbar);
        toolbar.setTitleTextAppearance(SavedPostsActivity.this, R.style.ToolBarFont);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        spinner = findViewById(R.id.loading_spinner);
        noSavedPosts = findViewById(R.id.no_saved_posts);

        firestoreSettings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();

        mAuth = FirebaseAuth.getInstance();
        mfirebaseUser = mAuth.getCurrentUser();
        mfirestore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        savedListView = findViewById(R.id.saved_post_list);
        savedListView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        gridViewAdapter = new GridViewAdapter(savedPost);
        savedListView.setAdapter(gridViewAdapter);

        getSavedPostList();
    }

    private void getSavedPostList() {

        savedPostList = new ArrayList<>();
        mreference = FirebaseDatabase.getInstance().getReference("Saves").child(mfirebaseUser.getUid());
        mreference.keepSynced(true);
        mreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    savedPostList.add(snapshot.getKey());
                }
                showSavedPost();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showSavedPost() {

        query = mfirestore.collection("Posts").orderBy("time", Query.Direction.DESCENDING);
        mfirestore.setFirestoreSettings(firestoreSettings);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(TAG, "onEvent: ", e);
                } else {
                    savedPost.clear();
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        Posts posts = doc.getDocument().toObject(Posts.class);

                        for (String postIdList : savedPostList) {
                            if (posts.getPost_id().equals(postIdList)) {
                                savedPost.add(posts);
                                savedListView.setVisibility(View.VISIBLE);
                            }
                        }
                        Collections.reverse(savedPost);
                        spinner.setVisibility(View.GONE);
                        gridViewAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
