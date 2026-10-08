package com.dennymathew.newstime.ui.headlines

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
    val articles: List<ArticleEntity> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: HeadlinesError? = null
)

enum class HeadlinesError { Network, Unauthorized, Server }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val status = MutableStateFlow(HeadlinesUiState())
    private var refreshJob: Job? = null

    val uiState: StateFlow<HeadlinesUiState> =
        combine(
            status.map { it.category }.distinctUntilChanged()
                .flatMapLatest { repository.articles(it) },
            status
        ) { articles, status ->
            // Ignore a list still emitted for the previous category during a switch.
            status.copy(articles = articles.filter { it.category == status.category.name })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HeadlinesUiState())

    init {
        refresh(force = false)
    }

    fun selectCategory(category: NewsCategory) {
        if (category == status.value.category) return
        refreshJob?.cancel()
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
}
