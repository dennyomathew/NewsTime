package com.dennymathew.newstime.ui.headlines

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dennymathew.newstime.FakeNewsApi
import com.dennymathew.newstime.FakeRefreshTimeStore
import com.dennymathew.newstime.data.NewsRepository
import com.dennymathew.newstime.data.local.NewsDatabase
import com.dennymathew.newstime.headlines
import com.dennymathew.newstime.inMemoryDatabase
import com.dennymathew.newstime.ui.theme.NewsTimeTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

/** The screen wired to a real ViewModel, repository and in-memory database. */
@RunWith(RobolectricTestRunner::class)
class SavedArticlesFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var database: NewsDatabase
    private val api = FakeNewsApi().apply { onGetTopHeadlines = { headlines(1..2) } }

    @Before
    fun setUp() {
        database = inMemoryDatabase()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun showScreen() {
        val viewModel = HeadlinesViewModel(
            NewsRepository(
                api,
                database.articleDao(),
                database.savedArticleDao(),
                FakeRefreshTimeStore()
            ),
            SavedStateHandle()
        )
        composeRule.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            NewsTimeTheme {
                HeadlinesScreen(
                    state = state,
                    onRefresh = viewModel::refresh,
                    onErrorShown = viewModel::errorShown,
                    onCategorySelected = viewModel::selectCategory,
                    onSavedSelected = viewModel::selectSaved,
                    onToggleSaved = viewModel::toggleSaved,
                    onArticleClick = {}
                )
            }
        }
    }

    @Test
    fun savedView_isEmptyUntilSomethingIsSaved() {
        showScreen()

        savedChip().performClick()

        composeRule.onNodeWithText(SAVED_EMPTY).assertExists()
    }

    @Test
    fun tappingBookmark_savesArticleAndSavedViewShowsItOffline() {
        showScreen()
        composeRule.onNodeWithText("Headline 1").assertExists()

        composeRule.onAllNodesWithContentDescription("Save article")[1].performClick()
        composeRule.onNodeWithContentDescription("Remove from saved").assertIsOn()

        api.onGetTopHeadlines = { throw IOException("offline") }
        savedChip().performClick()

        composeRule.onNodeWithText("Headline 2").assertExists()
        composeRule.onNodeWithText("Headline 1").assertDoesNotExist()
        composeRule.onNodeWithText(SAVED_EMPTY).assertDoesNotExist()

        // Un-saving from the Saved view empties it.
        composeRule.onNodeWithContentDescription("Remove from saved").performClick()
        composeRule.onNodeWithText(SAVED_EMPTY).assertExists()
    }

    /** The Saved chip is last in the row, so it may first need scrolling into view. */
    private fun savedChip(): SemanticsNodeInteraction {
        composeRule.onNodeWithTag(CATEGORY_CHIPS_TAG).performScrollToNode(hasText("Saved"))
        return composeRule.onNodeWithText("Saved")
    }

    private companion object {
        const val SAVED_EMPTY = "No saved articles yet. Tap the bookmark on a headline to save it."
    }
}
