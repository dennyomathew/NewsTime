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

    /** Cached headlines for [category]; the database is the single source of truth. */
    fun articles(category: NewsCategory): Flow<List<ArticleEntity>> =
        dao.observeArticles(category.name)

    /**
     * Fetches fresh headlines for [category] unless its cache was refreshed within [maxAge];
     * refresh times are persisted per category, so reopening the app or switching back to a
     * category doesn't refetch. Network and HTTP errors propagate so the caller can show them;
     * the cache is kept.
     */
    suspend fun refresh(category: NewsCategory, force: Boolean = false) {
        if (!force && isCacheFresh(category)) return

        val response = api.getTopHeadlines(
            sources = category.source,
            category = category.apiCategory,
            country = category.country
        )
        val entities = response.articles.mapIndexedNotNull { index, dto ->
            dto.toEntity(category, position = index)
        }
        dao.replaceAll(category.name, entities)
        refreshTimes.setLastRefreshMillis(category.name, now())
    }

    private suspend fun isCacheFresh(category: NewsCategory): Boolean {
        val last = refreshTimes.lastRefreshMillis(category.name) ?: return false
        val age = now() - last
        // A negative age means the device clock moved backwards; treat that as stale.
        return age in 0 until maxAge.inWholeMilliseconds && dao.count(category.name) > 0
    }

    private fun ArticleDto.toEntity(category: NewsCategory, position: Int): ArticleEntity? {
        return ArticleEntity(
            category = category.name,
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
