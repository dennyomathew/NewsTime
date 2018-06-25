package com.dennymathew.newstime.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.dennymathew.newstime.data.model.NewsArticle;

import java.util.List;

@Dao
public interface NewsArticleDao {

    @Query("SELECT * FROM NewsArticle")
    LiveData<List<NewsArticle>> loadNews();


}
