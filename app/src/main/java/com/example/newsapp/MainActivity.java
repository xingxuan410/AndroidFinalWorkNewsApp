package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements NewsListFragment.OnNewsSelectedListener {

    private boolean isLargeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 判断是否为大屏幕模式（通过布局中是否存在news_detail_container来判断）
        isLargeScreen = findViewById(R.id.news_detail_container) != null;
    }

    @Override
    public void onNewsSelected(News news) {
        // 只有大屏幕模式会调用此方法
        if (isLargeScreen) {
            NewsDetailFragment detailFragment =
                    (NewsDetailFragment) getSupportFragmentManager().findFragmentById(R.id.news_detail_container);
            if (detailFragment != null) {
                // 更新已存在的DetailFragment
                detailFragment.displayNews(news);
            }
        }
    }
}