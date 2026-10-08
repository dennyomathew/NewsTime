package com.dennymathew.newstime.ui.headlines

import androidx.lifecycle.SavedStateHandle
import com.dennymathew.newstime.FakeNewsApi
import com.dennymathew.newstime.FakeRefreshTimeStore
import com.dennymathew.newstime.HeadlinesRequest
import com.dennymathew.newstime.data.NewsCategory
import com.dennymathew.newstime.data.NewsRepository
import com.dennymathew.newstime.data.local.NewsDatabase
import com.dennymathew.newstime.headlines
import com.dennymathew.newstime.httpError
import com.dennymathew.newstime.inMemoryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HeadlinesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: NewsDatabase
    private val api = FakeNewsApi()
    private val savedState = SavedStateHandle()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        database = inMemoryDatabase()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        HeadlinesViewModel(
            NewsRepository(api, database.articleDao(), FakeRefreshTimeStore()),
            savedState
        )

    @Test
    fun init_loadsHeadlines() = runTest(dispatcher) {
        api.onGetTopHeadlines = { headlines(1..2) }

        val vm = viewModel()
        vm.uiState.launchIn(backgroundScope)

        val state = vm.uiState.value
        assertEquals(listOf("Headline 1", "Headline 2"), state.articles.map { it.title })
        assertFalse(state.isRefreshing)
        assertNull(state.error)
    }

    @Test
    fun offline_reportsNetworkErrorAndKeepsCachedArticles() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.launchIn(backgroundScope)

        api.onGetTopHeadlines = { throw IOException("offline") }
        vm.refresh()

        val state = vm.uiState.value
        assertEquals(HeadlinesError.Network, state.error)
        assertEquals(3, state.articles.size)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun badApiKey_reportsUnauthorized() = runTest(dispatcher) {
        api.onGetTopHeadlines = { throw httpError(401) }

        val vm = viewModel()
        vm.uiState.launchIn(backgroundScope)

        assertEquals(HeadlinesError.Unauthorized, vm.uiState.value.error)
    }

    @Test
    fun serverFailure_reportsServerError() = runTest(dispatcher) {
        api.onGetTopHeadlines = { throw httpError(500) }

        val vm = viewModel()
        vm.uiState.launchIn(backgroundScope)

        assertEquals(HeadlinesError.Server, vm.uiState.value.error)
    }

    @Test
    fun selectCategory_showsThatCategorysHeadlines() = runTest(dispatcher) {
        api.onGetTopHeadlines = { request ->
            if (request.category == "sports") headlines(10..11) else headlines(1..3)
        }
        val vm = viewModel()
        vm.uiState.launchIn(backgroundScope)

        vm.selectCategory(NewsCategory.Sports)

        val state = vm.uiState.value
        assertEquals(NewsCategory.Sports, state.category)
        assertEquals(listOf("Headline 10", "Headline 11"), state.articles.map { it.title })
    }

    @Test
    fun switchingBackToACategory_usesItsCache() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.launchIn(backgroundScope)

        vm.selectCategory(NewsCategory.Sports)
        vm.selectCategory(NewsCategory.Top)

        assertEquals(2, api.requests) // Top on start, Sports once; Top again is cached
        assertEquals(3, vm.uiState.value.articles.size)
    }

    @Test
    fun selectedCategory_isSavedForRestore() = runTest(dispatcher) {
        val vm = viewModel()

        vm.selectCategory(NewsCategory.Health)

        assertEquals("Health", savedState.get<String>("category"))
    }

    @Test
    fun restoredViewModel_reopensSavedCategory() = runTest(dispatcher) {
        // As after Android reclaimed the app while an article was open.
        savedState["category"] = "Science"

        val vm = viewModel()
        vm.uiState.launchIn(backgroundScope)

        assertEquals(NewsCategory.Science, vm.uiState.value.category)
        assertEquals(HeadlinesRequest(null, "science", "us"), api.calls.single())
    }

    @Test
    fun uiState_isLoadedOnceCacheIsRead() = runTest(dispatcher) {
        val vm = viewModel()
        assertFalse(vm.uiState.value.isLoaded)

        vm.uiState.launchIn(backgroundScope)

        assertTrue(vm.uiState.value.isLoaded)
    }

    @Test
    fun errorShown_clearsError() = runTest(dispatcher) {
        api.onGetTopHeadlines = { throw httpError(500) }
        val vm = viewModel()
        vm.uiState.launchIn(backgroundScope)

        vm.errorShown()

        assertNull(vm.uiState.value.error)
    }
}
