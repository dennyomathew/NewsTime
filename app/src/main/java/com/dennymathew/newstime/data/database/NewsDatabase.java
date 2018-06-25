package com.dennymathew.newstime.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.dennymathew.newstime.data.model.NewsArticle;

@Database(entities =  {NewsArticle.class}, version = 1)
public abstract class NewsDatabase extends RoomDatabase{

    public abstract NewsArticleDao newsArticleDao();
}
