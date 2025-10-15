package com.way2.news;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.way2.news.activities.SettingsActivity;
import com.way2.news.fragments.CategoryFragment;
import com.way2.news.fragments.HomeFragment;
import com.way2.news.fragments.ProfileFragment;
import com.way2.news.fragments.SavedFragment;
import com.way2.news.utils.PreferenceManager;
import com.way2.news.utils.DoubleBackPressHandler;

public class MainActivity extends AppCompatActivity implements CategoryFragment.OnCategorySelectedListener {
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private CategoryFragment categoryFragment;
    private SavedFragment savedFragment;
    private ProfileFragment profileFragment;
    private PreferenceManager preferenceManager;
    private DoubleBackPressHandler doubleBackPressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupToolbar();
        setupBottomNavigation();
        setupFragments();
        loadDefaultFragment();
        
        // Initialize double back press handler
        doubleBackPressHandler = new DoubleBackPressHandler(this);

        // Request notification permission on Android 13+
        requestPostNotificationsPermissionIfNeeded();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (doubleBackPressHandler != null) {
            doubleBackPressHandler.cleanup();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        preferenceManager = new PreferenceManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Smart News");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                showFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_categories) {
                showFragment(categoryFragment);
                return true;
            } else if (itemId == R.id.nav_add) {
                // Launch AddNewsActivity, do not change selected tab
                startActivity(new Intent(this, com.way2.news.activities.AddNewsActivity.class));
                return false; // keep current selection
            } else if (itemId == R.id.nav_saved) {
                showFragment(savedFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                showFragment(profileFragment);
                return true;
            }
            
            return false;
        });
    }

    private void setupFragments() {
        homeFragment = new HomeFragment();
        categoryFragment = new CategoryFragment();
        categoryFragment.setOnCategorySelectedListener(this);
        savedFragment = new SavedFragment();
        profileFragment = new ProfileFragment();
    }

    private void loadDefaultFragment() {
        showFragment(homeFragment);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        
        // Reset double back press handler when navigating to a new fragment
        if (doubleBackPressHandler != null) {
            doubleBackPressHandler.reset();
        }
    }

    @Override
    public void onCategorySelected(String category) {
        // Switch to home fragment and load news for selected category
        showFragment(homeFragment);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        
        if (homeFragment != null) {
            homeFragment.loadNewsByCategory(category);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_search) {
            // Implement search functionality
            showSearchDialog();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_breaking_news) {
            if (homeFragment != null) {
                homeFragment.loadBreakingNews();
            }
            return true;
        } else if (id == R.id.action_notifications) {
            startActivity(new Intent(this, com.way2.news.activities.NotificationsActivity.class));
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Check if we're currently showing the home fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        
        // Check if current fragment is HomeFragment by class type (not instance)
        boolean isOnHomeFragment = currentFragment instanceof HomeFragment;
        
        if (!isOnHomeFragment) {
            // If not on home fragment, go to home
            showFragment(homeFragment);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else {
            // Handle double back press to exit when on home fragment
            if (doubleBackPressHandler != null) {
                doubleBackPressHandler.handleBackPress();
            } else {
                // Fallback if handler is not initialized
                super.onBackPressed();
            }
        }
    }

    private void showSearchDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Search News");
        
        // Create search input field
        final android.widget.EditText searchInput = new android.widget.EditText(this);
        searchInput.setHint("Enter keywords to search...");
        searchInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        
        builder.setView(searchInput);
        builder.setMessage("Search for news articles by keywords");
        
        builder.setPositiveButton("Search", (dialog, which) -> {
            String searchQuery = searchInput.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
            } else {
                android.widget.Toast.makeText(this, "Please enter a search term", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void performSearch(String searchQuery) {
        // Show loading dialog
        android.app.AlertDialog loadingDialog = new android.app.AlertDialog.Builder(this)
                .setMessage("Searching for: " + searchQuery)
                .setCancelable(false)
                .create();
        loadingDialog.show();
        
        // Simulate search process
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            loadingDialog.dismiss();
            
            // For demo purposes, show search results dialog
            // In a real app, you would search your news database/API
            showSearchResults(searchQuery);
            
        }, 2000); // 2 second delay to simulate search
    }
    
    private void showSearchResults(String searchQuery) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Search Results");
        
        // Create a simple list of mock search results
        String[] mockResults = {
            "Breaking: " + searchQuery + " makes headlines",
            "Latest updates on " + searchQuery,
            "Expert analysis: " + searchQuery + " impact",
            "Local news: " + searchQuery + " in your area",
            "International: " + searchQuery + " global perspective"
        };
        
        builder.setItems(mockResults, (dialog, which) -> {
            // Simulate opening a news article
            android.widget.Toast.makeText(this, "Opening article: " + mockResults[which], android.widget.Toast.LENGTH_SHORT).show();
            
            // In a real app, you would navigate to NewsDetailsActivity
            // Intent intent = new Intent(this, NewsDetailsActivity.class);
            // intent.putExtra("news_id", "search_result_" + which);
            // startActivity(intent);
        });
        
        builder.setPositiveButton("New Search", (dialog, which) -> {
            showSearchDialog();
        });
        
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void requestPostNotificationsPermissionIfNeeded() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    androidx.core.app.ActivityCompat.requestPermissions(
                            this,
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            1001
                    );
                }
            }
        } catch (Throwable ignored) {}
    }
    
}