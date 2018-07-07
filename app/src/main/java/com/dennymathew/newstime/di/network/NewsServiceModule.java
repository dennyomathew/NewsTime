package com.dennymathew.newstime.di.network;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.dennymathew.newstime.data.database.NewsArticleDao;
import com.dennymathew.newstime.data.database.NewsDatabase;
import com.dennymathew.newstime.data.network.service.NewsService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

import static com.dennymathew.newstime.util.Constants.DATABASE_NAME;

@Module(includes = NetworkModule.class)
public class NewsServiceModule {

    @Provides
    NewsService provideNewsService(Retrofit retrofit) {
        return retrofit.create(NewsService.class);
    }

    @Provides
    @Singleton
    NewsDatabase provideDatabase(Context context) {
        return Room.databaseBuilder(context,
                NewsDatabase.class, DATABASE_NAME).build();
    }

    @Provides
    @Singleton
    NewsArticleDao provideNewsArticle(NewsDatabase db) {
        return db.newsArticleDao();
    }

}
