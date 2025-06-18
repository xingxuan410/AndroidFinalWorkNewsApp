package com.example.newsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class NewsDetailFragment extends Fragment {

    // 【修改】声明新增的视图控件
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView contentTextView;
    private Button readMoreButton;
    private WebView webView;

    private NewsRepository newsRepository;
    private SharedViewModel sharedViewModel;
    private int newsId = -1;
    private String articleUrl = "";
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

        // 【修改】获取所有视图控件的引用，包括新增的
        titleTextView = view.findViewById(R.id.text_news_title);
        dateTextView = view.findViewById(R.id.text_news_date);
        contentTextView = view.findViewById(R.id.text_news_content);
        readMoreButton = view.findViewById(R.id.button_read_full_article); // <-- 【新增】获取按钮
        webView = view.findViewById(R.id.webView_full_article);           // <-- 【新增】获取WebView

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

        // 【新增】设置按钮的点击监听器
        readMoreButton.setOnClickListener(v -> {
            // 检查URL是否有效
            if (articleUrl != null && !articleUrl.isEmpty()) {
                // 隐藏摘要和按钮
                contentTextView.setVisibility(View.GONE);
                readMoreButton.setVisibility(View.GONE);

                // 显示并配置WebView
                webView.setVisibility(View.VISIBLE);

                // 启用JavaScript，很多现代网页需要它
                webView.getSettings().setJavaScriptEnabled(true);
                // 设置WebViewClient，这样链接会在你的WebView中打开，而不是跳到外部浏览器
                webView.setWebViewClient(new WebViewClient());

                // 加载完整新闻的URL
                webView.loadUrl(articleUrl);
            }
        });
    }

    private void loadNewsById(int id) {
        newsRepository.getNewsById(id).observe(getViewLifecycleOwner(), this::displayNews);
    }

    public void displayNews(News news) {
        // 【修改】当显示新闻时，重置UI状态
        resetUIState();

        if (news != null) {
            titleTextView.setText(news.getTitle());
            dateTextView.setText(news.getDate());
            contentTextView.setText(news.getContent());

            // 【新增】保存新闻的URL，并确保按钮可见
            this.articleUrl = news.getUrl(); // <-- 假设你的News类有 getUrl() 方法
            readMoreButton.setVisibility(View.VISIBLE);

        } else {
            titleTextView.setText(R.string.news_not_found); // <-- 使用字符串资源
            dateTextView.setText("");
            contentTextView.setText("");

            // 【新增】如果新闻未找到，隐藏按钮
            readMoreButton.setVisibility(View.GONE);
            this.articleUrl = "";
        }
    }

    // 【新增】一个辅助方法，用于重置UI状态
    // 当加载新新闻时（尤其是在平板上切换新闻时），确保WebView被隐藏，摘要被显示
    private void resetUIState() {
        contentTextView.setVisibility(View.VISIBLE);
        readMoreButton.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        webView.loadUrl("about:blank"); // 停止加载并清空WebView，防止旧内容闪现
    }
}