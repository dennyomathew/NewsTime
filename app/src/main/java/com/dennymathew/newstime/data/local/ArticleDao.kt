package com.dennymathew.newstime.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY position ASC")
    fun observeArticles(): Flow<List<ArticleEntity>>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(articles: List<ArticleEntity>) {
        clearAll()
        upsertAll(articles)
    }
}
