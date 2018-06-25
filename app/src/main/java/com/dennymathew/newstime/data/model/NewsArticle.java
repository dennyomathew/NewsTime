package com.dennymathew.newstime.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Denny on 3/30/2018.
 */

public class NewsArticle {

    @SerializedName(value="author")
    private String author;

    @SerializedName(value="title")
    private String title;

    @SerializedName(value="description")
    private String description;

    @SerializedName(value="url")
    private String url;

    @SerializedName(value="urlToImage")
    private String urlToImage;

    @SerializedName(value="publishedAt")
    private String publishedAt;
}
