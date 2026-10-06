package com.dennymathew.newstime

import com.dennymathew.newstime.data.remote.ArticleDto
import com.dennymathew.newstime.data.remote.NewsApi
import com.dennymathew.newstime.data.remote.TopHeadlinesResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class FakeNewsApi : NewsApi {
    var requests = 0
        private set
    var onGetTopHeadlines: () -> TopHeadlinesResponse = { headlines(1..3) }

    override suspend fun getTopHeadlines(sources: String): TopHeadlinesResponse {
        requests++
        return onGetTopHeadlines()
    }
}

fun articleDto(id: Int, url: String? = "https://example.com/$id", title: String? = "Headline $id") =
    ArticleDto(title = title, url = url, description = "Summary $id", urlToImage = null)

fun headlines(ids: IntRange) = TopHeadlinesResponse(
    status = "ok",
    totalResults = ids.count(),
    articles = ids.map { articleDto(it) }
)

fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody(null)))
