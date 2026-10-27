package com.dennymathew.newstime.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {
    /** Newest saved first. */
    @Query("SELECT * FROM saved_articles ORDER BY savedAtMillis DESC, rowid DESC")
    fun observeAll(): Flow<List<SavedArticleEntity>>

    @Query("SELECT url FROM saved_articles")
    fun observeUrls(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE url = :url")
    suspend fun delete(url: String): Int

    /** Saves [article], or removes it if it's already saved. Returns whether it's now saved. */
    @Transaction
    suspend fun toggle(article: SavedArticleEntity): Boolean {
        if (delete(article.url) > 0) return false
        insert(article)
        return true
    }
}
