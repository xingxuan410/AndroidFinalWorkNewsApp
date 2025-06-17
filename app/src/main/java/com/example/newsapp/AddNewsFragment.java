// 文件名: AddNewsFragment.java
package com.example.newsapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddNewsFragment extends Fragment {

    private static final String DB_TAG = "App_DB_Log"; // 统一数据库 TAG

    private EditText editTextTitle;
    private EditText editTextSummary;
    private EditText editTextContent;
    private EditText editTextDate;
    private Button buttonSaveNews;
    private NewsDao newsDao;
    private SharedViewModel sharedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsDao = NewsDatabase.getDatabase(requireContext()).newsDao();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextTitle = view.findViewById(R.id.edit_text_title);
        editTextSummary = view.findViewById(R.id.edit_text_summary);
        editTextContent = view.findViewById(R.id.edit_text_content);
        editTextDate = view.findViewById(R.id.edit_text_date);
        buttonSaveNews = view.findViewById(R.id.button_save_news);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        editTextDate.setText(currentDate);

        buttonSaveNews.setOnClickListener(v -> saveNews());
    }

    private void saveNews() {
        String title = editTextTitle.getText().toString().trim();
        String summary = editTextSummary.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "标题、内容和日期不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        News news = new News(title, summary, content, date);

        NewsDatabase.databaseWriteExecutor.execute(() -> {
            // Log.i(DB_TAG, "数据库操作开始: 尝试插入新闻: " + news.getTitle() + "，线程: " + Thread.currentThread().getName()); // 如果需要非常详细的开始日志
            try {
                newsDao.insert(news);
                Log.i(DB_TAG, "数据库操作成功: 新闻已插入: " + news.getTitle());
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "新闻已保存", Toast.LENGTH_SHORT).show();
                    sharedViewModel.setNewsUpdated(true);
                    boolean isLargeScreen = requireActivity().findViewById(R.id.news_detail_container) != null;
                    if (isLargeScreen) {
                        if (isAdded() && getActivity() != null) {
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    } else if (getView() != null && isAdded()) {
                        Navigation.findNavController(getView()).popBackStack();
                    }
                });
            } catch (Exception e) {
                Log.e(DB_TAG, "数据库操作错误: 插入新闻失败: " + news.getTitle(), e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "保存新闻失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}