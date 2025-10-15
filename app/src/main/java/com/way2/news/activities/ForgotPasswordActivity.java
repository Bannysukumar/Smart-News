package com.way2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.way2.news.R;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail;
    private Button btnResetPassword, btnBackToLogin;
    private TextView tvInstructions;
    private ProgressBar progressBar;
    
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        initializeViews();
        setupClickListeners();
        initializeFirebase();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);
        tvInstructions = findViewById(R.id.tv_instructions);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(this);
        btnBackToLogin.setOnClickListener(this);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btn_reset_password) {
            performPasswordReset();
        } else if (id == R.id.btn_back_to_login) {
            navigateToLogin();
        }
    }

    private void performPasswordReset() {
        String email = etEmail.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        showProgress(true);
        
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    showProgress(false);
                    
                    if (task.isSuccessful()) {
                        showSuccessMessage();
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void showSuccessMessage() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Email Sent")
               .setMessage("We have sent you a password reset link to your email address. Please check your inbox and follow the instructions to reset your password.")
               .setPositiveButton("OK", (dialog, which) -> {
                   navigateToLogin();
               })
               .setCancelable(false)
               .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!show);
        btnBackToLogin.setEnabled(!show);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onBackPressed() {
        navigateToLogin();
    }
}
