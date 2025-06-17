package com.example.newsapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    // 错误原因：您很可能缺少下面这一行声明
    private final MutableLiveData<Integer> selectedNewsId = new MutableLiveData<>();

    public void selectNews(int newsId) {
        // 如果上面一行存在，这里就能正确找到 selectedNewsId
        selectedNewsId.setValue(newsId);
    }

    public LiveData<Integer> getSelectedNewsId() {
        // 如果声明存在，这里也能正确返回
        return selectedNewsId;
    }
}