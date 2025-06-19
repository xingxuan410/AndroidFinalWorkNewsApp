// 文件路径: com/example/newsapp/api/NewsApiResponse.java
package com.example.newsapp.api;

import java.util.List;

public class NewsApiResponse {
    private String status;
    // 修改点：字段名从 totalResults 变更为 totalArticles
    private int totalArticles;
    private List<Article> articles;

    // Getters
    public String getStatus() {
        return status;
    }

    // 修改点：对应修改getter方法
    public int getTotalArticles() {
        return totalArticles;
    }

    public List<Article> getArticles() {
        return articles;
    }
}