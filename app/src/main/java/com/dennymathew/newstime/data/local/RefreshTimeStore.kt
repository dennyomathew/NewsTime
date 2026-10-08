package com.dennymathew.newstime.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Remembers when each feed was last fetched, across app restarts. */
interface RefreshTimeStore {
    suspend fun lastRefreshMillis(feed: String): Long?
    suspend fun setLastRefreshMillis(feed: String, millis: Long)
}

private val Context.refreshDataStore by preferencesDataStore(name = "refresh")

@Singleton
class DataStoreRefreshTimeStore @Inject constructor(
    @ApplicationContext private val context: Context
) : RefreshTimeStore {

    override suspend fun lastRefreshMillis(feed: String): Long? =
        context.refreshDataStore.data.first()[key(feed)]

    override suspend fun setLastRefreshMillis(feed: String, millis: Long) {
        context.refreshDataStore.edit { it[key(feed)] = millis }
    }

    private fun key(feed: String) = longPreferencesKey("last_refresh_millis_$feed")
}
