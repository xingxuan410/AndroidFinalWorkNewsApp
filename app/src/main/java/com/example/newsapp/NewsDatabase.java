// 文件: com/example/newsapp/NewsDatabase.java
package com.example.newsapp;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// [MODIFIED] Version updated to 3
@Database(entities = {News.class}, version = 3, exportSchema = false)
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
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    NewsDatabase.class, "news_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // [MODIFIED] Add new migration
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE news_table ADD COLUMN url TEXT");
        }
    };

    // [NEW] Migration from version 2 to 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add the 'isFavorite' column with a default value of 0 (false)
            database.execSQL("ALTER TABLE news_table ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0");
            Log.i(DB_TAG, "数据库迁移: 'isFavorite'列已成功添加到'news_table'。");
        }
    };
}