package com.blogspot.yashas003.chitter.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.yashas003.chitter.R;
import com.varunest.sparkbutton.SparkButton;

public class SavedPostsActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView savedPostText;
    SparkButton sparkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        toolbar = findViewById(R.id.saved_posts_toolbar);
        toolbar.setTitleTextAppearance(SavedPostsActivity.this, R.style.ToolBarFont);
        setSupportActionBar(toolbar);

        savedPostText = findViewById(R.id.saved_post_text);
        savedPostText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SavedPostsActivity.this, "Saved Post Activity", Toast.LENGTH_SHORT).show();
            }
        });

        sparkButton = findViewById(R.id.spark);
        sparkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sparkButton.playAnimation();
                sparkButton.setChecked(true);
            }
        });
    }
}
