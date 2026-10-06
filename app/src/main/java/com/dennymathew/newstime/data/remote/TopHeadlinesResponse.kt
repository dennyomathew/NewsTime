package com.dennymathew.newstime.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TopHeadlinesResponse(
    val status: String = "",
    val totalResults: Int = 0,
    val articles: List<ArticleDto> = emptyList()
)

@Serializable
data class ArticleDto(
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null
)
