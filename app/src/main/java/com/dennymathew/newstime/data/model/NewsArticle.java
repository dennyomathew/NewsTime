package com.dennymathew.newstime.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Denny on 3/30/2018.
 */

@Entity
public class NewsArticle {

    @PrimaryKey(autoGenerate = true)
    private int id;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}
