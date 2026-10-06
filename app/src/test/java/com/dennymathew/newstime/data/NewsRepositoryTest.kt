package com.dennymathew.newstime.data

import com.dennymathew.newstime.FakeNewsApi
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import java.io.IOException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TestTimeSource

@RunWith(RobolectricTestRunner::class)
class NewsRepositoryTest {

    private lateinit var database: NewsDatabase
    private val api = FakeNewsApi()
    private val clock = TestTimeSource()
    private lateinit var repository: NewsRepository

    @Before
    fun setUp() {
        database = inMemoryDatabase()
        repository = NewsRepository(api, database.articleDao(), clock)
    }

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
        clock += 59.minutes

        repository.refresh()

        assertEquals(1, api.requests)
    }

    @Test
    fun refresh_afterAnHour_fetchesAgain() = runTest {
        repository.refresh()
        clock += 61.minutes

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

    @Test(expected = HttpException::class)
    fun httpErrors_propagate() = runTest {
        api.onGetTopHeadlines = { throw httpError(401) }

        repository.refresh()
    }

    private suspend fun titles() = repository.articles.first().map { it.title }
}
