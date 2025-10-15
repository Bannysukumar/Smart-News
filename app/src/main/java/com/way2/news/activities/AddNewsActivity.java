package com.way2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.way2.news.R;
import com.way2.news.models.News;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewsActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private EditText etTitle, etContent, etSummary, etImageUrl, etAuthor, etSource, etTags;
    private Spinner spinnerCategory, spinnerLanguage;
    private CheckBox cbBreaking, cbTrending;
    private Button btnSave, btnPreview;
    private ProgressBar progressBar;
    
    private FirebaseFirestore firestore;
    private boolean isEditMode = false;
    private String newsId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);
        
        initializeViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        initializeFirebase();
        
        // Check if editing existing news
        Intent intent = getIntent();
        if (intent.hasExtra("news_id")) {
            isEditMode = true;
            newsId = intent.getStringExtra("news_id");
            loadNewsData();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_add_news);
        etTitle = findViewById(R.id.et_news_title);
        etContent = findViewById(R.id.et_news_content);
        etSummary = findViewById(R.id.et_news_summary);
        etImageUrl = findViewById(R.id.et_news_image_url);
        etAuthor = findViewById(R.id.et_news_author);
        etSource = findViewById(R.id.et_news_source);
        etTags = findViewById(R.id.et_news_tags);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerLanguage = findViewById(R.id.spinner_language);
        cbBreaking = findViewById(R.id.cb_breaking_news);
        cbTrending = findViewById(R.id.cb_trending_news);
        btnSave = findViewById(R.id.btn_save_news);
        btnPreview = findViewById(R.id.btn_preview_news);
        progressBar = findViewById(R.id.progress_bar_add_news);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Edit News" : "Add News");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        // Category spinner
        List<String> categories = Arrays.asList(
            "Technology", "Sports", "Business", "Entertainment", 
            "Health", "Politics", "World", "Science"
        );
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Language spinner
        List<String> languages = Arrays.asList("en", "hi", "te", "ta", "bn", "gu", "kn", "ml", "mr", "pa", "ur");
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(this);
        btnPreview.setOnClickListener(this);
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
    }

    private void loadNewsData() {
        if (newsId != null) {
            firestore.collection("news")
                .document(newsId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        News article = task.getResult().toObject(News.class);
                        if (article != null) {
                            populateFields(article);
                        }
                    } else {
                        Toast.makeText(this, "Failed to load news data", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        }
    }

    private void populateFields(News article) {
        etTitle.setText(article.getTitle());
        etContent.setText(article.getContent());
        etSummary.setText(article.getSummary());
        etImageUrl.setText(article.getImageUrl());
        etAuthor.setText(article.getAuthor());
        etSource.setText(article.getSourceUrl());
        
        // Note: News model doesn't support tags
        
        // Set category
        String category = article.getCategory();
        if (category != null) {
            for (int i = 0; i < spinnerCategory.getCount(); i++) {
                if (spinnerCategory.getItemAtPosition(i).toString().equals(category)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
        
        // Set language
        String language = article.getLanguage();
        if (language != null) {
            for (int i = 0; i < spinnerLanguage.getCount(); i++) {
                if (spinnerLanguage.getItemAtPosition(i).toString().equals(language)) {
                    spinnerLanguage.setSelection(i);
                    break;
                }
            }
        }
        
        cbBreaking.setChecked(article.isBreaking());
        // Note: News model doesn't support trending
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btn_save_news) {
            saveNews();
        } else if (id == R.id.btn_preview_news) {
            previewNews();
        }
    }

    private void saveNews() {
        if (!validateFields()) {
            return;
        }

        showProgress(true);

        // Note: News model doesn't support tags, so we'll skip this for now

        // Create news article
        News article = new News();
        article.setTitle(etTitle.getText().toString().trim());
        article.setContent(etContent.getText().toString().trim());
        article.setSummary(etSummary.getText().toString().trim());
        article.setImageUrl(etImageUrl.getText().toString().trim());
        article.setCategory(spinnerCategory.getSelectedItem().toString());
        article.setLanguage(spinnerLanguage.getSelectedItem().toString());
        article.setBreaking(cbBreaking.isChecked());
        article.setAuthor(etAuthor.getText().toString().trim());
        article.setSourceUrl(etSource.getText().toString().trim());
        article.setTimestamp(new java.util.Date());
        article.setViewCount(0);
        article.setBookmarked(false);

        if (isEditMode && newsId != null) {
            // Update existing news
            firestore.collection("news")
                .document(newsId)
                .set(article)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "News updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update news", Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            // Submit for admin approval instead of direct publish
            com.way2.news.utils.PreferenceManager pm = new com.way2.news.utils.PreferenceManager(this);
            String uid = pm.getUserId();
            Map<String, Object> request = new HashMap<>();
            request.put("authorId", uid);
            request.put("status", "pending"); // pending | approved | rejected
            request.put("submittedAt", new java.util.Date());
            request.put("news", article);

            firestore.collection("newsSubmissions")
                .add(request)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Submitted for approval", Toast.LENGTH_SHORT).show();
                        clearFields();
                    } else {
                        Toast.makeText(this, "Submit failed", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(etTitle.getText())) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etContent.getText())) {
            etContent.setError("Content is required");
            etContent.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etSummary.getText())) {
            etSummary.setError("Summary is required");
            etSummary.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etAuthor.getText())) {
            etAuthor.setError("Author is required");
            etAuthor.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etSource.getText())) {
            etSource.setError("Source is required");
            etSource.requestFocus();
            return false;
        }

        return true;
    }

    private int calculateReadTime(String content) {
        // Simple calculation: ~200 words per minute
        int wordCount = content.split("\\s+").length;
        return Math.max(1, wordCount / 200);
    }

    private void previewNews() {
        if (!validateFields()) {
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("News Preview")
               .setMessage("Title: " + etTitle.getText().toString() + "\n\n" +
                          "Summary: " + etSummary.getText().toString() + "\n\n" +
                          "Category: " + spinnerCategory.getSelectedItem().toString() + "\n" +
                          "Language: " + spinnerLanguage.getSelectedItem().toString() + "\n" +
                          "Breaking: " + (cbBreaking.isChecked() ? "Yes" : "No") + "\n" +
                          "Trending: " + (cbTrending.isChecked() ? "Yes" : "No"))
               .setPositiveButton("OK", null)
               .show();
    }

    private void clearFields() {
        etTitle.setText("");
        etContent.setText("");
        etSummary.setText("");
        etImageUrl.setText("");
        etAuthor.setText("");
        etSource.setText("");
        etTags.setText("");
        spinnerCategory.setSelection(0);
        spinnerLanguage.setSelection(0);
        cbBreaking.setChecked(false);
        cbTrending.setChecked(false);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnPreview.setEnabled(!show);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_clear) {
            clearFields();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
