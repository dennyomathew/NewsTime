package com.dennymathew.newstime.di.activity;

import com.dennymathew.newstime.ui.activity.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {

    @Provides
    MainActivity provideMainActivity(MainActivity mainActivity){
        return mainActivity;
    }
}
