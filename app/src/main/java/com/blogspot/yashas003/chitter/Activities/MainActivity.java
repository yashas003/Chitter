package com.blogspot.yashas003.chitter.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.blogspot.yashas003.chitter.Fragments.AllPostsFragment;
import com.blogspot.yashas003.chitter.Fragments.HomeFragment;
import com.blogspot.yashas003.chitter.Fragments.NotificationFragment;
import com.blogspot.yashas003.chitter.Fragments.ProfileFragment;
import com.blogspot.yashas003.chitter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    String user_id;
    String userName;
    String userImage;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    BottomNavigationView btmNav;
    FragmentManager fm;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()) {
                        case R.id.ic_home:
                            selectedFragment = new HomeFragment();
                            break;

                        case R.id.ic_search:
                            selectedFragment = new AllPostsFragment();
                            break;

                        case R.id.ic_photo:
                            Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                            intent.putExtra("user_name", userName);
                            intent.putExtra("user_image", userImage);
                            startActivity(intent);
                            return false;

                        case R.id.ic_notifications:
                            selectedFragment = new NotificationFragment();
                            break;

                        case R.id.ic_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment).addToBackStack(null).commit();
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        btmNav = findViewById(R.id.bottom_navigation);
        btmNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        fm = getSupportFragmentManager();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {

            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        } else {

            user_id = mAuth.getCurrentUser().getUid();
            firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (task.getResult().exists()) {

                            userName = task.getResult().getString("user_name");
                            userImage = task.getResult().getString("user_image");
                        } else {

                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
