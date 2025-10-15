package com.way2.news.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.way2.news.R;

import java.util.HashMap;
import java.util.Map;

public class UserEditProfileActivity extends AppCompatActivity {
    private EditText etName;
    private EditText etUsername; // read-only
    private EditText etEmail;
    private EditText etPhone;
    private View btnSave;
    private ProgressBar progress;
    private Toolbar toolbar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_profile);
        etName = findViewById(R.id.et_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnSave = findViewById(R.id.btn_save);
        progress = findViewById(R.id.progress_bar);
        toolbar = findViewById(R.id.toolbar_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadCurrentValues();

        etUsername.setEnabled(false);
        etUsername.setFocusable(false);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> save());
    }

    private void loadCurrentValues() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null) etName.setText(user.getDisplayName());
            if (user.getEmail() != null) etEmail.setText(user.getEmail());
        }
        String uid = auth.getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String username = doc.getString("username");
                    String phone = doc.getString("phone");
                    if (!TextUtils.isEmpty(username)) etUsername.setText(username);
                    if (!TextUtils.isEmpty(phone)) etPhone.setText(phone);
                }
            });
        }
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }

        setSaving(true);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            setSaving(false);
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update display name first
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                setSaving(false);
                Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update email next (optional, only if changed)
            if (!email.equals(user.getEmail())) {
                user.updateEmail(email).addOnCompleteListener(t2 -> {
                    if (!t2.isSuccessful()) {
                        setSaving(false);
                        Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show();
                    } else {
                        updateFirestorePhoneAndFinish(user.getUid(), phone);
                    }
                });
            } else {
                updateFirestorePhoneAndFinish(user.getUid(), phone);
            }
        });
    }

    private void updateFirestorePhoneAndFinish(String uid, String phone) {
        Map<String, Object> map = new HashMap<>();
        map.put("phone", phone);
        db.collection("users").document(uid).set(map, com.google.firebase.firestore.SetOptions.merge())
            .addOnCompleteListener(done -> {
                setSaving(false);
                if (done.isSuccessful()) {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setSaving(boolean saving) {
        if (progress != null) progress.setVisibility(saving ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!saving);
    }
}


