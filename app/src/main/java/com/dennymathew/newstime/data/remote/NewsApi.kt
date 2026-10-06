package com.dennymathew.newstime.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("sources") sources: String = DEFAULT_SOURCE
    ): TopHeadlinesResponse

    companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val DEFAULT_SOURCE = "associated-press"
    }
}
