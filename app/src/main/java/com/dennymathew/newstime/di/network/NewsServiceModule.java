package com.dennymathew.newstime.di.network;

import com.dennymathew.newstime.data.network.NewsService;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module(includes = NetworkModule.class)
public class NewsServiceModule {

    @Provides
    NewsService providesNewsService(Retrofit retrofit) {
        return retrofit.create(NewsService.class);
    }

}
