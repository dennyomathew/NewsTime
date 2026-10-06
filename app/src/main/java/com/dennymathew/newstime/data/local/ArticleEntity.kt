package com.dennymathew.newstime.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val title: String,
    val description: String?,
    val author: String?,
    val imageUrl: String?,
    val publishedAt: String?,
    // Preserves the API's ordering (newest first) across refreshes.
    val position: Int
)
