package com.dennymathew.newstime.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * `articles` is a cache that refreshes replace; `saved_articles` is user data. Every schema
 * change needs a version bump, a [Migration] in [ALL_MIGRATIONS], the generated
 * `app/schemas/.../<version>.json` committed, and a case in `NewsDatabaseMigrationTest`.
 */
@Database(
    entities = [ArticleEntity::class, SavedArticleEntity::class],
    version = 4,
    exportSchema = true
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun savedArticleDao(): SavedArticleDao

    companion object {
        const val NAME = "news_time.db"

        /** Adds saved articles. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `saved_articles` (`url` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, `description` TEXT, `author` TEXT, " +
                        "`sourceName` TEXT, `imageUrl` TEXT, `publishedAtMillis` INTEGER, " +
                        "`savedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`url`))"
                )
            }
        }

        val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_3_4)
    }
}
