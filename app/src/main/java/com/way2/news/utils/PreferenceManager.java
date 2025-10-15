package com.way2.news.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PreferenceManager {
    private static final String PREF_NAME = "SmartNewsPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_SELECTED_LANGUAGE = "selected_language";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_FAVORITE_CATEGORIES = "favorite_categories";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_DISPLAY_NAME = "user_display_name";
    private static final String KEY_LOGIN_METHOD = "login_method";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    private static final String KEY_OFFLINE_MODE = "offline_mode";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    // First Launch
    public boolean isFirstLaunch() {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean isFirstLaunch) {
        editor.putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch);
        editor.apply();
    }

    // Language
    public String getSelectedLanguage() {
        return preferences.getString(KEY_SELECTED_LANGUAGE, "en");
    }

    public void setSelectedLanguage(String language) {
        editor.putString(KEY_SELECTED_LANGUAGE, language);
        editor.apply();
    }

    // Dark Mode
    public boolean isDarkModeEnabled() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        editor.putBoolean(KEY_DARK_MODE, enabled);
        editor.apply();
    }

    // Notifications
    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    // Favorite Categories
    public Set<String> getFavoriteCategories() {
        return preferences.getStringSet(KEY_FAVORITE_CATEGORIES, new HashSet<>());
    }

    public void setFavoriteCategories(Set<String> categories) {
        editor.putStringSet(KEY_FAVORITE_CATEGORIES, categories);
        editor.apply();
    }

    public void addFavoriteCategory(String category) {
        Set<String> categories = getFavoriteCategories();
        categories.add(category);
        setFavoriteCategories(categories);
    }

    public void removeFavoriteCategory(String category) {
        Set<String> categories = getFavoriteCategories();
        categories.remove(category);
        setFavoriteCategories(categories);
    }

    // User Info
    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public void setUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, null);
    }

    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getUserDisplayName() {
        return preferences.getString(KEY_USER_DISPLAY_NAME, null);
    }

    public void setUserDisplayName(String displayName) {
        editor.putString(KEY_USER_DISPLAY_NAME, displayName);
        editor.apply();
    }

    public String getLoginMethod() {
        return preferences.getString(KEY_LOGIN_METHOD, null);
    }

    public void setLoginMethod(String loginMethod) {
        editor.putString(KEY_LOGIN_METHOD, loginMethod);
        editor.apply();
    }

    // Sync Time
    public long getLastSyncTime() {
        return preferences.getLong(KEY_LAST_SYNC_TIME, 0);
    }

    public void setLastSyncTime(long time) {
        editor.putLong(KEY_LAST_SYNC_TIME, time);
        editor.apply();
    }

    // Offline Mode
    public boolean isOfflineMode() {
        return preferences.getBoolean(KEY_OFFLINE_MODE, false);
    }

    public void setOfflineMode(boolean offline) {
        editor.putBoolean(KEY_OFFLINE_MODE, offline);
        editor.apply();
    }

    // Clear all preferences
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    // Clear user data only
    public void clearUserData() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_DISPLAY_NAME);
        editor.remove(KEY_LOGIN_METHOD);
        editor.apply();
    }
}
