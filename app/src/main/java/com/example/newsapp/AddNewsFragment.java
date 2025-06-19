package com.example.newsapp;

import android.os.Bundle;
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

    private EditText editTextTitle;
    private EditText editTextSummary;
    private EditText editTextContent;
    private EditText editTextDate;
    private NewsListViewModel newsListViewModel; // 复用已有的 ViewModel

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取 Activity 作用域的 ViewModel
        newsListViewModel = new ViewModelProvider(requireActivity()).get(NewsListViewModel.class);
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
        Button buttonSaveNews = view.findViewById(R.id.button_save_news);

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

        // 使用 ViewModel 插入数据。这里不再需要 Executors 或 Handlers。
        newsListViewModel.insert(news);

        Toast.makeText(getContext(), "新闻已保存", Toast.LENGTH_SHORT).show();

        // 导航返回
        // 列表将通过 LiveData 自动更新，不再需要标志位。
        if (getView() != null) {
            boolean isLargeScreen = requireActivity().findViewById(R.id.news_detail_container) != null;
            if (isLargeScreen) {
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                Navigation.findNavController(getView()).popBackStack();
            }
        }
    }
}