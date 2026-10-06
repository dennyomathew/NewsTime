package com.dennymathew.newstime.di

import android.content.Context
import androidx.room.Room
import com.dennymathew.newstime.BuildConfig
import com.dennymathew.newstime.data.local.ArticleDao
import com.dennymathew.newstime.data.local.NewsDatabase
import com.dennymathew.newstime.data.remote.NewsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.SECONDS)
            .cache(Cache(context.cacheDir.resolve("http"), 10L * 1024 * 1024))
            .addInterceptor { chain ->
                // Send the key as a header so it never appears in logged URLs.
                chain.proceed(
                    chain.request().newBuilder()
                        .header("X-Api-Key", BuildConfig.API_KEY)
                        .header("Accept-Language", Locale.getDefault().language)
                        .build()
                )
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BASIC)
                            .apply { redactHeader("X-Api-Key") }
                    )
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideNewsApi(client: OkHttpClient): NewsApi {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(NewsApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NewsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NewsDatabase =
        Room.databaseBuilder(context, NewsDatabase::class.java, NewsDatabase.NAME).build()

    @Provides
    fun provideArticleDao(database: NewsDatabase): ArticleDao = database.articleDao()
}
