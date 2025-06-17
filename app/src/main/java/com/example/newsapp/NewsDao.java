// 文件: com/example/newsapp/NewsDao.java
package com.example.newsapp;

import androidx.lifecycle.LiveData; // 可选，如果想用 LiveData
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NewsDao {

    @Insert
    void insert(News news);

    @Update
    void update(News news);

    @Delete
    void delete(News news);

    @Query("DELETE FROM news_table")
    void deleteAllNews();

    @Query("SELECT * FROM news_table ORDER BY date DESC")
    List<News> getAllNews(); // 或者 LiveData<List<News>> getAllNews();

    @Query("SELECT * FROM news_table WHERE id = :newsId")
    News getNewsById(int newsId); // 或者 LiveData<News> getNewsById(int newsId);

    // 新增：用于搜索新闻的方法
    @Query("SELECT * FROM news_table WHERE title LIKE :query OR summary LIKE :query OR content LIKE :query ORDER BY date DESC")
    List<News> searchNews(String query); // 或者 LiveData<List<News>> searchNews(String query);
}