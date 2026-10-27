package com.dennymathew.newstime.ui.headlines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennymathew.newstime.data.NewsCategory
import com.dennymathew.newstime.data.NewsRepository
import com.dennymathew.newstime.data.local.ArticleContent
import com.dennymathew.newstime.data.local.ArticleEntity
import com.dennymathew.newstime.data.local.SavedArticleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class HeadlinesUiState(
    /** The headline feed; kept while [showingSaved] so switching back returns to it. */
    val category: NewsCategory = NewsCategory.Top,
    /** True when the Saved chip is selected instead of a category. */
    val showingSaved: Boolean = false,
    /** False until the list being shown (headlines or saved) has been read at least once. */
    val isLoaded: Boolean = false,
    val articles: List<ArticleEntity> = emptyList(),
    /** Saved articles, newest saved first. */
    val savedArticles: List<SavedArticleEntity> = emptyList(),
    val savedUrls: Set<String> = emptySet(),
    val isRefreshing: Boolean = false,
    val error: HeadlinesError? = null
)

enum class HeadlinesError { Network, Unauthorized, Server }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    // The selected chip survives the app being reclaimed while an article is open.
    private val status = MutableStateFlow(
        HeadlinesUiState(
            category = savedState.get<String>(KEY_CATEGORY)
                ?.let { name -> NewsCategory.entries.firstOrNull { it.name == name } }
                ?: NewsCategory.Top,
            showingSaved = savedState.get<Boolean>(KEY_SHOWING_SAVED) ?: false
        )
    )
    private var refreshJob: Job? = null

    val uiState: StateFlow<HeadlinesUiState> =
        combine(
            status.map { it.category }.distinctUntilChanged()
                .flatMapLatest { category -> repository.articles(category).map { category to it } },
            repository.savedArticles(),
            repository.savedUrls(),
            status
        ) { (listCategory, articles), saved, savedUrls, status ->
            // During a switch the previous category's list can still arrive; ignore it.
            val articlesLoaded = listCategory == status.category
            status.copy(
                isLoaded = status.showingSaved || articlesLoaded,
                articles = if (articlesLoaded) articles else status.articles,
                savedArticles = saved,
                savedUrls = savedUrls
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), status.value)

    init {
        refresh(force = false)
    }

    fun selectCategory(category: NewsCategory) {
        val current = status.value
        if (category == current.category) {
            // Coming back from Saved to the same feed: its list is still loaded.
            if (current.showingSaved) {
                savedState[KEY_SHOWING_SAVED] = false
                status.update { it.copy(showingSaved = false) }
            }
            return
        }
        refreshJob?.cancel()
        savedState[KEY_CATEGORY] = category.name
        savedState[KEY_SHOWING_SAVED] = false
        status.value = HeadlinesUiState(category = category)
        refresh(force = false)
    }

    /** Shows saved articles; they come from the database only, so this works offline. */
    fun selectSaved() {
        if (status.value.showingSaved) return
        savedState[KEY_SHOWING_SAVED] = true
        status.update { it.copy(showingSaved = true) }
    }

    fun toggleSaved(article: ArticleContent) {
        viewModelScope.launch { repository.toggleSaved(article) }
    }

    fun refresh(force: Boolean = true) {
        if (refreshJob?.isActive == true) return
        val category = status.value.category
        refreshJob = viewModelScope.launch {
            status.update { it.copy(isRefreshing = true, error = null) }
            val error = try {
                repository.refresh(category, force)
                null
            } catch (e: IOException) {
                HeadlinesError.Network
            } catch (e: HttpException) {
                if (e.code() == 401) HeadlinesError.Unauthorized else HeadlinesError.Server
            }
            status.update { it.copy(isRefreshing = false, error = error) }
        }
    }

    fun errorShown() {
        status.update { it.copy(error = null) }
    }

    private companion object {
        const val KEY_CATEGORY = "category"
        const val KEY_SHOWING_SAVED = "showingSaved"
    }
}
