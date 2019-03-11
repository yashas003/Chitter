package com.blogspot.yashas003.chitter.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.yashas003.chitter.Adapters.UserAdapter;
import com.blogspot.yashas003.chitter.Model.Users;
import com.blogspot.yashas003.chitter.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class SearchFragment extends Fragment {
    BottomNavigationView bottomNavigationView;
    Menu menu;
    MenuItem menuItem;
    RecyclerView users_list_view;
    UserAdapter userAdapter;
    List<Users> users_list;

    FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.findItem(R.id.ic_search);
        if (!menuItem.isChecked()) {
            menuItem.setChecked(true);
        }

        users_list = new ArrayList<>();
        userAdapter = new UserAdapter(users_list);

        users_list_view = view.findViewById(R.id.recycler_view);
        users_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        users_list_view.setAdapter(userAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.d(TAG, "Error:" + e.getMessage());
                } else {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            Users users = doc.getDocument().toObject(Users.class);
                            users_list.add(users);
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
        return view;
    }
}
