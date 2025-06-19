// 文件: com/example/newsapp/NewsDao.java
package com.example.newsapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
        // Ignore conflicts for news from API
    void insert(News news);

    @Update
    void update(News news);

    @Query("SELECT * FROM news_table ORDER BY date DESC")
    LiveData<List<News>> getAllNews();

    @Query("SELECT * FROM news_table WHERE id = :newsId")
    LiveData<News> getNewsById(int newsId);

    @Query("SELECT * FROM news_table WHERE title LIKE :query OR summary LIKE :query OR content LIKE :query ORDER BY date DESC")
    LiveData<List<News>> searchNews(String query);

    // [NEW] Get all favorite news
    @Query("SELECT * FROM news_table WHERE isFavorite = 1 ORDER BY date DESC")
    LiveData<List<News>> getFavoriteNews();

    // [NEW] Update favorite status for a list of news IDs
    @Query("UPDATE news_table SET isFavorite = :isFavorite WHERE id IN (:newsIds)")
    void updateFavoriteStatus(List<Integer> newsIds, boolean isFavorite);

    // [NEW] Delete a list of news by their IDs
    @Query("DELETE FROM news_table WHERE id IN (:newsIds)")
    void deleteNewsByIds(List<Integer> newsIds);
}