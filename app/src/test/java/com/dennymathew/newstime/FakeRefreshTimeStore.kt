package com.dennymathew.newstime

import com.dennymathew.newstime.data.local.RefreshTimeStore

class FakeRefreshTimeStore(var lastRefresh: Long? = null) : RefreshTimeStore {
    override suspend fun lastRefreshMillis(): Long? = lastRefresh
    override suspend fun setLastRefreshMillis(millis: Long) {
        lastRefresh = millis
    }
}
