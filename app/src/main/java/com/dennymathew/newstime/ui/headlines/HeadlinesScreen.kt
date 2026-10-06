package com.dennymathew.newstime.ui.headlines

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dennymathew.newstime.R
import com.dennymathew.newstime.data.local.ArticleEntity
import com.dennymathew.newstime.ui.theme.NewsTimeTheme

@Composable
fun HeadlinesRoute(viewModel: HeadlinesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HeadlinesScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onErrorShown = viewModel::errorShown
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadlinesScreen(
    state: HeadlinesUiState,
    onRefresh: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = state.error?.let { stringResource(it.messageRes()) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.articles.isEmpty() && !state.isRefreshing) {
                EmptyState(Modifier.align(Alignment.Center))
            } else {
                ArticleList(state.articles)
            }
        }
    }
}

@Composable
private fun ArticleList(articles: List<ArticleEntity>) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles, key = { it.url }) { article ->
            ArticleCard(article, onClick = { uriHandler.openUri(article.url) })
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
            if (article.imageUrl != null) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
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
                        url = "https://example.com/1",
                        title = "Example headline",
                        description = "A short summary of the story.",
                        author = null,
                        imageUrl = null,
                        publishedAt = null,
                        position = 0
                    )
                )
            ),
            onRefresh = {},
            onErrorShown = {}
        )
    }
}
