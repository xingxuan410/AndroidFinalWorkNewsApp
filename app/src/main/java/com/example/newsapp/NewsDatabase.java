// 文件: com/example/newsapp/NewsDatabase.java
package com.example.newsapp;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull; // <-- 【新增】导入 @NonNull
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration; // <-- 【新增】导入 Migration
import androidx.sqlite.db.SupportSQLiteDatabase; // <-- 【新增】导入 SupportSQLiteDatabase

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 版本号已经正确地从 1 更新到了 2
@Database(entities = {News.class}, version = 2, exportSchema = false)
public abstract class NewsDatabase extends RoomDatabase {

    private static final String DB_TAG = "App_DB_Log";

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
                            // 【修改】添加迁移策略。如果数据库需要从版本1升级到2，就执行这个策略
                            .addMigrations(MIGRATION_1_2)
                            .build();
                    Log.i(DB_TAG, "数据库实例: 实例创建成功。");
                }
            }
        }
        return INSTANCE;
    }

    // 【新增】定义一个从版本1到版本2的迁移策略
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 在这里编写数据库架构变更的SQL语句
            // 这行代码的意思是：在'news_table'表中，添加一个名为'url'的新列，其类型为TEXT
            Log.i(DB_TAG, "数据库迁移: 正在执行从版本1到2的迁移...");
            database.execSQL("ALTER TABLE news_table ADD COLUMN url TEXT");
            Log.i(DB_TAG, "数据库迁移: 'url'列已成功添加到'news_table'。");
        }
    };
}