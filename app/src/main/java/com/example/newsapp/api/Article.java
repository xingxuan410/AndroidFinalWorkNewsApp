package com.example.newsapp.api;

// 这个类用来表示从 NewsAPI 获取到的单条新闻文章
public class Article {
    private String title;
    private String description;
    private String content;
    private String publishedAt;
    private Source source; // 新增，用于处理嵌套的 source 对象

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public String getPublishedAt() { return publishedAt; }
    public Source getSource() { return source; }

    // 新增内部类
    public static class Source {
        private String name;
        public String getName() { return name; }
    }
}
