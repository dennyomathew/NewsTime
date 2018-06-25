package com.dennymathew.newstime.di.network;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.dennymathew.newstime.data.database.NewsDatabase;
import com.dennymathew.newstime.data.service.NewsService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

import static com.dennymathew.newstime.shared.Constants.DATABASE_NAME;

@Module(includes = NetworkModule.class)
public class NewsServiceModule {

    @Provides
    NewsService providesNewsService(Retrofit retrofit) {
        return retrofit.create(NewsService.class);
    }

    @Provides
    @Singleton
    NewsDatabase providesDatabase(Context context) {
        return Room.databaseBuilder(context,
                NewsDatabase.class, DATABASE_NAME).build();
    }

}
