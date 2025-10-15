package com.way2.news.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.way2.news.R;
import com.way2.news.models.News;
import com.way2.news.utils.PreferenceManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsDetailsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView newsImage;
    private TextView newsTitle;
    private TextView newsContent;
    private TextView newsAuthor;
    private TextView newsTime;
    private TextView newsCategory;
    private ProgressBar progressBar;
    
    private News news;
    private PreferenceManager preferenceManager;
    private boolean isBookmarked = false;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);
        
        initializeViews();
        setupToolbar();
        getNewsData();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        newsImage = findViewById(R.id.iv_news_image);
        newsTitle = findViewById(R.id.tv_news_title);
        newsContent = findViewById(R.id.tv_news_content);
        newsAuthor = findViewById(R.id.tv_news_author);
        newsTime = findViewById(R.id.tv_news_time);
        newsCategory = findViewById(R.id.tv_news_category);
        progressBar = findViewById(R.id.progress_bar);
        
        preferenceManager = new PreferenceManager(this);
        firestore = FirebaseFirestore.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("News Details");
        }
    }

    private void getNewsData() {
        Intent intent = getIntent();
        if (intent != null) {
            String newsId = intent.getStringExtra("news_id");
            String title = intent.getStringExtra("news_title");
            String content = intent.getStringExtra("news_content");
            String imageUrl = intent.getStringExtra("news_image");
            String category = intent.getStringExtra("news_category");
            String sourceUrl = intent.getStringExtra("news_source");
            
            // Create news object from intent data
            news = new News();
            news.setId(newsId);
            news.setTitle(title);
            news.setContent(content);
            news.setImageUrl(imageUrl);
            news.setCategory(category);
            news.setSourceUrl(sourceUrl);
            news.setTimestamp(new Date());
            
            displayNews();
        }
    }

    private void displayNews() {
        if (news != null) {
            // Set title
            newsTitle.setText(news.getTitle());
            
            // Set content
            if (news.getContent() != null && !news.getContent().isEmpty()) {
                newsContent.setText(news.getContent());
            } else {
                newsContent.setText(news.getSummary());
            }
            
            // Set category
            newsCategory.setText(news.getCategory());
            
            // Set author
            if (news.getAuthor() != null && !news.getAuthor().isEmpty()) {
                newsAuthor.setText("By " + news.getAuthor());
                newsAuthor.setVisibility(View.VISIBLE);
            } else {
                newsAuthor.setVisibility(View.GONE);
            }
            
            // Set time
            if (news.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                newsTime.setText(sdf.format(news.getTimestamp()));
            }
            
            // Load image
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                Glide.with(this)
                    .load(news.getImageUrl())
                    .placeholder(R.drawable.ic_news_placeholder)
                    .error(R.drawable.ic_news_placeholder)
                    .into(newsImage);
            }
            
            // Check bookmark status
            checkBookmarkStatus();
        }
        
        progressBar.setVisibility(View.GONE);
    }

    private void checkBookmarkStatus() {
        String uid = preferenceManager.getUserId();
        if (uid == null || news == null) {
            isBookmarked = false;
            return;
        }
        firestore.collection("users").document(uid)
            .collection("savedNews").document(news.getId())
            .get()
            .addOnSuccessListener(this::onBookmarkDoc)
            .addOnFailureListener(e -> {
                isBookmarked = false;
                invalidateOptionsMenu();
            });
    }

    private void onBookmarkDoc(DocumentSnapshot doc) {
        isBookmarked = (doc != null && doc.exists());
        invalidateOptionsMenu();
    }

    private void setupClickListeners() {
        // Add any additional click listeners here
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem bookmarkItem = menu.findItem(R.id.action_bookmark);
        if (bookmarkItem != null) {
            bookmarkItem.setIcon(isBookmarked ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_bookmark) {
            toggleBookmark();
            return true;
        } else if (id == R.id.action_share) {
            shareNews();
            return true;
        } else if (id == R.id.action_open_source) {
            openSourceUrl();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void toggleBookmark() {
        isBookmarked = !isBookmarked;
        String uid = preferenceManager.getUserId();
        if (uid == null || uid.isEmpty() || news == null) {
            Toast.makeText(this, "Please login to save news", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBookmarked) {
            java.util.Map<String, Object> saved = new java.util.HashMap<>();
            saved.put("newsId", news.getId());
            saved.put("title", news.getTitle());
            saved.put("summary", news.getSummary() != null ? news.getSummary() : news.getContent());
            saved.put("imageUrl", news.getImageUrl());
            saved.put("category", news.getCategory());
            saved.put("language", news.getLanguage());
            saved.put("sourceUrl", news.getSourceUrl());
            saved.put("author", news.getAuthor());
            saved.put("timestamp", news.getTimestamp());
            saved.put("savedAt", FieldValue.serverTimestamp());

            firestore.collection("users").document(uid)
                .collection("savedNews").document(news.getId())
                .set(saved)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "News bookmarked", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show());
        } else {
            firestore.collection("users").document(uid)
                .collection("savedNews").document(news.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Remove failed", Toast.LENGTH_SHORT).show());
        }

        // Refresh menu to update bookmark icon
        invalidateOptionsMenu();
    }

    private void shareNews() {
        if (news != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, news.getTitle() + "\n\n" + 
                (news.getContent() != null ? news.getContent() : news.getSummary()) + 
                "\n\nRead more: " + news.getSourceUrl());
            
            startActivity(Intent.createChooser(shareIntent, "Share news via"));
        }
    }

    private void openSourceUrl() {
        if (news != null && news.getSourceUrl() != null && !news.getSourceUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getSourceUrl()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Source URL not available", Toast.LENGTH_SHORT).show();
        }
    }
}
