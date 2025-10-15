package com.way2.news.models;

import java.util.Date;

public class News {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private String category;
    private String language;
    private String sourceUrl;
    private String author;
    private String authorId; // Firestore author user id
    private Date timestamp;
    private boolean isBookmarked;
    private int viewCount;
    private boolean isBreaking;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private boolean isLiked;
    private boolean isDisliked;

    // Default constructor
    public News() {}

    // Constructor with essential fields
    public News(String id, String title, String summary, String imageUrl, String category, String language) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.category = category;
        this.language = language;
        this.timestamp = new Date();
        this.isBookmarked = false;
        this.viewCount = 0;
        this.isBreaking = false;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.commentCount = 0;
        this.isLiked = false;
        this.isDisliked = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isBreaking() {
        return isBreaking;
    }

    public void setBreaking(boolean breaking) {
        isBreaking = breaking;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isDisliked() {
        return isDisliked;
    }

    public void setDisliked(boolean disliked) {
        isDisliked = disliked;
    }

    @Override
    public String toString() {
        return "News{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", language='" + language + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
