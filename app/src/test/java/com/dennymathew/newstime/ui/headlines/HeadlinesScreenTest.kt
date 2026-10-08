package com.dennymathew.newstime.ui.headlines

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.dennymathew.newstime.data.NewsCategory
import com.dennymathew.newstime.data.local.ArticleEntity
import com.dennymathew.newstime.ui.theme.NewsTimeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HeadlinesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val openedUris = mutableListOf<String>()
    private var refreshes = 0
    private var errorsShown = 0
    private val selectedCategories = mutableListOf<NewsCategory>()

    // Most tests start from a loaded cache; pass loaded = false to test the loading frame.
    private fun setScreen(state: HeadlinesUiState, loaded: Boolean = true) {
        val shown = if (loaded) state.copy(isLoaded = true) else state
        composeRule.setContent { Screen(shown) }
    }

    @Composable
    private fun Screen(state: HeadlinesUiState) {
        NewsTimeTheme {
            HeadlinesScreen(
                state = state,
                onRefresh = { refreshes++ },
                onErrorShown = { errorsShown++ },
                onCategorySelected = { selectedCategories += it },
                onArticleClick = { openedUris += it.url }
            )
        }
    }

    @Test
    fun showsTitleAndArticles() {
        setScreen(HeadlinesUiState(articles = listOf(article(1), article(2))))

        composeRule.onNodeWithText("Top News of the Hour").assertExists()
        composeRule.onNodeWithText("Headline 1").assertExists()
        composeRule.onNodeWithText("Summary 1").assertExists()
        composeRule.onNodeWithText("Headline 2").assertExists()
    }

    @Test
    fun showsSourceAndRelativePublishTime() {
        val twoHoursAgo = System.currentTimeMillis() - 2 * 60 * 60 * 1000L
        setScreen(HeadlinesUiState(articles = listOf(article(1, publishedAtMillis = twoHoursAgo))))

        composeRule.onNodeWithText("Associated Press · 2 hours ago").assertExists()
    }

    @Test
    fun showsSourceAloneWhenPublishTimeIsUnknown() {
        setScreen(HeadlinesUiState(articles = listOf(article(1, publishedAtMillis = null))))

        composeRule.onNodeWithText("Associated Press").assertExists()
    }

    @Test
    fun scrollPosition_survivesSaveAndRestore() {
        val restorationTester = StateRestorationTester(composeRule)
        val articles = (1..30).map { article(it) }
        restorationTester.setContent {
            Screen(HeadlinesUiState(isLoaded = true, articles = articles))
        }
        composeRule.onNodeWithTag(ARTICLE_LIST_TAG).performScrollToIndex(20)
        composeRule.onNodeWithText("Headline 21").assertIsDisplayed()

        // What happens when Android reclaims the app while an article is open.
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText("Headline 21").assertIsDisplayed()
        composeRule.onNodeWithText("Headline 1").assertDoesNotExist()
    }

    @Test
    fun beforeCacheIsRead_showsNeitherListNorEmptyState() {
        setScreen(HeadlinesUiState(), loaded = false)

        composeRule.onNodeWithText("No headlines yet. Pull down to refresh.").assertDoesNotExist()
        composeRule.onNodeWithTag(ARTICLE_LIST_TAG).assertDoesNotExist()
    }

    @Test
    fun showsCategoryChipsWithCurrentOneSelected() {
        setScreen(HeadlinesUiState(category = NewsCategory.Business))

        composeRule.onNodeWithText("Top").assertIsNotSelected()
        composeRule.onNodeWithText("Business").assertIsSelected()
    }

    @Test
    fun tappingChipSelectsCategory() {
        setScreen(HeadlinesUiState())

        composeRule.onNodeWithText("Technology").performClick()

        assertEquals(listOf(NewsCategory.Technology), selectedCategories)
    }

    @Test
    fun showsEmptyStateWhenNothingIsCached() {
        setScreen(HeadlinesUiState())

        composeRule.onNodeWithText("No headlines yet. Pull down to refresh.").assertExists()
    }

    @Test
    fun hidesEmptyStateWhileRefreshing() {
        setScreen(HeadlinesUiState(isRefreshing = true))

        composeRule.onNodeWithText("No headlines yet. Pull down to refresh.").assertDoesNotExist()
    }

    @Test
    fun tappingArticleOpensItsUrl() {
        setScreen(HeadlinesUiState(articles = listOf(article(1))))

        composeRule.onNodeWithText("Headline 1").performClick()

        assertEquals(listOf("https://example.com/1"), openedUris)
    }

    @Test
    fun errorShowsMessageAndIsMarkedShown() {
        setScreen(HeadlinesUiState(articles = listOf(article(1)), error = HeadlinesError.Unauthorized))

        composeRule.onNodeWithText("News API key is missing or invalid. See the README.").assertExists()
        composeRule.mainClock.advanceTimeBy(10_000)
        composeRule.waitForIdle()
        assertEquals(1, errorsShown)
    }

    @Test
    fun pullingDownRequestsRefresh() {
        setScreen(HeadlinesUiState(articles = listOf(article(1))))

        // Drag from the first card well past the refresh threshold, as a person would.
        composeRule.onNodeWithText("Headline 1").performTouchInput {
            swipeDown(startY = top, endY = top + 1_500f, durationMillis = 500)
        }
        composeRule.waitForIdle()

        assertTrue(refreshes > 0)
    }

    private fun article(id: Int, publishedAtMillis: Long? = null) = ArticleEntity(
        category = NewsCategory.Top.name,
        url = "https://example.com/$id",
        title = "Headline $id",
        description = "Summary $id",
        author = null,
        sourceName = "Associated Press",
        imageUrl = null,
        publishedAtMillis = publishedAtMillis,
        position = id
    )
}
