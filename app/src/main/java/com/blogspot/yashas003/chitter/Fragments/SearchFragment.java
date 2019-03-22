package com.blogspot.yashas003.chitter.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.blogspot.yashas003.chitter.Adapters.UserAdapter;
import com.blogspot.yashas003.chitter.Model.Users;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    BottomNavigationView bottomNavigationView;
    Menu menu;
    MenuItem menuItem;

    EditText searchBar;

    RecyclerView users_list_view;
    UserAdapter userAdapter;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings firestoreSettings;
    CollectionReference reference = firestore.collection("Users");
    Query query;

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

        searchBar = view.findViewById(R.id.search_bar);

        firestoreSettings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();

        users_list_view = view.findViewById(R.id.recycler_view);
        users_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Show all users when fragment created
        query = firestore.collection("Users");
        firestore.setFirestoreSettings(firestoreSettings);
        showAdapter(query);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().length() == 0) {

                    // Show all users when search bar character null
                    query = firestore.collection("Users");
                    firestore.setFirestoreSettings(firestoreSettings);
                    showAdapter(query);
                } else {

                    // Show only the users according to the entered name
                    query = reference.orderBy("user_name").startAt(s.toString().trim()).endAt(s.toString().trim() + "\uf8ff");
                    showAdapter(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return view;
    }

    private void showAdapter(final Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                List<Users> users_list = new ArrayList<>();

                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Users users = doc.toObject(Users.class);
                        users_list.add(users);
                    }
                    userAdapter = new UserAdapter(users_list);
                    users_list_view.setAdapter(userAdapter);
                }
                userAdapter.notifyDataSetChanged();
            }
        });
    }
}
