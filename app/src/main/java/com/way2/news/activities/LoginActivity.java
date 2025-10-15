package com.way2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
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
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.way2.news.MainActivity;
import com.way2.news.R;
import com.way2.news.utils.PreferenceManager;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin, btnPhoneLogin;
    private TextView tvSignUp, tvForgotPassword, tvSkipLogin, tvAdminLogin;
    private ImageView ivPasswordToggle;
    private ProgressBar progressBar;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;
    private boolean isPasswordVisible = false;
    
    // Google Sign-In
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 1001;
    
    // Phone Authentication
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initializeViews();
        setupClickListeners();
        setupGoogleSignIn();
        initializeFirebase();
        checkCurrentUser();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleLogin = findViewById(R.id.btn_google_login);
        btnPhoneLogin = findViewById(R.id.btn_phone_login);
        tvSignUp = findViewById(R.id.tv_sign_up);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSkipLogin = findViewById(R.id.tv_skip_login);
        tvAdminLogin = findViewById(R.id.tv_admin_login);
        ivPasswordToggle = findViewById(R.id.iv_password_toggle);
        progressBar = findViewById(R.id.progress_bar);
        
        preferenceManager = new PreferenceManager(this);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(this);
        btnGoogleLogin.setOnClickListener(this);
        btnPhoneLogin.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        tvSkipLogin.setOnClickListener(this);
        tvAdminLogin.setOnClickListener(this);
        ivPasswordToggle.setOnClickListener(this);
    }
    
    private void setupGoogleSignIn() {
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Respect last login method and route accordingly
            String loginMethod = preferenceManager.getLoginMethod();
            if ("admin".equals(loginMethod)) {
                navigateToAdminDashboard();
            } else {
                navigateToMainActivity();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btn_login) {
            performLogin();
        } else if (id == R.id.btn_google_login) {
            performGoogleLogin();
        } else if (id == R.id.btn_phone_login) {
            performPhoneLogin();
        } else if (id == R.id.tv_sign_up) {
            navigateToSignUp();
        } else if (id == R.id.tv_forgot_password) {
            navigateToForgotPassword();
        } else if (id == R.id.tv_skip_login) {
            skipLogin();
        } else if (id == R.id.tv_admin_login) {
            navigateToAdminLogin();
        } else if (id == R.id.iv_password_toggle) {
            togglePasswordVisibility();
        }
    }

    private void performLogin() {
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
                    showProgress(false);
                    
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to preferences
                            preferenceManager.setUserId(user.getUid());
                            preferenceManager.setUserEmail(user.getEmail());
                            preferenceManager.setUserName(user.getDisplayName());

                            // Backfill Firestore profile if missing
                            createUserProfileIfMissing(user);

                            // Check if this user is an admin; if yes, mark method and route to admin
                            checkIfAdminAndRoute(user.getUid());
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void checkIfAdminAndRoute(String userId) {
        // If admins collection has this uid with isAdmin==true, treat as admin login
        firestore.collection("admins")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        boolean isAdmin = document != null && document.exists() && Boolean.TRUE.equals(document.getBoolean("isAdmin"));
                        if (isAdmin) {
                            preferenceManager.setLoginMethod("admin");
                            Toast.makeText(LoginActivity.this, "Admin login successful!", Toast.LENGTH_SHORT).show();
                            navigateToAdminDashboard();
                        } else {
                            // Regular email/password login
                            preferenceManager.setLoginMethod("email");
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        }
                    } else {
                        // On failure to verify, default to regular user route
                        preferenceManager.setLoginMethod("email");
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    }
                });
    }

    private void performGoogleLogin() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void performPhoneLogin() {
        // Show phone number input dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Phone Authentication");
        
        // Create input field
        final android.widget.EditText phoneInput = new android.widget.EditText(this);
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneInput.setHint("Enter your phone number");
        phoneInput.setText("+91"); // Default country code
        
        builder.setView(phoneInput);
        builder.setMessage("Enter your phone number to receive OTP");
        
        builder.setPositiveButton("Send OTP", (dialog, which) -> {
            String phoneNumber = phoneInput.getText().toString().trim();
            if (phoneNumber.length() >= 10) {
                sendOTP(phoneNumber);
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void sendOTP(String phoneNumber) {
        // Show loading dialog
        android.app.AlertDialog loadingDialog = new android.app.AlertDialog.Builder(this)
                .setMessage("Sending OTP to " + phoneNumber + "...")
                .setCancelable(false)
                .create();
        loadingDialog.show();
        
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        loadingDialog.dismiss();
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                        loadingDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        loadingDialog.dismiss();
                        LoginActivity.this.verificationId = verificationId;
                        LoginActivity.this.resendToken = token;
                        showOTPVerificationDialog(phoneNumber);
                    }
                })
                .build();
        
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    
    private void showOTPVerificationDialog(String phoneNumber) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Verify OTP");
        
        // Create OTP input field
        final android.widget.EditText otpInput = new android.widget.EditText(this);
        otpInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        otpInput.setHint("Enter 6-digit OTP");
        otpInput.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(6)});
        
        builder.setView(otpInput);
        builder.setMessage("Enter the OTP sent to " + phoneNumber);
        
        builder.setPositiveButton("Verify", (dialog, which) -> {
            String otp = otpInput.getText().toString().trim();
            if (otp.length() == 6) {
                verifyOTP(phoneNumber, otp);
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Resend OTP", (dialog, which) -> {
            sendOTP(phoneNumber);
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }
    
    private void verifyOTP(String phoneNumber, String otp) {
        // Show loading dialog
        android.app.AlertDialog loadingDialog = new android.app.AlertDialog.Builder(this)
                .setMessage("Verifying OTP...")
                .setCancelable(false)
                .create();
        loadingDialog.show();
        
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }
    
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Save user data to preferences
                                preferenceManager.setUserId(user.getUid());
                                preferenceManager.setUserEmail(user.getPhoneNumber() + "@phone.local");
                                preferenceManager.setUserDisplayName("Phone User");
                                preferenceManager.setLoginMethod("phone");
                                
                                // Save user to Firestore
                                savePhoneUserToFirestore(user);
                                
                                Toast.makeText(LoginActivity.this, "Phone authentication successful!", Toast.LENGTH_SHORT).show();
                                navigateToMainActivity();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "OTP verification failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void savePhoneUserToFirestore(FirebaseUser user) {
        DocumentReference userRef = firestore.collection("users").document(user.getUid());
        
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("email", user.getPhoneNumber() + "@phone.local");
        userData.put("displayName", "Phone User");
        userData.put("loginMethod", "phone");
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("lastLoginAt", FieldValue.serverTimestamp());
        
        userRef.set(userData, SetOptions.merge());
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToAdminLogin() {
        Intent intent = new Intent(this, AdminLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void skipLogin() {
        // Allow user to use app without login
        navigateToMainActivity();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Save user data to preferences
                                preferenceManager.setUserId(user.getUid());
                                preferenceManager.setUserEmail(user.getEmail());
                                preferenceManager.setUserDisplayName(user.getDisplayName());
                                preferenceManager.setLoginMethod("google");
                                
                                // Save user to Firestore
                                saveUserToFirestore(user);
                                
                                Toast.makeText(LoginActivity.this, "Signed in with Google successfully!", Toast.LENGTH_SHORT).show();
                                navigateToMainActivity();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void saveUserToFirestore(FirebaseUser user) {
        DocumentReference userRef = firestore.collection("users").document(user.getUid());
        
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("displayName", user.getDisplayName());
        userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userData.put("loginMethod", "google");
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("lastLoginAt", FieldValue.serverTimestamp());
        
        userRef.set(userData, SetOptions.merge());
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void createUserProfileIfMissing(@NonNull FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("users").document(user.getUid());

        userDoc.get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && snapshot.exists()) {
                userDoc.set(new java.util.HashMap<String, Object>() {{
                    put("updatedAt", FieldValue.serverTimestamp());
                }}, SetOptions.merge());
                return;
            }

            String displayName = user.getDisplayName();
            java.util.Map<String, Object> profile = new java.util.HashMap<>();
            profile.put("uid", user.getUid());
            profile.put("email", user.getEmail());
            profile.put("displayName", displayName);
            profile.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
            profile.put("provider", "password");
            profile.put("language", preferenceManager != null ? preferenceManager.getSelectedLanguage() : "en");
            profile.put("role", "user");
            profile.put("createdAt", FieldValue.serverTimestamp());
            profile.put("updatedAt", FieldValue.serverTimestamp());

            userDoc.set(profile, SetOptions.merge());
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnGoogleLogin.setEnabled(!show);
        btnPhoneLogin.setEnabled(!show);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to splash screen
        super.onBackPressed();
        finishAffinity();
    }
}
