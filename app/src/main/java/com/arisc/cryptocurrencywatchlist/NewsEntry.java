package com.arisc.cryptocurrencywatchlist;

public class NewsEntry {

    //Getters/Setters.
    private String mSource;
    private String mAuthor;
    private String mTitle;
    private String mDescription;
    private String mUrl;
    private String mImageUrl;

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        this.mSource = source;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

}
