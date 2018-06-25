package com.dennymathew.newstime.di.app;

import android.content.Context;

import com.dennymathew.newstime.base.App;

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
    Context providesAppContext(App application) {
        return application.getApplicationContext();
    }
}
