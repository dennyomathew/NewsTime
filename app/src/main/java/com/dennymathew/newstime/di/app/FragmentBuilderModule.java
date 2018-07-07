package com.dennymathew.newstime.di.app;

import com.dennymathew.newstime.ui.topnews.fragment.HeadlinesFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector(modules = ViewModelModule.class)
    abstract HeadlinesFragment contributesHeadlinesFragment();
}
