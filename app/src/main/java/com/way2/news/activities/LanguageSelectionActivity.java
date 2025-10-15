package com.way2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.way2.news.R;
import com.way2.news.adapters.LanguageAdapter;
import com.way2.news.models.Language;
import com.way2.news.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionActivity extends AppCompatActivity implements LanguageAdapter.OnLanguageClickListener {
    private RecyclerView recyclerViewLanguages;
    private Button btnContinue;
    private TextView tvTitle;
    private LanguageAdapter languageAdapter;
    private List<Language> languageList;
    private PreferenceManager preferenceManager;
    private String selectedLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);
        
        initializeViews();
        setupLanguages();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerViewLanguages = findViewById(R.id.recycler_view_languages);
        btnContinue = findViewById(R.id.btn_continue);
        tvTitle = findViewById(R.id.tv_title);
        preferenceManager = new PreferenceManager(this);
    }

    private void setupLanguages() {
        languageList = new ArrayList<>();
        languageList.add(new Language("en", "English", "English", R.drawable.ic_flag_uk));
        languageList.add(new Language("hi", "हिन्दी", "Hindi", R.drawable.ic_flag_india));
        languageList.add(new Language("te", "తెలుగు", "Telugu", R.drawable.ic_flag_india));
        languageList.add(new Language("ta", "தமிழ்", "Tamil", R.drawable.ic_flag_india));
        languageList.add(new Language("bn", "বাংলা", "Bengali", R.drawable.ic_flag_india));
        languageList.add(new Language("gu", "ગુજરાતી", "Gujarati", R.drawable.ic_flag_india));
        languageList.add(new Language("mr", "मराठी", "Marathi", R.drawable.ic_flag_india));
        languageList.add(new Language("kn", "ಕನ್ನಡ", "Kannada", R.drawable.ic_flag_india));
        languageList.add(new Language("ml", "മലയാളം", "Malayalam", R.drawable.ic_flag_india));
        languageList.add(new Language("pa", "ਪੰਜਾਬੀ", "Punjabi", R.drawable.ic_flag_india));
    }

    private void setupRecyclerView() {
        languageAdapter = new LanguageAdapter(this, languageList);
        languageAdapter.setOnLanguageClickListener(this);
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewLanguages.setLayoutManager(layoutManager);
        recyclerViewLanguages.setAdapter(languageAdapter);
    }

    private void setupClickListeners() {
        btnContinue.setOnClickListener(v -> {
            preferenceManager.setSelectedLanguage(selectedLanguage);
            preferenceManager.setFirstLaunch(false);
            
            Intent intent = new Intent(LanguageSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onLanguageClick(Language language) {
        selectedLanguage = language.getCode();
        languageAdapter.setSelectedLanguage(selectedLanguage);
    }
}
