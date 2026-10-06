package com.dennymathew.newstime.data

import com.dennymathew.newstime.data.local.ArticleDao
import com.dennymathew.newstime.data.local.ArticleEntity
import com.dennymathew.newstime.data.remote.NewsApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@Singleton
class NewsRepository(
    private val api: NewsApi,
    private val dao: ArticleDao,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val maxAge: Duration = 1.hours
) {
    @Inject
    constructor(api: NewsApi, dao: ArticleDao) : this(api, dao, TimeSource.Monotonic)

    private var lastFetchMark: TimeMark? = null

    /** Cached headlines; the database is the single source of truth. */
    val articles: Flow<List<ArticleEntity>> = dao.observeArticles()

    /**
     * Fetches fresh headlines unless the cache was refreshed within [maxAge].
     * Network and HTTP errors propagate so the caller can show them; the cache is kept.
     */
    suspend fun refresh(force: Boolean = false) {
        val mark = lastFetchMark
        if (!force && mark != null && mark.elapsedNow() < maxAge) return

        val response = api.getTopHeadlines()
        val entities = response.articles.mapIndexedNotNull { index, dto ->
            val url = dto.url ?: return@mapIndexedNotNull null
            val title = dto.title ?: return@mapIndexedNotNull null
            ArticleEntity(
                url = url,
                title = title,
                description = dto.description,
                author = dto.author,
                imageUrl = dto.urlToImage,
                publishedAt = dto.publishedAt,
                position = index
            )
        }
        dao.replaceAll(entities)
        lastFetchMark = timeSource.markNow()
    }
}
