// 文件路径: com/example/newsapp/api/Article.java
package com.example.newsapp.api;

// 这个类用来表示从GNews API获取到的单条新闻文章的网络数据模型
public class Article {
    private String title;
    private String description;
    private String content;
    private String url;         // <-- 【关键新增】用于接收JSON中的'url'字段
    private String image;       // <-- 【建议新增】GNews也提供图片URL，可以一并添加
    private String publishedAt;
    private Source source;

    // --- Getters ---
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    } // <-- 【关键新增】为url字段添加getter方法

    public String getImage() {
        return image;
    } // <-- 【建议新增】图片的getter

    public String getPublishedAt() {
        return publishedAt;
    }

    public Source getSource() {
        return source;
    }

    // 内部类Source保持不变
    public static class Source {
        private String name;
        private String url; // <-- GNews的source对象里也有一个url，可以加上

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}