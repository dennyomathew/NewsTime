package com.dennymathew.newstime.ui.headlines

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
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

    private fun setScreen(state: HeadlinesUiState) {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalUriHandler provides object : UriHandler {
                    override fun openUri(uri: String) {
                        openedUris += uri
                    }
                }
            ) {
                NewsTimeTheme {
                    HeadlinesScreen(
                        state = state,
                        onRefresh = { refreshes++ },
                        onErrorShown = { errorsShown++ }
                    )
                }
            }
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

    private fun article(id: Int) = ArticleEntity(
        url = "https://example.com/$id",
        title = "Headline $id",
        description = "Summary $id",
        author = null,
        imageUrl = null,
        publishedAt = null,
        position = id
    )
}
