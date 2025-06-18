package com.example.newsapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView; // <-- 【新增】导入
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class NewsDetailFragment extends Fragment {

    // 声明所有视图控件
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView contentTextView;
    private Button readMoreButton;
    private WebView webView;
    private NestedScrollView summaryScrollView; // <-- 【新增】声明摘要滚动视图

    private NewsRepository newsRepository;
    private SharedViewModel sharedViewModel;
    private int newsId = -1;
    private String articleUrl = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // ... onCreate 方法保持不变 ...
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

    @SuppressLint("SetJavaScriptEnabled") // 将注解移到方法上，覆盖整个方法
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 获取所有视图控件的引用
        summaryScrollView = view.findViewById(R.id.summary_scroll_view); // <-- 【新增】获取摘要滚动视图
        titleTextView = view.findViewById(R.id.text_news_title);
        dateTextView = view.findViewById(R.id.text_news_date);
        contentTextView = view.findViewById(R.id.text_news_content);
        readMoreButton = view.findViewById(R.id.button_read_full_article);
        webView = view.findViewById(R.id.fullscreen_webView);
        contentTextView.setMovementMethod(new ScrollingMovementMethod());
        // ... 大小屏幕判断逻辑保持不变 ...
        boolean isLargeScreen = requireActivity().findViewById(R.id.news_detail_container) != null;
        if (isLargeScreen) {
            sharedViewModel.getSelectedNewsId().observe(getViewLifecycleOwner(), id -> {
                if (id != null) {
                    loadNewsById(id);
                }
            });
        } else {
            if (newsId != -1) {
                loadNewsById(newsId);
            }
        }

        // 【修改】按钮点击逻辑
        readMoreButton.setOnClickListener(v -> {
            // URL检查保持不变
            if (articleUrl != null && !articleUrl.isEmpty()) {
                // 【修改】隐藏整个摘要滚动视图
                summaryScrollView.setVisibility(View.GONE);

                // 显示并配置WebView
                webView.setVisibility(View.VISIBLE);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl(articleUrl);
            }
        });
    }

    private void loadNewsById(int id) {
        newsRepository.getNewsById(id).observe(getViewLifecycleOwner(), this::displayNews);
    }

    public void displayNews(News news) {
        resetUIState();

        if (news != null) {
            titleTextView.setText(news.getTitle());
            dateTextView.setText(news.getDate());
            contentTextView.setText(news.getContent());

            this.articleUrl = news.getUrl();

            // 【关键修复】根据URL是否存在来决定按钮的可见性
            if (this.articleUrl != null && !this.articleUrl.isEmpty()) {
                readMoreButton.setVisibility(View.VISIBLE);
            } else {
                readMoreButton.setVisibility(View.GONE);
            }

        } else {
            titleTextView.setText(R.string.news_not_found);
            dateTextView.setText("");
            contentTextView.setText("");
            readMoreButton.setVisibility(View.GONE); // 新闻未找到时也隐藏按钮
            this.articleUrl = "";
        }
    }

    // 【修改】更新UI重置方法
    private void resetUIState() {
        // 显示摘要滚动视图，隐藏WebView
        summaryScrollView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        webView.loadUrl("about:blank"); // 清空WebView
    }
}