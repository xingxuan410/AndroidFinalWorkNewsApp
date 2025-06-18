// 文件: com/example/newsapp/News.java
package com.example.newsapp;

import androidx.room.Entity;
import androidx.room.Ignore;  // <-- 【新增】导入 @Ignore 注解
import androidx.room.PrimaryKey;

@Entity(tableName = "news_table")
public class News {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String summary;
    private String content;
    private String date;
    private String url; // 新增的url字段

    /**
     * 【新增】Room需要一个无参构造函数。
     * 我们可以添加一个，或者确保Room能通过其他构造函数和setter方法工作。
     * 为了清晰和安全，我们显式添加一个。
     */
    public News() {
    }

    /**
     * 【新增】这是我们的新构造函数，包含了所有字段。
     * 这是我们从网络获取数据时，推荐使用的构造函数。
     */
    @Ignore // <-- 【关键】告诉Room忽略这个构造函数，避免混淆
    public News(String title, String summary, String content, String date, String url) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.date = date;
        this.url = url;
    }

    /**
     * 【保留】这是你原来的构造函数，我们把它保留下来，并让它调用新的构造函数。
     * 这样可以保持向后兼容性，旧代码调用它时，url字段会默认为null。
     */
    @Ignore // <-- 【关键】告诉Room也忽略这个构造函数
    public News(String title, String summary, String content, String date) {
        // 调用上面的全参数构造函数，url传null
        this(title, summary, content, date, null);
    }

    // --- Getter 和 Setter 方法保持不变 ---

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

    // 新增url的getter和setter
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}