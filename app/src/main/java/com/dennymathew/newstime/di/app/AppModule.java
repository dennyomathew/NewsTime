package com.dennymathew.newstime.di.app;

import android.content.Context;

import com.dennymathew.newstime.base.App;
import com.dennymathew.newstime.util.AppExecutors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Denny on 3/30/2018.
 */

@Module
public class AppModule {

    @Singleton
    @Provides
    Context provideAppContext(App application) {
        return application.getApplicationContext();
    }


    @Provides
    @Singleton
    AppExecutors provideAppExecutors() {
        return AppExecutors.getInstance();
    }
}
