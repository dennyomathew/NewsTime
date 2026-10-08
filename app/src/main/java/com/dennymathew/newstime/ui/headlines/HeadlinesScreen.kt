package com.dennymathew.newstime.ui.headlines

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dennymathew.newstime.R
import com.dennymathew.newstime.data.NewsCategory
import com.dennymathew.newstime.data.local.ArticleEntity
import com.dennymathew.newstime.ui.theme.NewsTimeTheme

@Composable
fun HeadlinesRoute(viewModel: HeadlinesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()
    HeadlinesScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onErrorShown = viewModel::errorShown,
        onCategorySelected = viewModel::selectCategory,
        onArticleClick = { article -> context.openInCustomTab(article.url, toolbarColor) }
    )
}

/** Opens [url] in a Chrome Custom Tab, falling back to any app that can view it. */
private fun Context.openInCustomTab(url: String, toolbarColor: Int) {
    val uri = url.toUri()
    val customTab = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder().setToolbarColor(toolbarColor).build()
        )
        .setShowTitle(true)
        .build()
    try {
        customTab.launchUrl(this, uri)
    } catch (e: ActivityNotFoundException) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            // No browser installed; nothing sensible to open the article with.
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadlinesScreen(
    state: HeadlinesUiState,
    onRefresh: () -> Unit,
    onErrorShown: () -> Unit,
    onCategorySelected: (NewsCategory) -> Unit,
    onArticleClick: (ArticleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Keeps each category's scroll position (also across the app being reclaimed while an
    // article is open), so coming back or switching chips returns to where you were.
    val listStates = rememberSaveableStateHolder()
    val errorMessage = state.error?.let { stringResource(it.messageRes()) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(title = { Text(stringResource(R.string.app_title)) })
                CategoryChips(selected = state.category, onSelected = onCategorySelected)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Drawing an empty list before the cache is read would reset the saved position.
                !state.isLoaded -> Unit
                state.articles.isEmpty() && !state.isRefreshing ->
                    EmptyState(Modifier.align(Alignment.Center))
                else -> listStates.SaveableStateProvider(state.category.name) {
                    ArticleList(state.articles, rememberLazyListState(), onArticleClick)
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(selected: NewsCategory, onSelected: (NewsCategory) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(NewsCategory.entries) { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelected(category) },
                label = { Text(stringResource(category.labelRes)) }
            )
        }
    }
}

@Composable
private fun ArticleList(
    articles: List<ArticleEntity>,
    listState: LazyListState,
    onArticleClick: (ArticleEntity) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag(ARTICLE_LIST_TAG),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles, key = { it.url }) { article ->
            ArticleCard(article, onClick = { onArticleClick(article) })
        }
    }
}

@Composable
private fun ArticleCard(article: ArticleEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            articleMeta(article)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Some links in News API data don't load (blocked, moved or not an image); rather
            // than leave an empty box, the card drops the image once loading fails.
            var imageFailed by remember(article.imageUrl) { mutableStateOf(false) }
            if (article.imageUrl != null && !imageFailed) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                    onError = { error ->
                        Log.w(TAG, "Image failed: ${article.imageUrl}", error.result.throwable)
                        imageFailed = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            article.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

internal const val ARTICLE_LIST_TAG = "articleList"
private const val TAG = "HeadlinesScreen"

/** "Associated Press · 2 hours ago", or whichever part is known. */
@Composable
private fun articleMeta(article: ArticleEntity): String? {
    val published = article.publishedAtMillis?.let {
        DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            .toString()
    }
    return listOfNotNull(article.sourceName, published).takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier.padding(32.dp)) {
        Text(
            text = stringResource(R.string.headlines_empty),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun HeadlinesError.messageRes(): Int = when (this) {
    HeadlinesError.Network -> R.string.error_network
    HeadlinesError.Unauthorized -> R.string.error_unauthorized
    HeadlinesError.Server -> R.string.error_server
}

@Preview
@Composable
private fun HeadlinesScreenPreview() {
    NewsTimeTheme {
        HeadlinesScreen(
            state = HeadlinesUiState(
                articles = listOf(
                    ArticleEntity(
                        category = NewsCategory.Top.name,
                        url = "https://example.com/1",
                        title = "Example headline",
                        description = "A short summary of the story.",
                        author = null,
                        sourceName = "Associated Press",
                        imageUrl = null,
                        publishedAtMillis = System.currentTimeMillis() - 2 * 60 * 60 * 1000L,
                        position = 0
                    )
                )
            ),
            onRefresh = {},
            onErrorShown = {},
            onCategorySelected = {},
            onArticleClick = {}
        )
    }
}
