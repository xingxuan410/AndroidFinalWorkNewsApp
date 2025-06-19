// 文件: com/example/newsapp/News.java
package com.example.newsapp;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "news_table")
public class News {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String summary;
    private String content;
    private String date;
    private String url;
    private boolean isFavorite; // [NEW] Field to track favorite status

    public News() {
        // Room requires a no-arg constructor.
    }

    @Ignore
    public News(String title, String summary, String content, String date, String url) {
        this(title, summary, content, date, url, false); // Default isFavorite to false
    }

    // [NEW] Primary constructor that Room will use
    public News(String title, String summary, String content, String date, String url, boolean isFavorite) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.date = date;
        this.url = url;
        this.isFavorite = isFavorite;
    }


    @Ignore
    public News(String title, String summary, String content, String date) {
        this(title, summary, content, date, null, false);
    }

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFavorite() {
        return isFavorite;
    } // [NEW]

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    } // [NEW]
}