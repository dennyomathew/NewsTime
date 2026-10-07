package com.dennymathew.newstime.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Remembers when headlines were last fetched, across app restarts. */
interface RefreshTimeStore {
    suspend fun lastRefreshMillis(): Long?
    suspend fun setLastRefreshMillis(millis: Long)
}

private val Context.refreshDataStore by preferencesDataStore(name = "refresh")

@Singleton
class DataStoreRefreshTimeStore @Inject constructor(
    @ApplicationContext private val context: Context
) : RefreshTimeStore {

    override suspend fun lastRefreshMillis(): Long? =
        context.refreshDataStore.data.first()[LAST_REFRESH]

    override suspend fun setLastRefreshMillis(millis: Long) {
        context.refreshDataStore.edit { it[LAST_REFRESH] = millis }
    }

    private companion object {
        val LAST_REFRESH = longPreferencesKey("last_refresh_millis")
    }
}
