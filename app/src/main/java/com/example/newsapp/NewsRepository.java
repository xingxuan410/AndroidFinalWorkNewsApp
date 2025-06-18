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

    // 【优化建议】将API URL的基础部分定义得更准确，路径部分留给ApiService接口
    private static final String GNEWS_BASE_URL = "https://gnews.io/";
    private final NewsDao newsDao;
    private final NewsApiService newsApiService;

    public NewsRepository(Application application) {
        NewsDatabase db = NewsDatabase.getDatabase(application);
        this.newsDao = db.newsDao();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GNEWS_BASE_URL) // 使用修正后的Base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.newsApiService = retrofit.create(NewsApiService.class);
    }

    // ... 其他方法保持不变 ...
    public LiveData<List<News>> getAllNews() { return newsDao.getAllNews(); }
    public LiveData<News> getNewsById(int newsId) { return newsDao.getNewsById(newsId); }
    public LiveData<List<News>> searchNews(String query) { return newsDao.searchNews(query); }
    public void insert(News news) {
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            newsDao.insert(news);
        });
    }


    public void refreshNews(String apiKey) {
        // 这里的调用是正确的
        newsApiService.getTopHeadlines("general", "zh", "cn", apiKey).enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                if (!response.isSuccessful()) {
                    // ... 你的错误处理代码很好，保持不变 ...
                    String errorString = "未知错误";
                    if (response.errorBody() != null) {
                        try {
                            errorString = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e("NewsRepository", "读取ErrorBody时出错", e);
                        }
                    }
                    Log.e("NewsRepository", "从 GNews 获取新闻失败，响应码: " + response.code() + ", 错误详情: " + errorString);
                    return;
                }

                if (response.body() != null && response.body().getArticles() != null) {
                    List<Article> articles = response.body().getArticles();
                    NewsDatabase.databaseWriteExecutor.execute(() -> {
                        // 为了防止重复插入，最好先清空旧数据
                        // newsDao.deleteAll(); // 如果你希望每次刷新都是全新的新闻列表，可以取消这行注释

                        int articlesToProcess = Math.min(articles.size(), 20); // 可以适当增加处理的文章数量

                        for (int i = 0; i < articlesToProcess; i++) {
                            Article article = articles.get(i);

                            // 检查必要字段是否为null，防止崩溃
                            if (article.getTitle() == null || article.getUrl() == null) {
                                continue; // 如果文章没有标题或URL，跳过这条新闻
                            }

                            // 你的字段处理逻辑很好，我们在这里加上url
                            String title = article.getTitle();
                            String summary = (article.getDescription() != null && !article.getDescription().isEmpty()) ? article.getDescription() : "没有概要";
                            String date = (article.getPublishedAt() != null && article.getPublishedAt().length() >= 10) ? article.getPublishedAt().substring(0, 10) : "没有日期";
                            String content = (article.getContent() != null && !article.getContent().isEmpty()) ? article.getContent().split(" \\[\\d+ chars\\]")[0] : summary; // 移除 "[... chars]"
                            String url = article.getUrl(); // <-- 【关键修复】获取URL

                            // 【关键修复】调用包含5个参数的构造函数，把url也传进去
                            News news = new News(title, summary, content, date, url);

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