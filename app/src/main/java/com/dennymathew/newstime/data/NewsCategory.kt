package com.dennymathew.newstime.data

import androidx.annotation.StringRes
import com.dennymathew.newstime.R

/**
 * A headline feed. [Top] is the Associated Press feed; the others are News API's US
 * top-headline categories. News API doesn't allow combining `sources` with `category`.
 */
enum class NewsCategory(
    @StringRes val labelRes: Int,
    val source: String? = null,
    val apiCategory: String? = null
) {
    Top(R.string.category_top, source = "associated-press"),
    Business(R.string.category_business, apiCategory = "business"),
    Technology(R.string.category_technology, apiCategory = "technology"),
    Sports(R.string.category_sports, apiCategory = "sports"),
    Entertainment(R.string.category_entertainment, apiCategory = "entertainment"),
    Health(R.string.category_health, apiCategory = "health"),
    Science(R.string.category_science, apiCategory = "science");

    /** Country filter; only valid together with a category. */
    val country: String? get() = if (apiCategory != null) "us" else null
}
