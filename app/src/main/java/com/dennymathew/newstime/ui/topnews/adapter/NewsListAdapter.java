package com.dennymathew.newstime.ui.topnews.adapter;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dennymathew.newstime.R;
import com.dennymathew.newstime.data.model.NewsArticle;
import com.dennymathew.newstime.databinding.NewsArticleItemBinding;
import com.dennymathew.newstime.ui.common.DataBoundListAdapter;

public class NewsListAdapter extends DataBoundListAdapter<NewsArticle, NewsArticleItemBinding>
{
    private final DataBindingComponent dataBindingComponent;
    private final NewsArticleClickCallback newsArticleClickCallback;

    public NewsListAdapter(DataBindingComponent dataBindingComponent, NewsArticleClickCallback newsArticleClickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.newsArticleClickCallback = newsArticleClickCallback;
    }

    @Override
    protected NewsArticleItemBinding createBinding(ViewGroup parent) {
        NewsArticleItemBinding binding = DataBindingUtil.
                inflate(LayoutInflater.from(parent.getContext()),  R.layout.news_article_item,
                        parent, false, dataBindingComponent);

        return binding;
    }

    @Override
    protected void bind(NewsArticleItemBinding binding, NewsArticle item) {
        binding.setNewsArticle(item);
    }

    @Override
    protected boolean areItemsTheSame(NewsArticle oldItem, NewsArticle newItem) {
        return false;
    }

    @Override
    protected boolean areContentsTheSame(NewsArticle oldItem, NewsArticle newItem) {
        return false;
    }

    public interface NewsArticleClickCallback {
        void onClick(NewsArticle newsArticle);
    }
}
