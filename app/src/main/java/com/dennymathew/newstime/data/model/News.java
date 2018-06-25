package com.dennymathew.newstime.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Denny on 3/30/2018.
 */


public class News {

    private String status;

    @SerializedName(value="totalResults")
    private int totalResults;

    @SerializedName(value="articles")
    List<NewsArticle> newsArticleLst;
}
