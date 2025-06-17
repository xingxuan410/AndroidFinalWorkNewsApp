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

        // Transformations.switchMap 会对 searchQuery 的变化做出反应。
        // 如果查询为空，则获取所有新闻。
        // 如果有查询，则执行搜索。
        newsList = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return repository.getAllNews();
            } else {
                // LIKE 的查询语句需要包含 '%'
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
}