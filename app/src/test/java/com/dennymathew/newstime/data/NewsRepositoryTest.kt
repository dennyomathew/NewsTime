package com.dennymathew.newstime.data

import com.dennymathew.newstime.FakeNewsApi
import com.dennymathew.newstime.FakeRefreshTimeStore
import com.dennymathew.newstime.articleDto
import com.dennymathew.newstime.data.local.NewsDatabase
import com.dennymathew.newstime.headlines
import com.dennymathew.newstime.httpError
import com.dennymathew.newstime.inMemoryDatabase
import com.dennymathew.newstime.data.remote.TopHeadlinesResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import java.io.IOException
import kotlin.time.Duration.Companion.minutes

@RunWith(RobolectricTestRunner::class)
class NewsRepositoryTest {

    private lateinit var database: NewsDatabase
    private val api = FakeNewsApi()
    private val refreshTimes = FakeRefreshTimeStore()
    private var nowMillis = 1_000_000_000L
    private lateinit var repository: NewsRepository

    @Before
    fun setUp() {
        database = inMemoryDatabase()
        repository = newRepository()
    }

    // A new instance stands in for the app being reopened: only the stores carry over.
    private fun newRepository() =
        NewsRepository(api, database.articleDao(), refreshTimes, now = { nowMillis })

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refresh_storesArticlesInApiOrder() = runTest {
        api.onGetTopHeadlines = { headlines(1..3) }

        repository.refresh()

        assertEquals(listOf("Headline 1", "Headline 2", "Headline 3"), titles())
    }

    @Test
    fun repeatedRefreshes_doNotDuplicateArticles() = runTest {
        repository.refresh(force = true)
        repository.refresh(force = true)

        assertEquals(3, titles().size)
    }

    @Test
    fun refresh_replacesStoriesThatDroppedOffTheFeed() = runTest {
        api.onGetTopHeadlines = { headlines(1..3) }
        repository.refresh()

        api.onGetTopHeadlines = { headlines(3..4) }
        repository.refresh(force = true)

        assertEquals(listOf("Headline 3", "Headline 4"), titles())
    }

    @Test
    fun refresh_withinAnHour_usesCache() = runTest {
        repository.refresh()
        nowMillis += 59.minutes.inWholeMilliseconds

        repository.refresh()

        assertEquals(1, api.requests)
    }

    @Test
    fun refresh_afterAnHour_fetchesAgain() = runTest {
        repository.refresh()
        nowMillis += 61.minutes.inWholeMilliseconds

        repository.refresh()

        assertEquals(2, api.requests)
    }

    @Test
    fun forcedRefresh_ignoresCacheAge() = runTest {
        repository.refresh()

        repository.refresh(force = true)

        assertEquals(2, api.requests)
    }

    @Test
    fun refresh_skipsArticlesWithoutUrlOrTitle() = runTest {
        api.onGetTopHeadlines = {
            TopHeadlinesResponse(
                articles = listOf(articleDto(1), articleDto(2, url = null), articleDto(3, title = null))
            )
        }

        repository.refresh()

        assertEquals(listOf("Headline 1"), titles())
    }

    @Test
    fun failedRefresh_keepsCachedArticles() = runTest {
        repository.refresh()
        api.onGetTopHeadlines = { throw IOException("offline") }

        runCatching { repository.refresh(force = true) }

        assertEquals(3, titles().size)
    }

    @Test
    fun failedRefresh_doesNotCountAsFetched() = runTest {
        api.onGetTopHeadlines = { throw IOException("offline") }
        runCatching { repository.refresh() }

        api.onGetTopHeadlines = { headlines(1..5) }
        repository.refresh()

        assertEquals(2, api.requests)
        assertEquals(5, titles().size)
    }

    @Test
    fun reopeningAppWithinAnHour_usesCache() = runTest {
        repository.refresh()
        nowMillis += 30.minutes.inWholeMilliseconds

        newRepository().refresh()

        assertEquals(1, api.requests)
    }

    @Test
    fun reopeningAppAfterAnHour_fetchesAgain() = runTest {
        repository.refresh()
        nowMillis += 61.minutes.inWholeMilliseconds

        newRepository().refresh()

        assertEquals(2, api.requests)
    }

    @Test
    fun freshTimestampWithEmptyCache_stillFetches() = runTest {
        // e.g. the database was recreated after a schema change.
        refreshTimes.lastRefresh = nowMillis

        repository.refresh()

        assertEquals(1, api.requests)
    }

    @Test
    fun clockMovedBackwards_treatsCacheAsStale() = runTest {
        repository.refresh()
        nowMillis -= 5.minutes.inWholeMilliseconds

        repository.refresh()

        assertEquals(2, api.requests)
    }

    @Test
    fun refresh_storesSourceAndPublishTime() = runTest {
        api.onGetTopHeadlines = {
            TopHeadlinesResponse(articles = listOf(articleDto(1), articleDto(2, publishedAt = "not a date")))
        }

        repository.refresh()

        val (first, second) = repository.articles.first()
        assertEquals("Associated Press", first.sourceName)
        assertEquals(1_791_322_200_000L, first.publishedAtMillis)
        assertNull(second.publishedAtMillis)
    }

    @Test(expected = HttpException::class)
    fun httpErrors_propagate() = runTest {
        api.onGetTopHeadlines = { throw httpError(401) }

        repository.refresh()
    }

    private suspend fun titles() = repository.articles.first().map { it.title }
}
