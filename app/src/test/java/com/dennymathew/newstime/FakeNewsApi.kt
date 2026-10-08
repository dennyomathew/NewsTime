package com.dennymathew.newstime

import com.dennymathew.newstime.data.remote.ArticleDto
import com.dennymathew.newstime.data.remote.NewsApi
import com.dennymathew.newstime.data.remote.SourceDto
import com.dennymathew.newstime.data.remote.TopHeadlinesResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/** The query parameters of one top-headlines request. */
data class HeadlinesRequest(val sources: String?, val category: String?, val country: String?)

class FakeNewsApi : NewsApi {
    val calls = mutableListOf<HeadlinesRequest>()
    val requests: Int get() = calls.size
    var onGetTopHeadlines: (HeadlinesRequest) -> TopHeadlinesResponse = { headlines(1..3) }

    override suspend fun getTopHeadlines(
        sources: String?,
        category: String?,
        country: String?
    ): TopHeadlinesResponse {
        val request = HeadlinesRequest(sources, category, country)
        calls += request
        return onGetTopHeadlines(request)
    }
}

fun articleDto(
    id: Int,
    url: String? = "https://example.com/$id",
    title: String? = "Headline $id",
    publishedAt: String? = "2026-10-06T21:30:00Z"
) = ArticleDto(
    source = SourceDto(id = "associated-press", name = "Associated Press"),
    title = title,
    url = url,
    description = "Summary $id",
    urlToImage = null,
    publishedAt = publishedAt
)

fun headlines(ids: IntRange) = TopHeadlinesResponse(
    status = "ok",
    totalResults = ids.count(),
    articles = ids.map { articleDto(it) }
)

fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody(null)))
