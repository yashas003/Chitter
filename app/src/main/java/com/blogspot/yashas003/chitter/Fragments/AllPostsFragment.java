package com.blogspot.yashas003.chitter.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blogspot.yashas003.chitter.Adapters.GridViewAdapter;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.support.constraint.Constraints.TAG;

public class AllPostsFragment extends Fragment {
    Menu menu;
    TextView search;
    MenuItem menuItem;
    ProgressBar loader;
    BottomNavigationView bottomNavigationView;

    List<Posts> postsList;
    RecyclerView post_list_view;
    GridViewAdapter gridViewAdapter;
    StaggeredGridLayoutManager gridLayoutManager;

    FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_posts, container, false);

        bottomNavigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.findItem(R.id.ic_search);
        if (!menuItem.isChecked()) {
            menuItem.setChecked(true);
        }

        search = view.findViewById(R.id.search_frag_bar);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SearchFragment()).addToBackStack(null).commit();
            }
        });

        loader = view.findViewById(R.id.progress_bar);
        firestore = FirebaseFirestore.getInstance();

        postsList = new ArrayList<>();
        gridViewAdapter = new GridViewAdapter(postsList);
        gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);

        post_list_view = view.findViewById(R.id.all_post_list_view);
        post_list_view.setLayoutManager(gridLayoutManager);
        post_list_view.setAdapter(gridViewAdapter);
        post_list_view.setItemViewCacheSize(30);

        getPosts();
        return view;
    }

    private void getPosts() {

        firestore.collection("Posts").orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.e(TAG, "onEvent: ", e);
                        } else {
                            postsList.clear();
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Posts posts = doc.getDocument().toObject(Posts.class);
                                    postsList.add(posts);
                                }
                            }
                            loader.setVisibility(View.GONE);
                            gridViewAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}