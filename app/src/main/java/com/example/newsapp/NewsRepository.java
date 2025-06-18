// 文件路径: com/example/newsapp/NewsRepository.java
package com.example.newsapp;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;

import com.example.newsapp.api.Article;
import com.example.newsapp.api.NewsApiResponse;
import com.example.newsapp.api.NewsApiService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsRepository {

    private static final String GNEWS_BASE_URL = "https://gnews.io/";
    private final NewsDao newsDao;
    private final NewsApiService newsApiService;

    public NewsRepository(Application application) {
        NewsDatabase db = NewsDatabase.getDatabase(application);
        this.newsDao = db.newsDao();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GNEWS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.newsApiService = retrofit.create(NewsApiService.class);
    }

    public LiveData<List<News>> getAllNews() { return newsDao.getAllNews(); }
    public LiveData<News> getNewsById(int newsId) { return newsDao.getNewsById(newsId); }
    public LiveData<List<News>> searchNews(String query) { return newsDao.searchNews(query); }

    // [NEW]
    public LiveData<List<News>> getFavoriteNews() {
        return newsDao.getFavoriteNews();
    }

    // [NEW]
    public void updateFavoriteStatus(List<Integer> newsIds, boolean isFavorite) {
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            newsDao.updateFavoriteStatus(newsIds, isFavorite);
        });
    }

    // [NEW]
    public void deleteNewsByIds(List<Integer> newsIds) {
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            newsDao.deleteNewsByIds(newsIds);
        });
    }

    public void insert(News news) {
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            newsDao.insert(news);
        });
    }

    public void refreshNews(String apiKey) {
        newsApiService.getTopHeadlines("general", "zh", "cn", apiKey).enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                if (!response.isSuccessful()) {
                    String errorString = "Unknown error";
                    if (response.errorBody() != null) {
                        try {
                            errorString = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e("NewsRepository", "Error reading error body", e);
                        }
                    }
                    Log.e("NewsRepository", "Failed to get news from GNews, code: " + response.code() + ", details: " + errorString);
                    return;
                }

                if (response.body() != null && response.body().getArticles() != null) {
                    List<Article> articles = response.body().getArticles();
                    NewsDatabase.databaseWriteExecutor.execute(() -> {
                        int articlesToProcess = Math.min(articles.size(), 20);

                        for (int i = 0; i < articlesToProcess; i++) {
                            Article article = articles.get(i);
                            if (article.getTitle() == null || article.getUrl() == null) {
                                continue;
                            }
                            String title = article.getTitle();
                            String summary = (article.getDescription() != null && !article.getDescription().isEmpty()) ? article.getDescription() : "没有概要";
                            String date = (article.getPublishedAt() != null && article.getPublishedAt().length() >= 10) ? article.getPublishedAt().substring(0, 10) : "没有日期";
                            String content = (article.getContent() != null && !article.getContent().isEmpty()) ? article.getContent().split(" \\[\\d+ chars\\]")[0] : summary;
                            String url = article.getUrl();

                            // Using constructor with favorite status (defaulting to false)
                            News news = new News(title, summary, content, date, url, false);
                            newsDao.insert(news);
                        }
                        Log.i("NewsRepository", "Successfully refreshed and stored " + articlesToProcess + " news articles.");
                    });
                }
            }

            @Override
            public void onFailure(Call<NewsApiResponse> call, Throwable t) {
                Log.e("NewsRepository", "Network request failed", t);
            }
        });
    }
}