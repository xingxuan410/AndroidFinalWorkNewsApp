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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class NewsDetailFragment extends Fragment {

    private TextView titleTextView, dateTextView, contentTextView;
    private Button readMoreButton;
    private WebView webView;
    private NestedScrollView summaryScrollView;

    private NewsRepository newsRepository;
    private SharedViewModel sharedViewModel;
    private int newsId = -1;
    private String articleUrl = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsRepository = new NewsRepository(requireActivity().getApplication());
        // Use requireActivity() to scope the ViewModel to the Activity
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Get newsId from arguments if passed (for small screen navigation)
        if (getArguments() != null) {
            newsId = getArguments().getInt("newsId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_detail, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        summaryScrollView = view.findViewById(R.id.summary_scroll_view);
        titleTextView = view.findViewById(R.id.text_news_title);
        dateTextView = view.findViewById(R.id.text_news_date);
        contentTextView = view.findViewById(R.id.text_news_content);
        readMoreButton = view.findViewById(R.id.button_read_full_article);
        webView = view.findViewById(R.id.fullscreen_webView);
        contentTextView.setMovementMethod(new ScrollingMovementMethod());

        // On any screen, observe the shared view model. This is the primary way
        // the detail fragment gets updated on large screens.
        sharedViewModel.getSelectedNewsId().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                loadNewsById(id);
            }
        });

        // If we received a newsId via arguments (from small screen nav), load it.
        // This is a fallback and the observer above is the primary mechanism.
        if (newsId != -1) {
            loadNewsById(newsId);
        }

        readMoreButton.setOnClickListener(v -> {
            if (articleUrl != null && !articleUrl.isEmpty()) {
                summaryScrollView.setVisibility(View.GONE);
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
            readMoreButton.setVisibility(this.articleUrl != null && !this.articleUrl.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            titleTextView.setText(R.string.news_not_found);
            dateTextView.setText("");
            contentTextView.setText("");
            readMoreButton.setVisibility(View.GONE);
            this.articleUrl = "";
        }
    }

    private void resetUIState() {
        summaryScrollView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        webView.loadUrl("about:blank");
    }
}
