package com.way2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.way2.news.MainActivity;
import com.way2.news.R;
import com.way2.news.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 3000; // 3 seconds
    
    private ImageView logoImageView;
    private TextView appNameTextView;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        initializeViews();
        setupAnimations();
        checkFirstLaunch();
    }

    private void initializeViews() {
        logoImageView = findViewById(R.id.iv_logo);
        appNameTextView = findViewById(R.id.tv_app_name);
        preferenceManager = new PreferenceManager(this);
    }

    private void setupAnimations() {
        // Logo animation
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        logoImageView.startAnimation(logoAnimation);
        
        // App name animation
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        textAnimation.setStartOffset(500); // Start after logo animation
        appNameTextView.startAnimation(textAnimation);
    }

    private void checkFirstLaunch() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (preferenceManager.isFirstLaunch()) {
                // First time launch - show language selection
                startActivity(new Intent(SplashActivity.this, LanguageSelectionActivity.class));
            } else {
                // Check if user is logged in
                if (preferenceManager.getUserId() != null) {
                    // Check if user is admin
                    String loginMethod = preferenceManager.getLoginMethod();
                    if ("admin".equals(loginMethod)) {
                        // Admin user - go to admin dashboard
                        startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
                    } else {
                        // Regular user - go to main activity
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                } else {
                    // User not logged in - go to login
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            }
            finish();
        }, SPLASH_DELAY);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash
        // Do nothing
    }
}
