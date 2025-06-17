// 文件名: com/example/newsapp/NewsListFragment.java
package com.example.newsapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NewsListFragment extends Fragment implements NewsAdapter.OnNewsClickListener {

    private static final String DB_TAG = "App_DB_Log"; // 统一数据库 TAG

    private NewsAdapter adapter;
    private OnNewsSelectedListener callback;
    private boolean isLargeScreen = false;
    private NewsDao newsDao;
    private SearchView searchView;
    private SharedViewModel sharedViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNewsSelectedListener) {
            callback = (OnNewsSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnNewsSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            newsDao = NewsDatabase.getDatabase(requireContext()).newsDao();
        }
        if (getActivity() != null) {
            sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null) {
            return;
        }
        isLargeScreen = requireActivity().findViewById(R.id.news_detail_container) != null;

        RecyclerView recyclerView = view.findViewById(R.id.recycler_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NewsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        searchView = view.findViewById(R.id.search_view);
        setupSearchView();

        if (sharedViewModel != null) {
            sharedViewModel.getNewsUpdated().observe(getViewLifecycleOwner(), updated -> {
                if (updated != null && updated) {
                    reloadNews();
                    sharedViewModel.setNewsUpdated(false);
                }
            });
        }

        FloatingActionButton fabAddNews = view.findViewById(R.id.fab_add_news);
        fabAddNews.setOnClickListener(v -> {
            if (isLargeScreen) {
                if (isAdded() && getActivity() != null) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.news_detail_container, new AddNewsFragment())
                            .addToBackStack(null)
                            .commit();
                }
            } else {
                if (getView() != null && isAdded()) {
                    Navigation.findNavController(view).navigate(R.id.action_newsListFragment_to_addNewsFragment);
                }
            }
        });
    }

    private void loadNewsFromDatabase() {
        if (newsDao == null) {
            if (adapter != null && isAdded()) adapter.setNewsList(new ArrayList<>());
            return;
        }
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            // Log.i(DB_TAG, "数据库操作开始: 尝试获取所有新闻，线程: " + Thread.currentThread().getName());
            List<News> loadedNewsList = new ArrayList<>();
            try {
                loadedNewsList = newsDao.getAllNews();
                Log.i(DB_TAG, "数据库操作成功: 从getAllNews获取到 " + loadedNewsList.size() + " 条新闻。");
            } catch (Exception e) {
                Log.e(DB_TAG, "数据库操作错误: 获取所有新闻失败。", e);
            }
            final List<News> finalLoadedNewsList = loadedNewsList;
            if (isAdded() && getActivity() != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (adapter != null) {
                        adapter.setNewsList(finalLoadedNewsList);
                        if (isLargeScreen && !finalLoadedNewsList.isEmpty() && callback != null) {
                            callback.onNewsSelected(finalLoadedNewsList.get(0));
                        }
                    }
                });
            }
        });
    }

    private void setupSearchView() {
        if (searchView == null) {
            return;
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadNewsFromDatabase();
                } else {
                    performSearch(newText);
                }
                return true;
            }
        });
        searchView.setOnCloseListener(() -> {
            loadNewsFromDatabase();
            return false;
        });
    }

    private void performSearch(String query) {
        final String searchQuery = "%" + query + "%";
        if (newsDao == null) {
            if (adapter != null && isAdded()) adapter.setNewsList(new ArrayList<>());
            return;
        }
        NewsDatabase.databaseWriteExecutor.execute(() -> {
            // Log.i(DB_TAG, "数据库操作开始: 尝试搜索新闻，查询: '" + searchQuery + "'，线程: " + Thread.currentThread().getName());
            List<News> searchResults = new ArrayList<>();
            try {
                searchResults = newsDao.searchNews(searchQuery);
                Log.i(DB_TAG, "数据库操作成功: 搜索查询 '" + searchQuery + "' 找到 " + searchResults.size() + " 条结果。");
            } catch (Exception e) {
                Log.e(DB_TAG, "数据库操作错误: 搜索新闻查询 '" + searchQuery + "' 失败。", e);
            }
            final List<News> finalSearchResults = searchResults;
            if (isAdded() && getActivity() != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (adapter != null) {
                        adapter.setNewsList(finalSearchResults);
                    }
                });
            }
        });
    }

    @Override
    public void onNewsClick(News news) {
        if (news == null) {
            return;
        }
        if (isLargeScreen) {
            if (callback != null) {
                callback.onNewsSelected(news);
            }
        } else {
            Bundle args = new Bundle();
            args.putInt("newsId", news.getId());
            if (getView() != null && isAdded()) {
                Navigation.findNavController(requireView()).navigate(
                        R.id.action_newsListFragment_to_newsDetailFragment, args);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadNews();
    }

    public interface OnNewsSelectedListener {
        void onNewsSelected(News news);
    }

    public void reloadNews() {
        if (getView() == null || !isAdded()){
            return;
        }
        if (searchView == null) {
            searchView = getView().findViewById(R.id.search_view);
            if (searchView == null) {
                loadNewsFromDatabase();
                return;
            }
        }

        String currentQuery = searchView.getQuery().toString().trim();
        if (currentQuery.isEmpty()) {
            loadNewsFromDatabase();
        } else {
            performSearch(currentQuery);
        }
    }
}