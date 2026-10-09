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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
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
    // Links come from News API (untrusted); never hand other schemes (intent:, file:, ...)
    // to the system. The repository already drops such articles; this is a second guard.
    if (uri.scheme?.lowercase() !in setOf("http", "https")) return
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
        // Edge-to-edge: only the top bar's space is padded; the list draws behind the
        // navigation bar and gets the bottom/side insets as contentPadding instead.
        val layoutDirection = LocalLayoutDirection.current
        val listPadding = PaddingValues(
            start = padding.calculateStartPadding(layoutDirection) + 16.dp,
            top = 16.dp,
            end = padding.calculateEndPadding(layoutDirection) + 16.dp,
            bottom = padding.calculateBottomPadding() + 16.dp
        )
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(padding)
        ) {
            when {
                // Drawing an empty list before the cache is read would reset the saved position.
                !state.isLoaded -> Unit
                state.articles.isEmpty() && !state.isRefreshing ->
                    EmptyState(Modifier.align(Alignment.Center))
                else -> listStates.SaveableStateProvider(state.category.name) {
                    ArticleList(state.articles, rememberLazyListState(), listPadding, onArticleClick)
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
    contentPadding: PaddingValues,
    onArticleClick: (ArticleEntity) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag(ARTICLE_LIST_TAG),
        contentPadding = contentPadding,
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
        // Spacing sits on each item (not spacedBy) so an image that never loads leaves no gap.
        Column(Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            articleMeta(article)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            // The image takes no space until it has loaded: no empty box while loading, and
            // nothing at all if the link fails (News API links are sometimes blocked or moved).
            article.imageUrl?.let { imageUrl -> ArticleImage(imageUrl) }
            article.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ArticleImage(imageUrl: String) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth()
    ) {
        val imageState by painter.state.collectAsState()
        when (imageState) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            is AsyncImagePainter.State.Error -> LaunchedEffect(imageUrl) {
                val error = (imageState as AsyncImagePainter.State.Error).result.throwable
                Log.w(TAG, "Image failed: $imageUrl", error)
            }
            else -> Unit // Loading: take no space.
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
