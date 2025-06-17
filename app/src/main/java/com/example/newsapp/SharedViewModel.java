// SharedViewModel.java
package com.example.newsapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> newsUpdated = new MutableLiveData<>(false);

    public void setNewsUpdated(boolean updated) {
        newsUpdated.setValue(updated);
    }

    public LiveData<Boolean> getNewsUpdated() {
        return newsUpdated;
    }
}