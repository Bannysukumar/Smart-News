package com.way2.news.models;

public class Language {
    private String code;
    private String name;
    private String englishName;
    private int flagResource;

    public Language() {}

    public Language(String code, String name, String englishName, int flagResource) {
        this.code = code;
        this.name = name;
        this.englishName = englishName;
        this.flagResource = flagResource;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public int getFlagResource() {
        return flagResource;
    }

    public void setFlagResource(int flagResource) {
        this.flagResource = flagResource;
    }

    @Override
    public String toString() {
        return "Language{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", englishName='" + englishName + '\'' +
                '}';
    }
}
