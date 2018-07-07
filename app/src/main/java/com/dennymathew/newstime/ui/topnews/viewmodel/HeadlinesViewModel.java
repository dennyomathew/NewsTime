package com.dennymathew.newstime.ui.topnews.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.dennymathew.newstime.data.model.NewsArticle;
import com.dennymathew.newstime.data.network.Resource;
import com.dennymathew.newstime.data.repository.NewsRepository;

import java.util.List;

import javax.inject.Inject;

public class HeadlinesViewModel extends ViewModel {

    NewsRepository newsRepository;

    LiveData<Resource<List<NewsArticle>>> newsArticlesData;

    @Inject
    public HeadlinesViewModel(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
        this.newsArticlesData = newsRepository.fetchNews();
    }

    public LiveData<Resource<List<NewsArticle>>> getNewsArticles() {
        return newsArticlesData;
    }


}
