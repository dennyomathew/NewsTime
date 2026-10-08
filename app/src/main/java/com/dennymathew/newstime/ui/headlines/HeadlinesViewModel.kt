package com.dennymathew.newstime.ui.headlines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennymathew.newstime.data.NewsCategory
import com.dennymathew.newstime.data.NewsRepository
import com.dennymathew.newstime.data.local.ArticleEntity
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
    val category: NewsCategory = NewsCategory.Top,
    /** False until the cached articles for [category] have been read at least once. */
    val isLoaded: Boolean = false,
    val articles: List<ArticleEntity> = emptyList(),
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
                ?: NewsCategory.Top
        )
    )
    private var refreshJob: Job? = null

    val uiState: StateFlow<HeadlinesUiState> =
        combine(
            status.map { it.category }.distinctUntilChanged()
                .flatMapLatest { category -> repository.articles(category).map { category to it } },
            status
        ) { (listCategory, articles), status ->
            // During a switch the previous category's list can still arrive; ignore it.
            if (listCategory == status.category) {
                status.copy(isLoaded = true, articles = articles)
            } else {
                status
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), status.value)

    init {
        refresh(force = false)
    }

    fun selectCategory(category: NewsCategory) {
        if (category == status.value.category) return
        refreshJob?.cancel()
        savedState[KEY_CATEGORY] = category.name
        status.value = HeadlinesUiState(category = category)
        refresh(force = false)
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
    }
}
