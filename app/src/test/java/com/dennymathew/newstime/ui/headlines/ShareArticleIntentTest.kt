package com.dennymathew.newstime.ui.headlines

import android.content.Intent
import com.dennymathew.newstime.data.local.SavedArticleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareArticleIntentTest {

    @Test
    fun buildsPlainTextSendWithTitleAndLink() {
        val intent = requireIntent(shareArticleIntent(article("https://example.com/story")))

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("Big news\nhttps://example.com/story", intent.getStringExtra(Intent.EXTRA_TEXT))
        assertEquals("Big news", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Big news", intent.getStringExtra(Intent.EXTRA_TITLE))
        // Implicit, text only: no target component and no URI permission grants.
        assertNull(intent.component)
        assertEquals(0, intent.flags)
    }

    @Test
    fun acceptsHttpLinks() {
        assertNotNull(shareArticleIntent(article("http://example.com/story")))
    }

    @Test
    fun refusesNonWebLinks() {
        assertNull(shareArticleIntent(article("intent://example.com#Intent;end")))
        assertNull(shareArticleIntent(article("file:///sdcard/secret")))
        assertNull(shareArticleIntent(article("javascript:alert(1)")))
    }

    private fun requireIntent(intent: Intent?): Intent {
        assertNotNull("expected a share intent", intent)
        return intent!!
    }

    private fun article(url: String) = SavedArticleEntity(
        url = url,
        title = "Big news",
        description = null,
        author = null,
        sourceName = null,
        imageUrl = null,
        publishedAtMillis = null,
        savedAtMillis = 0
    )
}
