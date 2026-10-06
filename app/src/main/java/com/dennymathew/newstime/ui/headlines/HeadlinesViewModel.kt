package com.dennymathew.newstime.ui.headlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennymathew.newstime.data.NewsRepository
import com.dennymathew.newstime.data.local.ArticleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class HeadlinesUiState(
    val articles: List<ArticleEntity> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: HeadlinesError? = null
)

enum class HeadlinesError { Network, Unauthorized, Server }

@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val status = MutableStateFlow(HeadlinesUiState())

    val uiState: StateFlow<HeadlinesUiState> =
        combine(repository.articles, status) { articles, status ->
            status.copy(articles = articles)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HeadlinesUiState())

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) {
        if (status.value.isRefreshing) return
        viewModelScope.launch {
            status.update { it.copy(isRefreshing = true, error = null) }
            val error = try {
                repository.refresh(force)
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
