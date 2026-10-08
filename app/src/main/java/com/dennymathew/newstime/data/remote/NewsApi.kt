package com.dennymathew.newstime.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    /** Null parameters are left out of the request. */
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("sources") sources: String? = null,
        @Query("category") category: String? = null,
        @Query("country") country: String? = null
    ): TopHeadlinesResponse

    companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
    }
}
