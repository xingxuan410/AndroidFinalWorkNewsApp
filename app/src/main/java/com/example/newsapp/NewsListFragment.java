package com.example.newsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NewsListFragment extends Fragment implements NewsAdapter.OnNewsClickListener {

    private NewsListViewModel newsListViewModel;
    private SharedViewModel sharedViewModel;
    private NewsAdapter adapter;
    private boolean isLargeScreen = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsListViewModel = new ViewModelProvider(requireActivity()).get(NewsListViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isLargeScreen = requireActivity().findViewById(R.id.news_detail_container) != null;

        setupRecyclerView(view);
        setupSearchView(view);
        setupFabs(view);

        newsListViewModel.newsList.observe(getViewLifecycleOwner(), news -> {
            adapter.submitList(news);
            if (isLargeScreen && !news.isEmpty() && sharedViewModel.getSelectedNewsId().getValue() == null) {
                sharedViewModel.selectNews(news.get(0).getId());
            }
        });

        // 首次加载时自动刷新一次新闻
        if (savedInstanceState == null) {
            newsListViewModel.refreshNewsFromApi();
        }
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NewsAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView(@NonNull View view) {
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                newsListViewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newsListViewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    // 更新：添加刷新按钮的逻辑
    private void setupFabs(@NonNull View view) {
        FloatingActionButton fabAddNews = view.findViewById(R.id.fab_add_news);
        fabAddNews.setOnClickListener(v -> {
            if (isLargeScreen) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.news_detail_container, new AddNewsFragment())
                        .addToBackStack(null)
                        .commit();
            } else {
                Navigation.findNavController(view).navigate(R.id.action_newsListFragment_to_addNewsFragment);
            }
        });

        // 你需要在 fragment_news_list.xml 布局文件中添加一个新的 FloatingActionButton
        // 其 ID 应该是 "fab_refresh_news"
        FloatingActionButton fabRefreshNews = view.findViewById(R.id.fab_refresh_news);
        if (fabRefreshNews != null) {
            fabRefreshNews.setOnClickListener(v -> {
                Toast.makeText(getContext(), "正在刷新新闻...", Toast.LENGTH_SHORT).show();
                newsListViewModel.refreshNewsFromApi();
            });
        }
    }

    @Override
    public void onNewsClick(News news) {
        if (isLargeScreen) {
            sharedViewModel.selectNews(news.getId());
        } else {
            Bundle args = new Bundle();
            args.putInt("newsId", news.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_newsListFragment_to_newsDetailFragment, args);
        }
    }
}
