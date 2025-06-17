// 文件: com/example/newsapp/News.java
package com.example.newsapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "news_table") // 定义表名
public class News {

    @PrimaryKey(autoGenerate = true) // 设置 id 为主键并自动生成
    private int id;

    private String title;
    private String summary;
    private String content;
    private String date;

    // 构造函数，注意 Room 需要一个无参构造函数，或者所有字段都有 getter/setter
    // 为了方便，我们保留这个构造函数，并确保 Room 可以通过字段或 getter/setter 访问它们
    public News(String title, String summary, String content, String date) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.date = date;
    }

    // Room 需要 getter 和 setter 方法来访问字段，或者字段本身是 public 的
    // (为了保持封装性，推荐使用 getter/setter)
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
}