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

    private static final String NEWS_API_URL = "https://gnews.io/api/";
    private final NewsDao newsDao;
    private final NewsApiService newsApiService;

    public NewsRepository(Application application) {
        NewsDatabase db = NewsDatabase.getDatabase(application);
        this.newsDao = db.newsDao();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NEWS_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.newsApiService = retrofit.create(NewsApiService.class);
    }

    public LiveData<List<News>> getAllNews() { return newsDao.getAllNews(); }
    public LiveData<News> getNewsById(int newsId) { return newsDao.getNewsById(newsId); }
    public LiveData<List<News>> searchNews(String query) { return newsDao.searchNews(query); }
    public void insert(News news) {
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            newsDao.insert(news);
        });
    }

    public void refreshNews(String apiKey) {
        // 修正点：将第一个参数从 "headlines" 改为 "general" (一个有效的 category)
        newsApiService.getTopHeadlines("general", "zh", "cn", apiKey).enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                // 修正点：改进错误处理和日志记录
                if (!response.isSuccessful()) {
                    String errorString = "未知错误";
                    if (response.errorBody() != null) {
                        try {
                            // 读取并显示具体的错误信息
                            errorString = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e("NewsRepository", "读取ErrorBody时出错", e);
                        }
                    }
                    Log.e("NewsRepository", "从 GNews 获取新闻失败，响应码: " + response.code() + ", 错误详情: " + errorString);
                    return; // 提前返回，不再执行后续代码
                }

                if (response.body() != null && response.body().getArticles() != null) {
                    List<Article> articles = response.body().getArticles();
                    NewsDatabase.databaseWriteExecutor.execute(() -> {
                        int articlesToProcess = Math.min(articles.size(), 5);

                        for (int i = 0; i < articlesToProcess; i++) {
                            Article article = articles.get(i);

                            String title = (article.getTitle() != null && !article.getTitle().isEmpty()) ? article.getTitle() : "没有标题";
                            String summary = (article.getDescription() != null && !article.getDescription().isEmpty()) ? article.getDescription() : "没有概要";
                            String date = (article.getPublishedAt() != null && article.getPublishedAt().length() >= 10) ? article.getPublishedAt().substring(0, 10) : "没有日期";
                            String content = (article.getContent() != null && !article.getContent().isEmpty()) ? article.getContent() : summary;

                            News news = new News(title, summary, content, date);
                            newsDao.insert(news);
                        }
                        Log.i("NewsRepository", "已从 GNews 成功刷新 " + articlesToProcess + " 条新闻并存入数据库。");
                    });
                }
            }

            @Override
            public void onFailure(Call<NewsApiResponse> call, Throwable t) {
                Log.e("NewsRepository", "网络请求失败", t);
            }
        });
    }
}