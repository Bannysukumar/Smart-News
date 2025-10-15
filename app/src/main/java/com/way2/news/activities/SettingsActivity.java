package com.way2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.way2.news.R;
import com.way2.news.utils.AuthUtils;
import com.way2.news.utils.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private Switch switchDarkMode;
    private Switch switchNotifications;
    private TextView tvLanguage;
    private View tvAbout;
    private View tvPrivacy;
    private View tvTerms;
    private View tvLogout;
    private View layoutChangePassword;
    
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initializeViews();
        setupClickListeners();
        loadCurrentSettings();
    }

    private void initializeViews() {
        // Note: No toolbar in the current layout
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchNotifications = findViewById(R.id.switch_notifications);
        // switchOfflineMode doesn't exist in layout - removing
        tvLanguage = findViewById(R.id.tv_selected_language);
        // These are LinearLayout containers, not TextViews
        tvAbout = findViewById(R.id.layout_privacy_policy); // Using privacy layout as about for now
        tvPrivacy = findViewById(R.id.layout_privacy_policy);
        tvTerms = findViewById(R.id.layout_terms_of_service);
        tvLogout = findViewById(R.id.layout_logout);
        layoutChangePassword = findViewById(R.id.layout_change_password);
        
        preferenceManager = new PreferenceManager(this);
    }

    // Removed setupToolbar method since there's no toolbar in the layout

    private void setupClickListeners() {
        // Dark mode switch
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                preferenceManager.setDarkModeEnabled(isChecked);
                applyDarkMode(isChecked);
            });
        }

        // Notifications switch
        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                preferenceManager.setNotificationsEnabled(isChecked);
            });
        }

        // Language selection - using the layout container
        View languageLayout = findViewById(R.id.layout_language_selection);
        if (languageLayout != null) {
            languageLayout.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, LanguageSelectionActivity.class);
                startActivity(intent);
            });
        }

        // Clear Cache functionality
        View clearCacheLayout = findViewById(R.id.layout_clear_cache);
        if (clearCacheLayout != null) {
            clearCacheLayout.setOnClickListener(v -> {
                showClearCacheDialog();
            });
        }

        // About (using privacy layout for now)
        if (tvAbout != null) {
            tvAbout.setOnClickListener(v -> {
                showAboutDialog();
            });
        }

        // Privacy Policy
        if (tvPrivacy != null) {
            tvPrivacy.setOnClickListener(v -> {
                openWebPage("https://yourwebsite.com/privacy");
            });
        }

        // Terms of Service
        if (tvTerms != null) {
            tvTerms.setOnClickListener(v -> {
                openWebPage("https://yourwebsite.com/terms");
            });
        }

        // Logout
        if (tvLogout != null) {
            tvLogout.setOnClickListener(v -> {
                logout();
            });
        }

        // Change Password
        if (layoutChangePassword != null) {
            layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }
    }

    private void showChangePasswordDialog() {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View view = inflater.inflate(R.layout.dialog_change_password, null);
        final android.widget.EditText etCurrent = view.findViewById(R.id.et_current_password);
        final android.widget.EditText etNew = view.findViewById(R.id.et_new_password);
        final android.widget.EditText etConfirm = view.findViewById(R.id.et_confirm_password);

        new android.app.AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(view)
            .setPositiveButton("Update", (d, w) -> {
                String current = etCurrent.getText().toString();
                String np = etNew.getText().toString();
                String cp = etConfirm.getText().toString();
                if (np.length() < 6) {
                    android.widget.Toast.makeText(this, "New password must be at least 6 chars", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!np.equals(cp)) {
                    android.widget.Toast.makeText(this, "Passwords do not match", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                reauthenticateAndChangePassword(current, np);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void reauthenticateAndChangePassword(String currentPassword, String newPassword) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            android.widget.Toast.makeText(this, "Not logged in", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential).addOnSuccessListener(v -> {
            user.updatePassword(newPassword).addOnSuccessListener(v2 -> {
                android.widget.Toast.makeText(this, "Password updated", android.widget.Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Update failed", android.widget.Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            android.widget.Toast.makeText(this, "Current password incorrect", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCurrentSettings() {
        // Load current settings from preferences
        if (switchDarkMode != null) {
            switchDarkMode.setChecked(preferenceManager.isDarkModeEnabled());
        }
        if (switchNotifications != null) {
            switchNotifications.setChecked(preferenceManager.areNotificationsEnabled());
        }
        
        // Set current language
        if (tvLanguage != null) {
            String currentLanguage = preferenceManager.getSelectedLanguage();
            String languageName = getLanguageName(currentLanguage);
            tvLanguage.setText(languageName);
        }
    }

    private String getLanguageName(String languageCode) {
        switch (languageCode) {
            case "en": return "English";
            case "hi": return "हिन्दी";
            case "te": return "తెలుగు";
            case "ta": return "தமிழ்";
            case "bn": return "বাংলা";
            case "gu": return "ગુજરાતી";
            case "mr": return "मराठी";
            case "kn": return "ಕನ್ನಡ";
            case "ml": return "മലയാളം";
            case "pa": return "ਪੰਜਾਬੀ";
            default: return "English";
        }
    }

    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void showAboutDialog() {
        // Create and show about dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("About Smart News")
               .setMessage("Smart News v1.0\n\nA modern news app with multi-language support.\n\nDeveloped with ❤️")
               .setPositiveButton("OK", null)
               .show();
    }

    private void showClearCacheDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Clear Cache")
               .setMessage("This will clear all cached data including images and temporary files. This action cannot be undone.")
               .setPositiveButton("Clear", (dialog, which) -> {
                   clearAppCache();
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void clearAppCache() {
        try {
            // Clear internal cache directory
            java.io.File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                deleteDir(cacheDir);
            }
            
            // Clear external files cache
            java.io.File externalCacheDir = getExternalCacheDir();
            if (externalCacheDir != null && externalCacheDir.exists()) {
                deleteDir(externalCacheDir);
            }
            
            // Clear application data cache
            try {
                java.io.File filesDir = getFilesDir();
                if (filesDir != null && filesDir.exists()) {
                    java.io.File[] files = filesDir.listFiles();
                    if (files != null) {
                        for (java.io.File file : files) {
                            if (file.getName().contains("cache") || file.getName().contains("temp")) {
                                deleteDir(file);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore file system errors
            }
            
            // Show success message
            android.widget.Toast.makeText(this, "Cache cleared successfully", android.widget.Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Failed to clear cache", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new java.io.File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir != null && dir.delete();
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
        startActivity(intent);
    }

    private void logout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Logout")
               .setMessage("Are you sure you want to logout?")
               .setPositiveButton("Yes", (dialog, which) -> {
                   // Sign out from Firebase
                   AuthUtils.signOut();
                   
                   // Clear user data from preferences
                   AuthUtils.clearUserDataFromPreferences(this);
                   
                   // Navigate to login activity
                   Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(intent);
                   finish();
               })
               .setNegativeButton("No", null)
               .show();
    }

    // Removed onOptionsItemSelected since there's no toolbar
}
