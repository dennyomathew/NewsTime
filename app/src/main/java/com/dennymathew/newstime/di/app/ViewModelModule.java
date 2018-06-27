package com.dennymathew.newstime.di.app;

import android.arch.lifecycle.ViewModelProvider;


import com.dennymathew.newstime.viewmodel.NewsViewModelFactory;

import dagger.Binds;
import dagger.Module;

@Module
abstract class ViewModelModule {


    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(NewsViewModelFactory factory);
}
