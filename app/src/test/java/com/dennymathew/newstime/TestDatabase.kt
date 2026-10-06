package com.dennymathew.newstime

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.dennymathew.newstime.data.local.NewsDatabase

fun inMemoryDatabase(): NewsDatabase = Room.inMemoryDatabaseBuilder(
    ApplicationProvider.getApplicationContext(),
    NewsDatabase::class.java
).allowMainThreadQueries()
    // Run queries inline so tests don't race Room's background executors.
    .setQueryExecutor { it.run() }
    .setTransactionExecutor { it.run() }
    .build()
