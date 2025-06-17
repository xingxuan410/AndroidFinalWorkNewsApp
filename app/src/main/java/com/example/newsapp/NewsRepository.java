package com.example.newsapp;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class NewsRepository {

    private final NewsDao newsDao;

    public NewsRepository(Application application) {
        NewsDatabase db = NewsDatabase.getDatabase(application);
        this.newsDao = db.newsDao();
    }

    public LiveData<List<News>> getAllNews() {
        return newsDao.getAllNews();
    }

    public LiveData<News> getNewsById(int newsId) {
        return newsDao.getNewsById(newsId);
    }

    public LiveData<List<News>> searchNews(String query) {
        return newsDao.searchNews(query);
    }

    public void insert(News news) {
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            newsDao.insert(news);
        });
    }
}