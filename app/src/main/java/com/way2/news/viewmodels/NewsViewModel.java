package com.way2.news.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.way2.news.models.News;
import com.way2.news.utils.NetworkUtils;
import com.way2.news.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class NewsViewModel extends AndroidViewModel {
    private MutableLiveData<List<News>> newsList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<News> selectedNews = new MutableLiveData<>();
    
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;
    private String currentCategory = "all";
    private String currentLanguage = "en";

    public NewsViewModel(@NonNull Application application) {
        super(application);
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(application);
        currentLanguage = preferenceManager.getSelectedLanguage();
    }

    public LiveData<List<News>> getNewsList() {
        return newsList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<News> getSelectedNews() {
        return selectedNews;
    }

    public void loadNews() {
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            errorMessage.setValue("No internet connection");
            return;
        }

        isLoading.setValue(true);
        
        // Avoid composite index requirements: fetch latest, then filter client-side
        firestore.collection("news")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<News> news = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    News newsItem = document.toObject(News.class);
                    newsItem.setId(document.getId());
                    if (newsItem.getLanguage() != null && newsItem.getLanguage().equals(currentLanguage)) {
                        if (currentCategory.equals("all") || (newsItem.getCategory() != null && newsItem.getCategory().equals(currentCategory))) {
                            news.add(newsItem);
                        }
                    }
                }

                if (news.isEmpty()) {
                    // Fallback: show latest news regardless of language so admins/users can see fresh posts
                    List<News> fallbackNews = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        News newsItem = document.toObject(News.class);
                        newsItem.setId(document.getId());
                        fallbackNews.add(newsItem);
                        if (fallbackNews.size() >= 20) break;
                    }
                    newsList.setValue(fallbackNews);
                } else {
                newsList.setValue(news);
                }
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Failed to load news: " + e.getMessage());
                isLoading.setValue(false);
            });
    }

    public void loadNewsByCategory(String category) {
        currentCategory = category;
        loadNews();
    }

    public void loadBreakingNews() {
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            errorMessage.setValue("No internet connection");
            return;
        }

        isLoading.setValue(true);
        
        // Fetch and filter client-side to avoid composite index
        firestore.collection("news")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<News> breakingNews = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    News newsItem = document.toObject(News.class);
                    newsItem.setId(document.getId());
                    if (newsItem.getLanguage() != null && newsItem.getLanguage().equals(currentLanguage) && newsItem.isBreaking()) {
                        breakingNews.add(newsItem);
                    }
                }
                newsList.setValue(breakingNews);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Failed to load breaking news: " + e.getMessage());
                isLoading.setValue(false);
            });
    }

    public void searchNews(String query) {
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            errorMessage.setValue("No internet connection");
            return;
        }

        isLoading.setValue(true);
        
        firestore.collection("news")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<News> searchResults = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    News newsItem = document.toObject(News.class);
                    newsItem.setId(document.getId());
                    if (newsItem.getLanguage() != null && newsItem.getLanguage().equals(currentLanguage)) {
                        // Simple text search in title and summary
                        if ((newsItem.getTitle() != null && newsItem.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                            (newsItem.getSummary() != null && newsItem.getSummary().toLowerCase().contains(query.toLowerCase()))) {
                            searchResults.add(newsItem);
                        }
                    }
                }
                newsList.setValue(searchResults);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Search failed: " + e.getMessage());
                isLoading.setValue(false);
            });
    }

    public void setSelectedNews(News news) {
        selectedNews.setValue(news);
    }

    public void refreshNews() {
        loadNews();
    }

    public void setLanguage(String language) {
        currentLanguage = language;
        preferenceManager.setSelectedLanguage(language);
        loadNews();
    }

    public String getCurrentCategory() {
        return currentCategory;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
