package com.dennymathew.newstime.ui.headlines

import android.content.Intent
import com.dennymathew.newstime.data.isWebUrl
import com.dennymathew.newstime.data.local.ArticleContent

/**
 * A plain-text `ACTION_SEND` for [article]: the headline and its link, so the message reads well
 * in apps that don't preview links. Implicit on purpose (the user picks the target in the
 * chooser); it carries only text, so no URI permission flags. Null unless the URL is http(s):
 * the cache already drops other links, this is a second guard.
 */
internal fun shareArticleIntent(article: ArticleContent): Intent? {
    if (!isWebUrl(article.url)) return null
    return Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.url}")
        putExtra(Intent.EXTRA_SUBJECT, article.title)
        putExtra(Intent.EXTRA_TITLE, article.title)
    }
}
