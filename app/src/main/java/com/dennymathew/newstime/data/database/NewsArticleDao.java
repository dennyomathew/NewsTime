package com.dennymathew.newstime.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.dennymathew.newstime.data.model.NewsArticle;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface NewsArticleDao {

    @Query("SELECT * FROM NewsArticle")
    LiveData<List<NewsArticle>> getTopNewsArticles();

    @Insert(onConflict = REPLACE)
    void insertNewsArticles(List<NewsArticle> newsArticles);

}
