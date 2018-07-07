package com.dennymathew.newstime.data.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dennymathew.newstime.data.database.NewsArticleDao;
import com.dennymathew.newstime.data.model.News;
import com.dennymathew.newstime.data.model.NewsArticle;
import com.dennymathew.newstime.data.network.Resource;
import com.dennymathew.newstime.data.network.service.NewsService;
import com.dennymathew.newstime.util.AppExecutors;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NewsRepository {

    private AppExecutors appExecutors;
    private NewsService newsService;
    private NewsArticleDao newsArticleDao;

    private Long lastFetched = 0L;


    public boolean shouldFetchFromNetwork() {
        if(lastFetched == 0L) {
            lastFetched = System.currentTimeMillis();
            return true;
        }
        return System.currentTimeMillis() - lastFetched > 3600;
    }

    @Inject
    public NewsRepository(AppExecutors appExecutors, NewsService newsService, NewsArticleDao newsArticleDao) {
        this.appExecutors = appExecutors;
        this.newsService = newsService;
        this.newsArticleDao = newsArticleDao;
        this.lastFetched = System.currentTimeMillis();
    }

//    public void fetchNewsFromNetwork() {
//
//        Call<News> newsApiCall = newsService.getTopNews();
//
//        newsApiCall.enqueue(new Callback<News>() {
//            @Override
//            public void onResponse(@NonNull Call<News> call, @NonNull Response<News> response) {
//                Timber.d("Successful! Data: %s",response.message());
//                news = response.body();
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<News> call, @NonNull Throwable t) {
//                Timber.d("Failed to fetch news");
//            }
//        });
//    }

    public LiveData<Resource<List<NewsArticle>>> fetchNews() {
        return new NetworkBoundResource<List<NewsArticle>, News>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull News news) {
                newsArticleDao.insertNewsArticles(news.newsArticleLst);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<NewsArticle> data) {
                return data == null || data.isEmpty() || shouldFetchFromNetwork();
            }

            @NonNull
            @Override
            protected LiveData<List<NewsArticle>> loadFromDb() {
                return newsArticleDao.getTopNewsArticles();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<News>> createCall() {
                return newsService.getTopNews();
            }
        }.asLiveData();
    }

}
