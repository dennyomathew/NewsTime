package com.dennymathew.newstime.data.local

import androidx.room.Entity

/** One headline within one [category] feed; the same story can appear in several feeds. */
@Entity(tableName = "articles", primaryKeys = ["category", "url"])
data class ArticleEntity(
    val category: String,
    override val url: String,
    override val title: String,
    override val description: String?,
    override val author: String?,
    override val sourceName: String?,
    override val imageUrl: String?,
    override val publishedAtMillis: Long?,
    // Preserves the API's ordering (newest first) across refreshes.
    val position: Int
) : ArticleContent
