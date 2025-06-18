// 文件: com/example/newsapp/NewsAdapter.java
package com.example.newsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Set;

public class NewsAdapter extends ListAdapter<News, NewsAdapter.NewsViewHolder> {

    private final OnNewsActionClickListener listener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedIds = Collections.emptySet();

    public interface OnNewsActionClickListener {
        void onNewsClick(News news);
        void onNewsLongClick(News news);
        void onNewsSelectionChange(News news);
    }

    public NewsAdapter(@NonNull OnNewsActionClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // [NEW] Method to update adapter's selection state
    public void setSelectionState(boolean isSelectionMode, Set<Integer> selectedIds) {
        this.isSelectionMode = isSelectionMode;
        this.selectedIds = selectedIds;
        notifyDataSetChanged(); // In a real-world app, prefer more specific notify calls
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = getItem(position);
        holder.bind(news, listener, isSelectionMode, selectedIds.contains(news.getId()));
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, summaryTextView, dateTextView;
        CheckBox selectionCheckBox;
        LinearLayout contentLayout;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_news_title);
            summaryTextView = itemView.findViewById(R.id.text_news_summary);
            dateTextView = itemView.findViewById(R.id.text_news_date);
            selectionCheckBox = itemView.findViewById(R.id.checkbox_select);
            contentLayout = itemView.findViewById(R.id.news_content_layout);
        }

        public void bind(final News news, final OnNewsActionClickListener listener, boolean isSelectionMode, boolean isSelected) {
            titleTextView.setText(news.getTitle());
            summaryTextView.setText(news.getSummary());
            dateTextView.setText(news.getDate());

            // Adjust layout based on selection mode
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentLayout.getLayoutParams();
            if (isSelectionMode) {
                selectionCheckBox.setVisibility(View.VISIBLE);
                selectionCheckBox.setChecked(isSelected);
                params.setMarginStart(0); // No margin when checkbox is visible
            } else {
                selectionCheckBox.setVisibility(View.GONE);
                params.setMarginStart(8); // Default margin
            }
            contentLayout.setLayoutParams(params);

            itemView.setOnClickListener(v -> {
                if (isSelectionMode) {
                    listener.onNewsSelectionChange(news);
                } else {
                    listener.onNewsClick(news);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (!isSelectionMode) {
                    listener.onNewsLongClick(news);
                }
                return true; // Consume the long click
            });
        }
    }

    private static final DiffUtil.ItemCallback<News> DIFF_CALLBACK = new DiffUtil.ItemCallback<News>() {
        @Override
        public boolean areItemsTheSame(@NonNull News oldItem, @NonNull News newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull News oldItem, @NonNull News newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getSummary().equals(newItem.getSummary()) &&
                    oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.isFavorite() == newItem.isFavorite(); // [MODIFIED] Check favorite status
        }
    };
}