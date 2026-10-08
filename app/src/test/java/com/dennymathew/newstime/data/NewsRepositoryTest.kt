package com.dennymathew.newstime.data

import com.dennymathew.newstime.FakeNewsApi
import com.dennymathew.newstime.FakeRefreshTimeStore
import com.dennymathew.newstime.HeadlinesRequest
import com.dennymathew.newstime.data.NewsCategory
import com.dennymathew.newstime.data.NewsCategory.Business
import com.dennymathew.newstime.data.NewsCategory.Top
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
        api.onGetTopHeadlines = { _ -> headlines(1..3) }

        repository.refresh(Top)

        assertEquals(listOf("Headline 1", "Headline 2", "Headline 3"), titles())
    }

    @Test
    fun repeatedRefreshes_doNotDuplicateArticles() = runTest {
        repository.refresh(Top, force = true)
        repository.refresh(Top, force = true)

        assertEquals(3, titles().size)
    }

    @Test
    fun refresh_replacesStoriesThatDroppedOffTheFeed() = runTest {
        api.onGetTopHeadlines = { _ -> headlines(1..3) }
        repository.refresh(Top)

        api.onGetTopHeadlines = { _ -> headlines(3..4) }
        repository.refresh(Top, force = true)

        assertEquals(listOf("Headline 3", "Headline 4"), titles())
    }

    @Test
    fun refresh_withinAnHour_usesCache() = runTest {
        repository.refresh(Top)
        nowMillis += 59.minutes.inWholeMilliseconds

        repository.refresh(Top)

        assertEquals(1, api.requests)
    }

    @Test
    fun refresh_afterAnHour_fetchesAgain() = runTest {
        repository.refresh(Top)
        nowMillis += 61.minutes.inWholeMilliseconds

        repository.refresh(Top)

        assertEquals(2, api.requests)
    }

    @Test
    fun forcedRefresh_ignoresCacheAge() = runTest {
        repository.refresh(Top)

        repository.refresh(Top, force = true)

        assertEquals(2, api.requests)
    }

    @Test
    fun refresh_skipsArticlesWithoutUrlOrTitle() = runTest {
        api.onGetTopHeadlines = { _ ->
            TopHeadlinesResponse(
                articles = listOf(articleDto(1), articleDto(2, url = null), articleDto(3, title = null))
            )
        }

        repository.refresh(Top)

        assertEquals(listOf("Headline 1"), titles())
    }

    @Test
    fun failedRefresh_keepsCachedArticles() = runTest {
        repository.refresh(Top)
        api.onGetTopHeadlines = { _ -> throw IOException("offline") }

        runCatching { repository.refresh(Top, force = true) }

        assertEquals(3, titles().size)
    }

    @Test
    fun failedRefresh_doesNotCountAsFetched() = runTest {
        api.onGetTopHeadlines = { _ -> throw IOException("offline") }
        runCatching { repository.refresh(Top) }

        api.onGetTopHeadlines = { _ -> headlines(1..5) }
        repository.refresh(Top)

        assertEquals(2, api.requests)
        assertEquals(5, titles().size)
    }

    @Test
    fun reopeningAppWithinAnHour_usesCache() = runTest {
        repository.refresh(Top)
        nowMillis += 30.minutes.inWholeMilliseconds

        newRepository().refresh(Top)

        assertEquals(1, api.requests)
    }

    @Test
    fun reopeningAppAfterAnHour_fetchesAgain() = runTest {
        repository.refresh(Top)
        nowMillis += 61.minutes.inWholeMilliseconds

        newRepository().refresh(Top)

        assertEquals(2, api.requests)
    }

    @Test
    fun freshTimestampWithEmptyCache_stillFetches() = runTest {
        // e.g. the database was recreated after a schema change.
        refreshTimes.times[Top.name] = nowMillis

        repository.refresh(Top)

        assertEquals(1, api.requests)
    }

    @Test
    fun clockMovedBackwards_treatsCacheAsStale() = runTest {
        repository.refresh(Top)
        nowMillis -= 5.minutes.inWholeMilliseconds

        repository.refresh(Top)

        assertEquals(2, api.requests)
    }

    @Test
    fun refresh_storesSourceAndPublishTime() = runTest {
        api.onGetTopHeadlines = { _ ->
            TopHeadlinesResponse(articles = listOf(articleDto(1), articleDto(2, publishedAt = "not a date")))
        }

        repository.refresh(Top)

        val (first, second) = repository.articles(Top).first()
        assertEquals("Associated Press", first.sourceName)
        assertEquals(1_791_322_200_000L, first.publishedAtMillis)
        assertNull(second.publishedAtMillis)
    }

    @Test
    fun topFeed_requestsAssociatedPress() = runTest {
        repository.refresh(Top)

        assertEquals(HeadlinesRequest("associated-press", null, null), api.calls.single())
    }

    @Test
    fun categoryFeed_requestsUsCategoryHeadlines() = runTest {
        repository.refresh(Business)

        assertEquals(HeadlinesRequest(null, "business", "us"), api.calls.single())
    }

    @Test
    fun categories_areCachedSeparately() = runTest {
        api.onGetTopHeadlines = { request ->
            if (request.category == "business") headlines(3..4) else headlines(1..3)
        }

        repository.refresh(Top)
        repository.refresh(Business)

        // Story 3 is in both feeds; refreshing Business didn't remove it from Top.
        assertEquals(listOf("Headline 1", "Headline 2", "Headline 3"), titles(Top))
        assertEquals(listOf("Headline 3", "Headline 4"), titles(Business))
    }

    @Test
    fun cacheAge_isTrackedPerCategory() = runTest {
        repository.refresh(Top)
        nowMillis += 10.minutes.inWholeMilliseconds

        repository.refresh(Business) // never fetched: goes to the network
        repository.refresh(Top) // fetched 10 minutes ago: cached

        assertEquals(2, api.requests)
    }

    @Test
    fun imageUrls_areMadeLoadable() = runTest {
        api.onGetTopHeadlines = { _ ->
            TopHeadlinesResponse(
                articles = listOf(
                    articleDto(1, urlToImage = "https://img.example.com/1.jpg"),
                    articleDto(2, urlToImage = "http://img.example.com/2.jpg"),
                    articleDto(3, urlToImage = "//img.example.com/3.jpg"),
                    articleDto(4, urlToImage = "  "),
                    articleDto(5, urlToImage = "not a url")
                )
            )
        }

        repository.refresh(Top)

        assertEquals(
            listOf(
                "https://img.example.com/1.jpg",
                "https://img.example.com/2.jpg",
                "https://img.example.com/3.jpg",
                null,
                null
            ),
            repository.articles(Top).first().map { it.imageUrl }
        )
    }

    @Test
    fun sourceNamesThatAreLinks_showJustTheSite() = runTest {
        api.onGetTopHeadlines = { _ ->
            TopHeadlinesResponse(
                articles = listOf(
                    articleDto(1, sourceName = "Associated Press"),
                    articleDto(2, sourceName = "http://mp1st.com/category/news"),
                    articleDto(3, sourceName = "https://www.example.com"),
                    articleDto(4, sourceName = " ")
                )
            )
        }

        repository.refresh(Top)

        assertEquals(
            listOf("Associated Press", "mp1st.com", "example.com", null),
            repository.articles(Top).first().map { it.sourceName }
        )
    }

    @Test(expected = HttpException::class)
    fun httpErrors_propagate() = runTest {
        api.onGetTopHeadlines = { _ -> throw httpError(401) }

        repository.refresh(Top)
    }

    private suspend fun titles(category: NewsCategory = Top) =
        repository.articles(category).first().map { it.title }
}
