package com.blogspot.yashas003.chitter.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.R;

public class UserProfileFragment extends Fragment {
    BottomNavigationView bottomNavigationView;
    Menu menu;
    MenuItem menuItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        menu = bottomNavigationView.getMenu();
        menuItem = menu.findItem(R.id.ic_search);
        if (!menuItem.isChecked()) {
            menuItem.setChecked(true);
        }

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            String user_ID = bundle.getString("user_id");
            Toast.makeText(getActivity(), user_ID, Toast.LENGTH_SHORT).show();
        }
        return view;
    }
}
