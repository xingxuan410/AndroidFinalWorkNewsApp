package com.example.newsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class NewsAdapter extends ListAdapter<News, NewsAdapter.NewsViewHolder> {

    private final OnNewsClickListener listener;

    public interface OnNewsClickListener {
        void onNewsClick(News news);
    }

    public NewsAdapter(@NonNull OnNewsClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // DiffUtil 计算差异以实现高效的动画效果
    private static final DiffUtil.ItemCallback<News> DIFF_CALLBACK = new DiffUtil.ItemCallback<News>() {
        @Override
        public boolean areItemsTheSame(@NonNull News oldItem, @NonNull News newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull News oldItem, @NonNull News newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getSummary().equals(newItem.getSummary()) &&
                    oldItem.getDate().equals(newItem.getDate());
        }
    };

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = getItem(position);
        holder.bind(news, listener);
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView summaryTextView;
        TextView dateTextView;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_news_title);
            summaryTextView = itemView.findViewById(R.id.text_news_summary);
            dateTextView = itemView.findViewById(R.id.text_news_date);
        }

        public void bind(final News news, final OnNewsClickListener listener) {
            titleTextView.setText(news.getTitle());
            summaryTextView.setText(news.getSummary());
            dateTextView.setText(news.getDate());
            itemView.setOnClickListener(v -> listener.onNewsClick(news));
        }
    }
}