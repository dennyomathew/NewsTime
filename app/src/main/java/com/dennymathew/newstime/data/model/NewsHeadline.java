package com.dennymathew.newstime.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;

/**
 * Created by Denny on 3/30/2018.
 */

@Entity
public class NewsHeadline {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String status;
    private int totalResults;
    List<NewsArticle> newsArticleLst;
}
