package com.dennymathew.newstime.data.network;

import com.dennymathew.newstime.BuildConfig;
import com.dennymathew.newstime.data.model.News;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NewsService {

    @GET("top-headlines?country=us&apiKey=" + BuildConfig.API_KEY)
    Call<News> getTopNews();
}


