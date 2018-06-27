package com.dennymathew.newstime.di.app;

import com.dennymathew.newstime.ui.activity.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector
    abstract MainActivity bindMainActivity();
}
