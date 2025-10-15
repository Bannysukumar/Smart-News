package com.way2.news.models;

import java.util.Date;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
    private String phoneNumber;
    private String preferredLanguage;
    private List<String> favoriteCategories;
    private List<String> savedNewsIds;
    private boolean notificationsEnabled;
    private boolean darkModeEnabled;
    private Date createdAt;
    private Date lastLoginAt;
    private String fcmToken;

    // Default constructor
    public User() {}

    // Constructor with essential fields
    public User(String uid, String email, String displayName) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.preferredLanguage = "en";
        this.notificationsEnabled = true;
        this.darkModeEnabled = false;
        this.createdAt = new Date();
        this.lastLoginAt = new Date();
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public List<String> getFavoriteCategories() {
        return favoriteCategories;
    }

    public void setFavoriteCategories(List<String> favoriteCategories) {
        this.favoriteCategories = favoriteCategories;
    }

    public List<String> getSavedNewsIds() {
        return savedNewsIds;
    }

    public void setSavedNewsIds(List<String> savedNewsIds) {
        this.savedNewsIds = savedNewsIds;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                ", notificationsEnabled=" + notificationsEnabled +
                ", darkModeEnabled=" + darkModeEnabled +
                '}';
    }
}
