// 文件: com/example/newsapp/FavoritesViewModel.java
package com.example.newsapp;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesViewModel extends AndroidViewModel {

    private final NewsRepository repository;
    public final LiveData<List<News>> favoriteNewsList;

    // LiveData for managing selection mode UI
    private final MutableLiveData<Boolean> _isSelectionMode = new MutableLiveData<>(false);
    public final LiveData<Boolean> isSelectionMode = _isSelectionMode;

    private final MutableLiveData<Set<Integer>> _selectedNewsIds = new MutableLiveData<>(new HashSet<>());
    public final LiveData<Set<Integer>> selectedNewsIds = _selectedNewsIds;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        repository = new NewsRepository(application);
        favoriteNewsList = repository.getFavoriteNews();
    }

    // Methods to manage selection state
    public void toggleSelection(int newsId) {
        Set<Integer> currentSelection = _selectedNewsIds.getValue();
        if (currentSelection == null) currentSelection = new HashSet<>();

        if (currentSelection.contains(newsId)) {
            currentSelection.remove(newsId);
        } else {
            currentSelection.add(newsId);
        }
        _selectedNewsIds.setValue(currentSelection);

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

    // Method to remove selected items from favorites
    public void unFavoriteSelectedNews() {
        Set<Integer> ids = _selectedNewsIds.getValue();
        if (ids != null && !ids.isEmpty()) {
            repository.updateFavoriteStatus(new ArrayList<>(ids), false);
            clearSelection();
        }
    }
}