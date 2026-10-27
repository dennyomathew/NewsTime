package com.dennymathew.newstime.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An article the user saved. It keeps its own copy of the headline because the `articles`
 * cache is replaced on every refresh; saved articles must outlive that.
 */
@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey override val url: String,
    override val title: String,
    override val description: String?,
    override val author: String?,
    override val sourceName: String?,
    override val imageUrl: String?,
    override val publishedAtMillis: Long?,
    val savedAtMillis: Long
) : ArticleContent
