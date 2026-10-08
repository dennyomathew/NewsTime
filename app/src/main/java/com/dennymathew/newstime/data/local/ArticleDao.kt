package com.dennymathew.newstime.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE category = :category ORDER BY position ASC")
    fun observeArticles(category: String): Flow<List<ArticleEntity>>

    @Query("SELECT COUNT(*) FROM articles WHERE category = :category")
    suspend fun count(category: String): Int

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles WHERE category = :category")
    suspend fun clear(category: String)

    /** Replaces one category's articles, leaving other categories' caches alone. */
    @Transaction
    suspend fun replaceAll(category: String, articles: List<ArticleEntity>) {
        clear(category)
        upsertAll(articles)
    }
}
