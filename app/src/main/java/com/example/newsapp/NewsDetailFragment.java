// 文件名: com/example/newsapp/NewsDetailFragment.java
package com.example.newsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class NewsDetailFragment extends Fragment {

    private TextView titleTextView;
    private TextView dateTextView;
    private TextView contentTextView;
    private NewsRepository newsRepository;
    private SharedViewModel sharedViewModel;
    private int newsId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsRepository = new NewsRepository(requireActivity().getApplication());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        if (getArguments() != null) {
            newsId = getArguments().getInt("newsId", -1);
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

        boolean isLargeScreen = requireActivity().findViewById(R.id.news_detail_container) != null;

        if (isLargeScreen) {
            // 在大屏幕上，观察来自 SharedViewModel 的选择
            sharedViewModel.getSelectedNewsId().observe(getViewLifecycleOwner(), id -> {
                if (id != null) {
                    loadNewsById(id);
                }
            });
        } else {
            // 在小屏幕上，使用来自 arguments 的 ID
            if (newsId != -1) {
                loadNewsById(newsId);
            }
        }
    }

    private void loadNewsById(int id) {
        newsRepository.getNewsById(id).observe(getViewLifecycleOwner(), this::displayNews);
    }

    public void displayNews(News news) {
        if (news != null) {
            titleTextView.setText(news.getTitle());
            dateTextView.setText(news.getDate());
            contentTextView.setText(news.getContent());
        } else {
            titleTextView.setText("新闻未找到");
            dateTextView.setText("");
            contentTextView.setText("");
        }
    }
}