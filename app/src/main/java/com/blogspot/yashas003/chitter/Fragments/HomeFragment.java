package com.blogspot.yashas003.chitter.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.blogspot.yashas003.chitter.Adapters.PostAdapter;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class HomeFragment extends Fragment {
    Menu menu;
    MenuItem menuItem;
    BottomNavigationView bottomNavigationView;

    String mUser;
    Toolbar toolbar;
    ProgressBar loader;
    ProgressBar loadingMore;
    ConstraintLayout welcome;

    List<Posts> post_list;
    PostAdapter postAdapter;
    RecyclerView postListView;
    List<String> following_list;
    LinearLayoutManager layoutManager;

    Query firstQuery;
    FirebaseAuth mAuth;
    DatabaseReference mreference;
    FirebaseFirestore mfirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.findItem(R.id.ic_home);
        if (!menuItem.isChecked()) {
            menuItem.setChecked(true);
        }

        toolbar = view.findViewById(R.id.home_toolbar);
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        toolbar.setTitleTextAppearance(getActivity(), R.style.ToolBarFont);
        activity.setSupportActionBar(toolbar);

        loadingMore = view.findViewById(R.id.loading_more);
        welcome = view.findViewById(R.id.welcome);
        loader = view.findViewById(R.id.home_progress);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser().getUid();
        mfirestore = FirebaseFirestore.getInstance();

        post_list = new ArrayList<>();
        postAdapter = new PostAdapter(post_list, getContext());
        layoutManager = new LinearLayoutManager(getActivity());

        postListView = view.findViewById(R.id.post_list_view);
        postListView.setLayoutManager(layoutManager);
        postListView.setAdapter(postAdapter);
        postListView.setItemViewCacheSize(30);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        following_list = new ArrayList<>();
        mreference = FirebaseDatabase.getInstance().getReference("Follow").child(mUser).child("following");
        mreference.keepSynced(true);
        mreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                following_list.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    following_list.add(snapshot.getKey());
                }
                readPosts();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void readPosts() {

        firstQuery = mfirestore.collection("Posts").orderBy("time", Query.Direction.DESCENDING);
        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(TAG, "onEvent: ", e);
                } else {
                    post_list.clear();

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            Posts posts = doc.getDocument().toObject(Posts.class);
                            for (String id : following_list) {

                                if (posts.getUser_id().equals(id)) {
                                    post_list.add(posts);
                                    loader.setVisibility(View.GONE);
                                }
                            }
                            if (posts.getUser_id().equals(mUser)) {
                                post_list.add(posts);
                                loader.setVisibility(View.GONE);
                            }
                            postListView.setVisibility(View.VISIBLE);
                            postAdapter.notifyDataSetChanged();
                        }
                    }
                    if (post_list.isEmpty()) {
                        loader.setVisibility(View.GONE);
                        welcome.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
}
