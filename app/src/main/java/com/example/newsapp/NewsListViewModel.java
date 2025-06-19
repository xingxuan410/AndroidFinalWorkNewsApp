// 文件: com/example/newsapp/NewsListViewModel.java
package com.example.newsapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewsListViewModel extends AndroidViewModel {

    private final NewsRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    public final LiveData<List<News>> newsList;

    // [NEW] LiveData for managing selection mode UI
    private final MutableLiveData<Boolean> _isSelectionMode = new MutableLiveData<>(false);
    public final LiveData<Boolean> isSelectionMode = _isSelectionMode;

    private final MutableLiveData<Set<Integer>> _selectedNewsIds = new MutableLiveData<>(new HashSet<>());
    public final LiveData<Set<Integer>> selectedNewsIds = _selectedNewsIds;

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

    // [NEW] Methods to manage selection state
    public void toggleSelection(int newsId) {
        Set<Integer> currentSelection = _selectedNewsIds.getValue();
        if (currentSelection == null) currentSelection = new HashSet<>();

        if (currentSelection.contains(newsId)) {
            currentSelection.remove(newsId);
        } else {
            currentSelection.add(newsId);
        }
        _selectedNewsIds.setValue(currentSelection);

        // If the last item is deselected, exit selection mode
        if (currentSelection.isEmpty()) {
            _isSelectionMode.setValue(false);
        }
    }

    public void startSelectionMode(int newsId) {
        _isSelectionMode.setValue(true);
        toggleSelection(newsId);
    }

    public void clearSelection() {
        _selectedNewsIds.setValue(new HashSet<>());
        _isSelectionMode.setValue(false);
    }

    // [NEW] Methods for actions on selected items
    public void deleteSelectedNews() {
        Set<Integer> ids = _selectedNewsIds.getValue();
        if (ids != null && !ids.isEmpty()) {
            repository.deleteNewsByIds(new ArrayList<>(ids));
            clearSelection();
        }
    }

    public void favoriteSelectedNews() {
        Set<Integer> ids = _selectedNewsIds.getValue();
        if (ids != null && !ids.isEmpty()) {
            repository.updateFavoriteStatus(new ArrayList<>(ids), true);
            clearSelection();
        }
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void insert(News news) {
        repository.insert(news);
    }

    public void refreshNewsFromApi() {
        String apiKey = "df35c0d464cf7e85c0b829aa2d8b280f"; // Your API key
        repository.refreshNews(apiKey);
    }
}