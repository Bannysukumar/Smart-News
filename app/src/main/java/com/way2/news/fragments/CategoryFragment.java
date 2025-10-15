package com.way2.news.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.way2.news.R;
import com.way2.news.adapters.CategoryAdapter;
import com.way2.news.models.Category;
import com.way2.news.utils.PreferenceManager;
import com.way2.news.viewmodels.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {
    private RecyclerView recyclerViewCategories;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    
    private CategoryAdapter categoryAdapter;
    private CategoryViewModel categoryViewModel;
    private PreferenceManager preferenceManager;
    private List<Category> categoryList;
    private OnCategorySelectedListener onCategorySelectedListener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(String category);
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.onCategorySelectedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerViewCategories = view.findViewById(R.id.recycler_view_categories);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        
        preferenceManager = new PreferenceManager(requireContext());
        categoryList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(requireContext(), categoryList);
        categoryAdapter.setOnCategoryClickListener(this);
        
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerViewCategories.setLayoutManager(layoutManager);
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void setupViewModel() {
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        
        // Observe category list
        categoryViewModel.getCategoryList().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryList.clear();
                categoryList.addAll(categories);
                categoryAdapter.updateCategoryList(categoryList);
                layoutEmptyState.setVisibility(View.GONE);
                recyclerViewCategories.setVisibility(View.VISIBLE);
            } else {
                layoutEmptyState.setVisibility(View.VISIBLE);
                recyclerViewCategories.setVisibility(View.GONE);
            }
        });
        
        // Observe loading state
        categoryViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        
        // Observe error messages
        categoryViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Load categories
        categoryViewModel.loadCategories();
    }

    private void setupClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            categoryViewModel.refreshCategories();
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        if (onCategorySelectedListener != null) {
            onCategorySelectedListener.onCategorySelected(category.getName());
        }
    }

    @Override
    public void onFavoriteToggle(Category category, boolean isFavorite) {
        if (isFavorite) {
            categoryViewModel.removeFromFavorites(category.getId());
            Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            categoryViewModel.addToFavorites(category.getId());
            Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
        }
    }
}
