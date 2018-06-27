package com.dennymathew.newstime.di.app;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;


import com.dennymathew.newstime.viewmodel.HeadlinesViewModel;
import com.dennymathew.newstime.viewmodel.NewsViewModelFactory;
import com.dennymathew.newstime.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(HeadlinesViewModel.class)
    abstract ViewModel bindHeadlinesViewModel(HeadlinesViewModel headlinesViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(NewsViewModelFactory factory);
}
