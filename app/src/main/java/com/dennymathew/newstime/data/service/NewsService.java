package com.dennymathew.newstime.data.service;

import com.dennymathew.newstime.BuildConfig;
import com.dennymathew.newstime.data.model.News;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NewsService {

    @GET("top-headlines?sources=associated-press&apiKey=" + BuildConfig.API_KEY)
    Call<News> getTopNews();
}


