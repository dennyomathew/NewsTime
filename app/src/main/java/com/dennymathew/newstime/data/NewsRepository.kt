package com.dennymathew.newstime.data

import com.dennymathew.newstime.data.local.ArticleDao
import com.dennymathew.newstime.data.local.ArticleEntity
import com.dennymathew.newstime.data.local.RefreshTimeStore
import com.dennymathew.newstime.data.remote.ArticleDto
import com.dennymathew.newstime.data.remote.NewsApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Singleton
class NewsRepository(
    private val api: NewsApi,
    private val dao: ArticleDao,
    private val refreshTimes: RefreshTimeStore,
    private val now: () -> Long,
    private val maxAge: Duration = 1.hours
) {
    @Inject
    constructor(api: NewsApi, dao: ArticleDao, refreshTimes: RefreshTimeStore) :
        this(api, dao, refreshTimes, System::currentTimeMillis)

    /** Cached headlines; the database is the single source of truth. */
    val articles: Flow<List<ArticleEntity>> = dao.observeArticles()

    /**
     * Fetches fresh headlines unless the cache was refreshed within [maxAge]; the last refresh
     * time is persisted, so reopening the app doesn't refetch. Network and HTTP errors propagate
     * so the caller can show them; the cache is kept.
     */
    suspend fun refresh(force: Boolean = false) {
        if (!force && isCacheFresh()) return

        val entities = api.getTopHeadlines().articles.mapIndexedNotNull { index, dto ->
            dto.toEntity(position = index)
        }
        dao.replaceAll(entities)
        refreshTimes.setLastRefreshMillis(now())
    }

    private suspend fun isCacheFresh(): Boolean {
        val last = refreshTimes.lastRefreshMillis() ?: return false
        val age = now() - last
        // A negative age means the device clock moved backwards; treat that as stale.
        return age in 0 until maxAge.inWholeMilliseconds && dao.count() > 0
    }

    private fun ArticleDto.toEntity(position: Int): ArticleEntity? {
        return ArticleEntity(
            url = url ?: return null,
            title = title ?: return null,
            description = description,
            author = author,
            sourceName = source?.name,
            imageUrl = urlToImage,
            publishedAtMillis = publishedAt?.let(::parseInstantMillis),
            position = position
        )
    }

    private fun parseInstantMillis(value: String): Long? =
        runCatching { Instant.parse(value).toEpochMilliseconds() }.getOrNull()
}
