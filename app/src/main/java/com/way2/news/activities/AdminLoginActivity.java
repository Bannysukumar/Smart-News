package com.way2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.way2.news.R;
import com.way2.news.utils.PreferenceManager;

public class AdminLoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvBackToUser, tvForgotPassword;
    private com.google.android.material.textfield.TextInputLayout passwordInputLayout;
    private ProgressBar progressBar;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        
        initializeViews();
        setupClickListeners();
        initializeFirebase();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_admin_email);
        etPassword = findViewById(R.id.et_admin_password);
        btnLogin = findViewById(R.id.btn_admin_login);
        tvBackToUser = findViewById(R.id.tv_back_to_user);
        tvForgotPassword = findViewById(R.id.tv_admin_forgot_password);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        progressBar = findViewById(R.id.progress_bar_admin);
        
        preferenceManager = new PreferenceManager(this);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(this);
        tvBackToUser.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        // Set up password toggle listener
        passwordInputLayout.setEndIconOnClickListener(v -> togglePasswordVisibility());
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btn_admin_login) {
            performAdminLogin();
        } else if (id == R.id.tv_back_to_user) {
            navigateToUserLogin();
        } else if (id == R.id.tv_admin_forgot_password) {
            showForgotPasswordDialog();
        // Password toggle is handled by the TextInputLayout's end icon
        }
    }

    private void performAdminLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        showProgress(true);
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Check if user is admin
                            checkAdminStatus(user.getUid());
                        }
                    } else {
                        showProgress(false);
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Login failed";
                        Toast.makeText(AdminLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void checkAdminStatus(String userId) {
        firestore.collection("admins")
            .document(userId)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    showProgress(false);
                    
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Boolean adminFlag = document != null ? document.getBoolean("isAdmin") : null;
                        if (document != null && document.exists() && Boolean.TRUE.equals(adminFlag)) {
                            // User is admin, proceed to admin dashboard
                            preferenceManager.setUserId(userId);
                            preferenceManager.setUserEmail(etEmail.getText().toString().trim());
                            preferenceManager.setLoginMethod("admin");
                            
                            Toast.makeText(AdminLoginActivity.this, "Admin login successful!", Toast.LENGTH_SHORT).show();
                            navigateToAdminDashboard();
                        } else {
                            // User is not admin
                            firebaseAuth.signOut();
                            String message;
                            if (document != null && document.exists()) {
                                message = "User found but not admin. isAdmin: " + String.valueOf(adminFlag);
                            } else {
                                message = "Admin document not found for user: " + userId;
                            }
                            Toast.makeText(AdminLoginActivity.this, "Access denied. " + message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Error checking admin status
                        firebaseAuth.signOut();
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(AdminLoginActivity.this, "Failed to verify admin status: " + error, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToUserLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Admin Password");
        
        final EditText emailInput = new EditText(this);
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Enter admin email");
        emailInput.setText(etEmail.getText().toString());
        
        builder.setView(emailInput);
        builder.setMessage("Enter the admin email to receive password reset instructions");
        
        builder.setPositiveButton("Send Reset Email", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty() && isValidEmail(email)) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        showProgress(true);
        
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    showProgress(false);
                    
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminLoginActivity.this, 
                            "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(AdminLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(new PasswordTransformationMethod());
            passwordInputLayout.setEndIconDrawable(R.drawable.ic_visibility_off);
        } else {
            etPassword.setTransformationMethod(null);
            passwordInputLayout.setEndIconDrawable(R.drawable.ic_visibility);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
