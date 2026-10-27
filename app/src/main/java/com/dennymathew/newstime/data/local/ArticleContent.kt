package com.dennymathew.newstime.data.local

/** What a headline card shows; shared by cached headlines and saved articles. */
interface ArticleContent {
    val url: String
    val title: String
    val description: String?
    val author: String?
    val sourceName: String?
    val imageUrl: String?
    val publishedAtMillis: Long?
}
