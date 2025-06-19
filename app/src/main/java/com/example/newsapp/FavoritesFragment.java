package com.example.newsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FavoritesFragment extends Fragment implements NewsAdapter.OnNewsActionClickListener {

    private FavoritesViewModel favoritesViewModel;
    private NewsAdapter adapter;
    private SharedViewModel sharedViewModel;
    private boolean isLargeScreen = false;

    private TextView emptyFavoritesText;
    private LinearLayout selectionActionBar;
    private Button uncollectButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritesViewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (favoritesViewModel.isSelectionMode.getValue() != null && favoritesViewModel.isSelectionMode.getValue()) {
                    favoritesViewModel.clearSelection();
                } else {
                    setEnabled(false);
                    // If on a large screen, just pop this fragment from the fragment manager's back stack.
                    // Otherwise, let the NavController handle it.
                    if (isLargeScreen) {
                        getParentFragmentManager().popBackStack();
                    } else {
                        if (getView() != null) {
                            Navigation.findNavController(getView()).popBackStack();
                        }
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isLargeScreen = requireActivity().findViewById(R.id.news_detail_container) != null;

        setupRecyclerView(view);
        setupUI(view);
        observeViewModel();
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NewsAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupUI(View view) {
        emptyFavoritesText = view.findViewById(R.id.text_empty_favorites);
        selectionActionBar = view.findViewById(R.id.selection_action_bar_favorites);
        uncollectButton = view.findViewById(R.id.button_uncollect);

        uncollectButton.setOnClickListener(v -> favoritesViewModel.unFavoriteSelectedNews());
    }

    private void observeViewModel() {
        favoritesViewModel.favoriteNewsList.observe(getViewLifecycleOwner(), favoriteNews -> {
            adapter.submitList(favoriteNews);
            emptyFavoritesText.setVisibility(favoriteNews.isEmpty() ? View.VISIBLE : View.GONE);
        });

        favoritesViewModel.isSelectionMode.observe(getViewLifecycleOwner(), isSelection -> {
            selectionActionBar.setVisibility(isSelection ? View.VISIBLE : View.GONE);
            if (favoritesViewModel.selectedNewsIds.getValue() != null) {
                adapter.setSelectionState(isSelection, favoritesViewModel.selectedNewsIds.getValue());
            }
        });

        favoritesViewModel.selectedNewsIds.observe(getViewLifecycleOwner(), selectedIds -> {
            if (favoritesViewModel.isSelectionMode.getValue() != null) {
                adapter.setSelectionState(favoritesViewModel.isSelectionMode.getValue(), selectedIds);
            }
        });
    }

    @Override
    public void onNewsClick(News news) {
        // [FIX] This is the critical fix for the crash on large screens.
        if (isLargeScreen) {
            // 1. Tell the SharedViewModel which news item to display.
            sharedViewModel.selectNews(news.getId());

            // 2. Replace this FavoritesFragment with a NewsDetailFragment in the detail pane.
            // The new NewsDetailFragment will automatically observe the SharedViewModel and show the correct data.
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.news_detail_container, new NewsDetailFragment())
                    .addToBackStack(null)
                    .commit();
        } else {
            // On a small screen, use the NavController as before.
            Bundle args = new Bundle();
            args.putInt("newsId", news.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_global_newsDetailFragment, args);
        }
    }

    @Override
    public void onNewsLongClick(News news) {
        favoritesViewModel.startSelectionMode(news.getId());
    }

    @Override
    public void onNewsSelectionChange(News news) {
        favoritesViewModel.toggleSelection(news.getId());
    }
}
