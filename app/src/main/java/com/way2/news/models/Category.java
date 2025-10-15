package com.way2.news.models;

import java.util.List;

public class Category {
    private String id;
    private String name;
    private String displayName;
    private String iconUrl;
    private String color;
    private boolean isEnabled;
    private int order;
    private List<String> supportedLanguages;

    // Default constructor
    public Category() {}

    // Constructor with essential fields
    public Category(String id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.isEnabled = true;
        this.order = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", isEnabled=" + isEnabled +
                ", order=" + order +
                '}';
    }
}
