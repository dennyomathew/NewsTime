package com.dennymathew.newstime.di.app;

import com.dennymathew.newstime.ui.fragment.HeadlinesFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector(modules = FragmentBuilderModule.class)
    abstract HeadlinesFragment contributesHeadlinesFragment();
}
