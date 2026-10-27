package com.dennymathew.newstime.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Every schema version must upgrade without losing data; schemas live in `app/schemas`. */
@RunWith(RobolectricTestRunner::class)
class NewsDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NewsDatabase::class.java
    )

    @Test
    fun migrate3To4_keepsCachedHeadlinesAndAddsSavedArticles() = runTest {
        helper.createDatabase(DB_NAME, 3).apply {
            execSQL(
                "INSERT INTO articles (category, url, title, description, author, sourceName, " +
                    "imageUrl, publishedAtMillis, position) VALUES ('Top', 'https://example.com/1', " +
                    "'Headline 1', 'Summary 1', NULL, 'Associated Press', NULL, 42, 0)"
            )
            close()
        }

        // Validates the migrated schema against 4.json.
        helper.runMigrationsAndValidate(DB_NAME, 4, true, NewsDatabase.MIGRATION_3_4).close()

        val database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NewsDatabase::class.java,
            DB_NAME
        ).addMigrations(*NewsDatabase.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        try {
            val cached = database.articleDao().observeArticles("Top").first().single()
            assertEquals("Headline 1", cached.title)
            assertEquals(42L, cached.publishedAtMillis)
            assertEquals(emptyList<SavedArticleEntity>(), database.savedArticleDao().observeAll().first())

            database.savedArticleDao().toggle(
                SavedArticleEntity(
                    url = cached.url,
                    title = cached.title,
                    description = cached.description,
                    author = cached.author,
                    sourceName = cached.sourceName,
                    imageUrl = cached.imageUrl,
                    publishedAtMillis = cached.publishedAtMillis,
                    savedAtMillis = 7
                )
            )
            assertEquals(listOf("https://example.com/1"), database.savedArticleDao().observeUrls().first())
        } finally {
            database.close()
        }
    }

    private companion object {
        const val DB_NAME = "migration-test"
    }
}
