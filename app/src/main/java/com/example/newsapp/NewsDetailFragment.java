// 文件名: com/example/newsapp/NewsDetailFragment.java
package com.example.newsapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NewsDetailFragment extends Fragment {

    private static final String DB_TAG = "App_DB_Log"; // 统一数据库 TAG

    private int newsId = -1;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView contentTextView;
    private NewsDao newsDao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            newsId = getArguments().getInt("newsId", -1);
        }
        if (getContext() != null) {
            newsDao = NewsDatabase.getDatabase(requireContext()).newsDao();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleTextView = view.findViewById(R.id.text_news_title);
        dateTextView = view.findViewById(R.id.text_news_date);
        contentTextView = view.findViewById(R.id.text_news_content);

        if (newsId != -1) {
            loadNewsById(newsId);
        } else {
            displayNews(null);
        }
    }

    private void loadNewsById(int id) {
        if (newsDao == null) {
            displayNews(null);
            return;
        }
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            // Log.i(DB_TAG, "数据库操作开始: 尝试通过ID获取新闻: " + id + "，线程: " + Thread.currentThread().getName());
            News news = null;
            try {
                news = newsDao.getNewsById(id);
                if (news != null) {
                    Log.i(DB_TAG, "数据库操作成功: 通过ID " + id + " 获取到新闻: " + news.getTitle());
                } else {
                    Log.w(DB_TAG, "数据库操作结果: 未找到ID为 " + id + " 的新闻");
                }
            } catch (Exception e) {
                Log.e(DB_TAG, "数据库操作错误: 通过ID " + id + " 获取新闻失败", e);
            }
            final News resultNews = news;
            if (isAdded() && getActivity() != null) {
                new Handler(Looper.getMainLooper()).post(() -> displayNews(resultNews));
            }
        });
    }

    public void displayNews(News news) {
        if (!isAdded()) {
            return;
        }
        if (news != null) {
            if (titleTextView != null) {
                titleTextView.setText(news.getTitle());
                dateTextView.setText(news.getDate());
                contentTextView.setText(news.getContent());
            }
        } else {
            if (titleTextView != null) {
                titleTextView.setText("新闻未找到");
                dateTextView.setText("");
                contentTextView.setText("");
            }
        }
    }
}