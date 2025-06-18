package com.example.newsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NewsListFragment extends Fragment implements NewsAdapter.OnNewsActionClickListener {

    private NewsListViewModel newsListViewModel;
    private SharedViewModel sharedViewModel;
    private NewsAdapter adapter;
    private boolean isLargeScreen = false;

    private Group fabNormalGroup;
    private LinearLayout selectionActionBar;
    private Button deleteButton, collectButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsListViewModel = new ViewModelProvider(requireActivity()).get(NewsListViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (newsListViewModel.isSelectionMode.getValue() != null && newsListViewModel.isSelectionMode.getValue()) {
                    newsListViewModel.clearSelection();
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
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
        setupUI(view);
        observeViewModel();

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

    private void setupUI(@NonNull View view) {
        fabNormalGroup = view.findViewById(R.id.fab_group_normal);
        selectionActionBar = view.findViewById(R.id.selection_action_bar);
        deleteButton = view.findViewById(R.id.button_delete);
        collectButton = view.findViewById(R.id.button_collect);

        FloatingActionButton fabAddNews = view.findViewById(R.id.fab_add_news);
        FloatingActionButton fabRefreshNews = view.findViewById(R.id.fab_refresh_news);
        FloatingActionButton fabFavorites = view.findViewById(R.id.fab_favorites);

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

        fabRefreshNews.setOnClickListener(v -> {
            Toast.makeText(getContext(), "正在刷新新闻...", Toast.LENGTH_SHORT).show();
            newsListViewModel.refreshNewsFromApi();
        });

        fabFavorites.setOnClickListener(v -> {
            // [FIX] Correctly handle navigation for large screens vs. small screens
            if (isLargeScreen) {
                // On large screens, replace the detail container with the favorites list
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.news_detail_container, new FavoritesFragment())
                        .addToBackStack(null) // Allow user to press back to return to detail view
                        .commit();
            } else {
                // On small screens, use the NavController to navigate to the new screen
                Navigation.findNavController(view).navigate(R.id.action_newsListFragment_to_favoritesFragment);
            }
        });

        deleteButton.setOnClickListener(v -> newsListViewModel.deleteSelectedNews());
        collectButton.setOnClickListener(v -> newsListViewModel.favoriteSelectedNews());
    }

    private void observeViewModel() {
        newsListViewModel.newsList.observe(getViewLifecycleOwner(), news -> {
            adapter.submitList(news);
            // [FIX] On a large screen, if a news item is not already selected, select the first one.
            // This populates the detail pane on first launch.
            if (isLargeScreen && !news.isEmpty() && sharedViewModel.getSelectedNewsId().getValue() == null) {
                sharedViewModel.selectNews(news.get(0).getId());
            }
        });

        newsListViewModel.isSelectionMode.observe(getViewLifecycleOwner(), isSelection -> {
            fabNormalGroup.setVisibility(isSelection ? View.GONE : View.VISIBLE);
            selectionActionBar.setVisibility(isSelection ? View.VISIBLE : View.GONE);
            if (newsListViewModel.selectedNewsIds.getValue() != null) {
                adapter.setSelectionState(isSelection, newsListViewModel.selectedNewsIds.getValue());
            }
        });

        newsListViewModel.selectedNewsIds.observe(getViewLifecycleOwner(), selectedIds -> {
            if (newsListViewModel.isSelectionMode.getValue() != null) {
                adapter.setSelectionState(newsListViewModel.isSelectionMode.getValue(), selectedIds);
            }
        });
    }

    @Override
    public void onNewsClick(News news) {
        if (isLargeScreen) {
            // On a large screen, just update the shared ViewModel. The detail fragment
            // is already visible and will observe this change.
            sharedViewModel.selectNews(news.getId());
        } else {
            // On a small screen, navigate to the detail fragment
            Bundle args = new Bundle();
            args.putInt("newsId", news.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_newsListFragment_to_newsDetailFragment, args);
        }
    }

    @Override
    public void onNewsLongClick(News news) {
        newsListViewModel.startSelectionMode(news.getId());
    }

    @Override
    public void onNewsSelectionChange(News news) {
        newsListViewModel.toggleSelection(news.getId());
    }
}
