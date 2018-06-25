package com.dennymathew.newstime.ui.activity;

import android.os.Bundle;

import com.dennymathew.newstime.R;
import com.dennymathew.newstime.base.activity.BaseActivity;
import com.dennymathew.newstime.data.model.News;
import com.dennymathew.newstime.data.service.NewsService;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    @Inject
    NewsService newsService;

    Call<News> newsApiCall;

    News news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsApiCall = newsService.getTopNews();

        newsApiCall.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                Timber.d("Successful! Data: %s",response.message());
                news = response.body();
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Timber.d("Failed to fetch news");
            }
        });
    }
}
