// 文件路径: com/example/newsapp/api/NewsApiService.java
package com.example.newsapp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    @GET("api/v4/top-headlines")
    Call<NewsApiResponse> getTopHeadlines(
            // 修正点：参数名从 topic 改为 category
            @Query("category") String category,   // category, 例如 general, world, business 等
            @Query("lang") String lang,           // 语言，例如 "zh" 代表中文
            @Query("country") String country,     // 国家，例如 "cn" 代表中国
            @Query("apikey") String apikey         // 你的 GNews API 密钥
    );
}