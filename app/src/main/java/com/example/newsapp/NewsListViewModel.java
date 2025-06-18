package com.example.newsapp;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

public class NewsListViewModel extends AndroidViewModel {

    private final NewsRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public final LiveData<List<News>> newsList;

    public NewsListViewModel(@NonNull Application application) {
        super(application);
        repository = new NewsRepository(application);

        newsList = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return repository.getAllNews();
            } else {
                return repository.searchNews("%" + query.trim() + "%");
            }
        });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void insert(News news) {
        repository.insert(news);
    }

    // 新增：触发从 API 刷新新闻的方法
    public void refreshNewsFromApi() {
        // !!!重要!!! 请在这里替换为你自己的 NewsAPI 密钥
        String apiKey = "05c1e09246546b3c6d069dfd383fe775";
        repository.refreshNews(apiKey);
    }
}
