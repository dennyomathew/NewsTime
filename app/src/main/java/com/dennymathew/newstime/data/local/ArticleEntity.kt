package com.dennymathew.newstime.data.local

import androidx.room.Entity

/** One headline within one [category] feed; the same story can appear in several feeds. */
@Entity(tableName = "articles", primaryKeys = ["category", "url"])
data class ArticleEntity(
    val category: String,
    val url: String,
    val title: String,
    val description: String?,
    val author: String?,
    val sourceName: String?,
    val imageUrl: String?,
    val publishedAtMillis: Long?,
    // Preserves the API's ordering (newest first) across refreshes.
    val position: Int
)
