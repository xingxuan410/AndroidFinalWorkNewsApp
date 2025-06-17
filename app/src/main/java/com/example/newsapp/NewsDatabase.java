
package com.example.newsapp;

import android.content.Context;
import android.util.Log;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {News.class}, version = 1, exportSchema = false)
public abstract class NewsDatabase extends RoomDatabase {

    private static final String DB_TAG = "App_DB_Log"; // 统一数据库 TAG

    public abstract NewsDao newsDao();

    private static volatile NewsDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static NewsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NewsDatabase.class) {
                if (INSTANCE == null) {
                    Log.i(DB_TAG, "数据库实例: 实例为null，正在创建新实例...");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    NewsDatabase.class, "news_database")
                            .build();
                    Log.i(DB_TAG, "数据库实例: 实例创建成功。");
                }
            }
        }
        return INSTANCE;
    }
}