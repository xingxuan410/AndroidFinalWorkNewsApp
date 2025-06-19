package com.example.newsapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [FIX] If we are on a large screen (the detail container exists)
        // and it's the initial creation of the activity, we must manually
        // place the detail fragment in its container.
        if (findViewById(R.id.news_detail_container) != null && savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.news_detail_container, new NewsDetailFragment())
                    .commit();
        }
    }
}
