package com.dennymathew.newstime

import com.dennymathew.newstime.data.local.RefreshTimeStore

class FakeRefreshTimeStore : RefreshTimeStore {
    val times = mutableMapOf<String, Long>()
    override suspend fun lastRefreshMillis(feed: String): Long? = times[feed]
    override suspend fun setLastRefreshMillis(feed: String, millis: Long) {
        times[feed] = millis
    }
}
