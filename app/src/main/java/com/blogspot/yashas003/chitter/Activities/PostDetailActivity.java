package com.blogspot.yashas003.chitter.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.blogspot.yashas003.chitter.Adapters.SinglePostAdapter;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class PostDetailActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView detailRecyclerView;
    ProgressBar loader;

    String postId;
    SinglePostAdapter singlePostAdapter;
    List<Posts> postsList;

    ListenerRegistration query;
    FirebaseFirestore mfirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        Intent intent = getIntent();
        postId = intent.getStringExtra("post_id");

        toolbar = findViewById(R.id.detail_post_toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolBarFont);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loader = findViewById(R.id.detail_post_loader);
        mfirestore = FirebaseFirestore.getInstance();

        postsList = new ArrayList<>();
        singlePostAdapter = new SinglePostAdapter(this, postsList);

        detailRecyclerView = findViewById(R.id.detail_post_recyclerView);
        detailRecyclerView.setHasFixedSize(true);
        detailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        detailRecyclerView.setAdapter(singlePostAdapter);

        readPosts();
    }

    private void readPosts() {

        query = mfirestore.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(TAG, "onEvent: ", e);
                } else {
                    postsList.clear();

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            Posts posts = doc.getDocument().toObject(Posts.class);

                            if (posts.getPost_id().equals(postId)) {
                                postsList.add(posts);
                                detailRecyclerView.setVisibility(View.VISIBLE);
                                loader.setVisibility(View.GONE);
                            }
                            singlePostAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}
