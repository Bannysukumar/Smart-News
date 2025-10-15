package com.way2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.way2.news.R;
import com.way2.news.adapters.AdminNewsAdapter;
import com.way2.news.models.News;
import com.way2.news.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private TextView tvWelcome, tvStats;
    private Button btnAddNews, btnManageCategories, btnViewUsers, btnAnalytics;
    private RecyclerView recyclerViewRecentNews;
    private AdminNewsAdapter newsAdapter;
    private List<News> recentNews;
    
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        
        // Guard: only allow access if login method is admin
        PreferenceManager pmGuard = new PreferenceManager(this);
        String methodGuard = pmGuard.getLoginMethod();
        if (!"admin".equals(methodGuard)) {
            // Not an admin, redirect away
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupClickListeners();
        initializeFirebase();
        loadDashboardData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_admin);
        tvWelcome = findViewById(R.id.tv_admin_welcome);
        tvStats = findViewById(R.id.tv_admin_stats);
        btnAddNews = findViewById(R.id.btn_add_news);
        btnManageCategories = findViewById(R.id.btn_manage_categories);
        btnViewUsers = findViewById(R.id.btn_view_users);
        btnAnalytics = findViewById(R.id.btn_analytics);
        recyclerViewRecentNews = findViewById(R.id.recycler_view_recent_news);
        
        preferenceManager = new PreferenceManager(this);
        recentNews = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void setupClickListeners() {
        btnAddNews.setOnClickListener(this);
        btnManageCategories.setOnClickListener(this);
        btnViewUsers.setOnClickListener(this);
        btnAnalytics.setOnClickListener(this);
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
    }

    private void loadDashboardData() {
        String userEmail = preferenceManager.getUserEmail();
        if (userEmail != null) {
            tvWelcome.setText("Welcome, " + userEmail);
        }
        
        loadRecentNews();
        loadStats();
    }

    private void loadRecentNews() {
        firestore.collection("news")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    recentNews.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        News article = document.toObject(News.class);
                        article.setId(document.getId());
                        recentNews.add(article);
                    }
                    setupRecyclerView();
                } else {
                    Toast.makeText(this, "Failed to load recent news", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupRecyclerView() {
        newsAdapter = new AdminNewsAdapter(recentNews, this);
        recyclerViewRecentNews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecentNews.setAdapter(newsAdapter);
    }

    private void loadStats() {
        // Load total news count
        firestore.collection("news")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int newsCount = task.getResult().size();
                    
                    // Load total users count
                    firestore.collection("users")
                        .get()
                        .addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                int userCount = userTask.getResult().size();
                                updateStats(newsCount, userCount);
                            } else {
                                updateStats(newsCount, 0);
                            }
                        });
                } else {
                    updateStats(0, 0);
                }
            });
    }

    private void updateStats(int newsCount, int userCount) {
        String statsText = "📊 Dashboard Stats\n" +
                          "📰 Total News: " + newsCount + "\n" +
                          "👥 Total Users: " + userCount + "\n" +
                          "🔥 Breaking News: " + getBreakingNewsCount() + "\n" +
                          "📈 Trending: " + getTrendingNewsCount();
        tvStats.setText(statsText);
    }

    private int getBreakingNewsCount() {
        int count = 0;
        for (News article : recentNews) {
            if (article.isBreaking()) {
                count++;
            }
        }
        return count;
    }

    private int getTrendingNewsCount() {
        int count = 0;
        for (News article : recentNews) {
            // Note: News model doesn't have isTrending field, so we'll skip this for now
            // You can add this field to the News model if needed
            count++;
        }
        return count;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btn_add_news) {
            navigateToAddNews();
        } else if (id == R.id.btn_manage_categories) {
            showManageCategories();
        } else if (id == R.id.btn_view_users) {
            showViewUsers();
        } else if (id == R.id.btn_analytics) {
            showAnalytics();
        }
    }

    private void navigateToAddNews() {
        Intent intent = new Intent(this, AddNewsActivity.class);
        startActivity(intent);
    }

    private void showManageCategories() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Manage Categories")
               .setMessage("Category management feature will be implemented soon.")
               .setPositiveButton("OK", null)
               .show();
    }

    private void showViewUsers() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("View Users")
               .setMessage("User management feature will be implemented soon.")
               .setPositiveButton("OK", null)
               .show();
    }

    private void showAnalytics() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Analytics")
               .setMessage("Analytics dashboard will be implemented soon.")
               .setPositiveButton("OK", null)
               .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_refresh) {
            loadDashboardData();
            return true;
        } else if (id == R.id.action_pending_approvals) {
            Intent intent = new Intent(this, PendingSubmissionsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            performLogout();
            return true;
        } else if (id == R.id.action_settings) {
            showAdminSettings();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Logout")
               .setMessage("Are you sure you want to logout from admin panel?")
               .setPositiveButton("Logout", (dialog, which) -> {
                   FirebaseAuth.getInstance().signOut();
                   preferenceManager.clearUserData();
                   
                   Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                   
                   Intent intent = new Intent(this, AdminLoginActivity.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(intent);
                   finish();
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void showAdminSettings() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Admin Settings")
               .setMessage("Admin settings will be implemented soon.")
               .setPositiveButton("OK", null)
               .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadDashboardData();
    }
}
