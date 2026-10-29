package com.dennymathew.newstime.data

/**
 * Whether [url] is an http(s) link. Article URLs come from News API (untrusted), and only web
 * links are ever opened or shared, never other schemes (intent:, file:, content:, ...).
 */
internal fun isWebUrl(url: String): Boolean =
    url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true)
