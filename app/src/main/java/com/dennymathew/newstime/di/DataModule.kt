package com.dennymathew.newstime.di

import com.dennymathew.newstime.data.local.DataStoreRefreshTimeStore
import com.dennymathew.newstime.data.local.RefreshTimeStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindRefreshTimeStore(store: DataStoreRefreshTimeStore): RefreshTimeStore
}
