package com.way2.news.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.way2.news.models.Category;
import com.way2.news.utils.NetworkUtils;
import com.way2.news.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CategoryViewModel extends AndroidViewModel {
    private MutableLiveData<List<Category>> categoryList = new MutableLiveData<>();
    private MutableLiveData<List<Category>> favoriteCategories = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(application);
    }

    public LiveData<List<Category>> getCategoryList() {
        return categoryList;
    }

    public LiveData<List<Category>> getFavoriteCategories() {
        return favoriteCategories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadCategories() {
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            errorMessage.setValue("No internet connection");
            return;
        }

        isLoading.setValue(true);
        
        firestore.collection("categories")
            .whereEqualTo("isEnabled", true)
            .orderBy("order")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Category> categories = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Category category = document.toObject(Category.class);
                    category.setId(document.getId());
                    categories.add(category);
                }
                categoryList.setValue(categories);
                loadFavoriteCategories(categories);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Failed to load categories: " + e.getMessage());
                isLoading.setValue(false);
            });
    }

    private void loadFavoriteCategories(List<Category> allCategories) {
        Set<String> favoriteIds = preferenceManager.getFavoriteCategories();
        List<Category> favorites = new ArrayList<>();
        
        for (Category category : allCategories) {
            if (favoriteIds.contains(category.getId())) {
                favorites.add(category);
            }
        }
        
        favoriteCategories.setValue(favorites);
    }

    public void addToFavorites(String categoryId) {
        Set<String> favorites = preferenceManager.getFavoriteCategories();
        favorites.add(categoryId);
        preferenceManager.setFavoriteCategories(favorites);
        
        // Refresh favorite categories
        List<Category> allCategories = categoryList.getValue();
        if (allCategories != null) {
            loadFavoriteCategories(allCategories);
        }
    }

    public void removeFromFavorites(String categoryId) {
        Set<String> favorites = preferenceManager.getFavoriteCategories();
        favorites.remove(categoryId);
        preferenceManager.setFavoriteCategories(favorites);
        
        // Refresh favorite categories
        List<Category> allCategories = categoryList.getValue();
        if (allCategories != null) {
            loadFavoriteCategories(allCategories);
        }
    }

    public boolean isFavorite(String categoryId) {
        Set<String> favorites = preferenceManager.getFavoriteCategories();
        return favorites.contains(categoryId);
    }

    public void refreshCategories() {
        loadCategories();
    }
}
