package com.blogspot.yashas003.chitter.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.yashas003.chitter.R;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {
    BottomNavigationView bottomNavigationView;
    MenuItem menuItem;
    Menu menu;
    Toolbar toolbar;
    FirebaseAuth mAuth;

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
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        toolbar.setTitleTextAppearance(getActivity(), R.style.ToolBarFont);
        activity.setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        return view;
    }
}
