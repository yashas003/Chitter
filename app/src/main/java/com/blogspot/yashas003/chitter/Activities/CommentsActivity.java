package com.blogspot.yashas003.chitter.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.Adapters.CommentAdapter;
import com.blogspot.yashas003.chitter.Model.Comments;
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
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText commentText;
    ProgressBar spinner;
    FloatingActionButton fab;
    RecyclerView recyclerView;

    CommentAdapter commentAdapter;
    List<Comments> commentsList;

    String postId;
    String ownerId;

    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        Intent intent = getIntent();
        postId = intent.getStringExtra("post_id");
        ownerId = intent.getStringExtra("owner_id");

        toolbar = findViewById(R.id.comments_toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolBarFont);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        commentText = findViewById(R.id.add_comment);
        fab = findViewById(R.id.send_comment);
        spinner = findViewById(R.id.comments_loader);

        commentsList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentsList);

        recyclerView = findViewById(R.id.comment_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
        recyclerView.setAdapter(commentAdapter);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (commentText.getText().toString().equals("")) {
                    Toast.makeText(CommentsActivity.this, "Comment cannot be empty :(", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });
        readComment();
    }

    private void addComment() {

        reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", commentText.getText().toString());
        hashMap.put("owner", firebaseUser.getUid());

        reference.push().setValue(hashMap);
        commentText.setText("");
    }

    private void readComment() {

        reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                commentsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comments comments = snapshot.getValue(Comments.class);
                    commentsList.add(comments);
                }
                spinner.setVisibility(View.GONE);
                Collections.reverse(commentsList);
                commentAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
