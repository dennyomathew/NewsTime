package com.dennymathew.newstime.di.app;

import com.dennymathew.newstime.di.activity.MainActivityModule;
import com.dennymathew.newstime.ui.activity.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = MainActivityModule.class)
    abstract MainActivity bindMainActivity();
}
