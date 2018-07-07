package com.dennymathew.newstime.data.network.service;

import android.arch.lifecycle.LiveData;

import com.dennymathew.newstime.BuildConfig;
import com.dennymathew.newstime.data.model.News;
import com.dennymathew.newstime.data.repository.ApiResponse;

import retrofit2.http.GET;

public interface NewsService {

    @GET("top-headlines?sources=associated-press&apiKey=" + BuildConfig.API_KEY)
    LiveData<ApiResponse<News>> getTopNews();
}


