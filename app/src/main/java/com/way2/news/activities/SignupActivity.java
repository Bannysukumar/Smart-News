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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.way2.news.MainActivity;
import com.way2.news.R;
import com.way2.news.utils.PreferenceManager;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private EditText etUsername;
    private Button btnSignUp, btnGoogleSignUp, btnPhoneSignUp;
    private TextView tvLogin, tvTerms;
    private ImageView ivPasswordToggle, ivConfirmPasswordToggle;
    private ProgressBar progressBar;
    
    private FirebaseAuth firebaseAuth;
    private PreferenceManager preferenceManager;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
    // Google Sign-In
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 1001;
    
    // Phone Authentication
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        initializeViews();
        setupClickListeners();
        setupGoogleSignIn();
        initializeFirebase();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnGoogleSignUp = findViewById(R.id.btn_google_sign_up);
        btnPhoneSignUp = findViewById(R.id.btn_phone_sign_up);
        tvLogin = findViewById(R.id.tv_login);
        tvTerms = findViewById(R.id.tv_terms);
        ivPasswordToggle = findViewById(R.id.iv_password_toggle);
        ivConfirmPasswordToggle = findViewById(R.id.iv_confirm_password_toggle);
        progressBar = findViewById(R.id.progress_bar);
        
        preferenceManager = new PreferenceManager(this);
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(this);
        btnGoogleSignUp.setOnClickListener(this);
        btnPhoneSignUp.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        tvTerms.setOnClickListener(this);
        ivPasswordToggle.setOnClickListener(this);
        ivConfirmPasswordToggle.setOnClickListener(this);
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btn_sign_up) {
            performSignUp();
        } else if (id == R.id.btn_google_sign_up) {
            performGoogleSignUp();
        } else if (id == R.id.btn_phone_sign_up) {
            performPhoneSignUp();
        } else if (id == R.id.tv_login) {
            navigateToLogin();
        } else if (id == R.id.tv_terms) {
            showTermsDialog();
        } else if (id == R.id.iv_password_toggle) {
            togglePasswordVisibility();
        } else if (id == R.id.iv_confirm_password_toggle) {
            toggleConfirmPasswordVisibility();
        }
    }

    private void performSignUp() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username) || username.length() < 3) {
            etUsername.setError("Username (min 3 chars) required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
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

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        showProgress(true);

        // Ensure username uniqueness before creating auth user
        FirebaseFirestore.getInstance().collection("usernames").document(username)
            .get().addOnSuccessListener(doc -> {
                if (doc != null && doc.exists()) {
                    showProgress(false);
                    etUsername.setError("Username already taken");
                    etUsername.requestFocus();
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    showProgress(false);
                    
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Update user profile with name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                            
                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Save user data to preferences
                                            preferenceManager.setUserId(user.getUid());
                                            preferenceManager.setUserEmail(user.getEmail());
                                            preferenceManager.setUserName(name);

                                            // Ensure Firestore user profile exists
                                            createUserProfileIfMissing(user, name);

                                            // Reserve username mapping -> uid
                                            FirebaseFirestore.getInstance().collection("usernames")
                                                .document(username)
                                                .set(new java.util.HashMap<String, Object>() {{
                                                    put("uid", user.getUid());
                                                    put("createdAt", FieldValue.serverTimestamp());
                                                }});

                                            // Store on user profile as well
                                            FirebaseFirestore.getInstance().collection("users")
                                                .document(user.getUid())
                                                .set(new java.util.HashMap<String, Object>() {{
                                                    put("username", username);
                                                    put("updatedAt", FieldValue.serverTimestamp());
                                                }}, SetOptions.merge());
                                            
                                            // Send email verification
                                            sendEmailVerification(user);
                                            
                                            Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                            navigateToMainActivity();
                                        }
                                    }
                                });
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Sign up failed";
                        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
            }).addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(this, "Failed to check username", Toast.LENGTH_SHORT).show();
            });
    }

    private void performGoogleSignUp() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void performPhoneSignUp() {
        // Show phone number input dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Phone Registration");
        
        // Create input field
        final android.widget.EditText phoneInput = new android.widget.EditText(this);
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneInput.setHint("Enter your phone number");
        phoneInput.setText("+91"); // Default country code
        
        builder.setView(phoneInput);
        builder.setMessage("Enter your phone number to create account");
        
        builder.setPositiveButton("Send OTP", (dialog, which) -> {
            String phoneNumber = phoneInput.getText().toString().trim();
            if (phoneNumber.length() >= 10) {
                sendSignUpOTP(phoneNumber);
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void sendSignUpOTP(String phoneNumber) {
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
                        Toast.makeText(SignupActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        loadingDialog.dismiss();
                        SignupActivity.this.verificationId = verificationId;
                        SignupActivity.this.resendToken = token;
                        showSignUpOTPVerificationDialog(phoneNumber);
                    }
                })
                .build();
        
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    
    private void showSignUpOTPVerificationDialog(String phoneNumber) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Verify OTP");
        
        // Create OTP input field
        final android.widget.EditText otpInput = new android.widget.EditText(this);
        otpInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        otpInput.setHint("Enter 6-digit OTP");
        otpInput.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(6)});
        
        builder.setView(otpInput);
        builder.setMessage("Enter the OTP sent to " + phoneNumber);
        
        builder.setPositiveButton("Verify & Sign Up", (dialog, which) -> {
            String otp = otpInput.getText().toString().trim();
            if (otp.length() == 6) {
                verifySignUpOTP(phoneNumber, otp);
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Resend OTP", (dialog, which) -> {
            sendSignUpOTP(phoneNumber);
        });
        
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }
    
    private void verifySignUpOTP(String phoneNumber, String otp) {
        // Show loading dialog
        android.app.AlertDialog loadingDialog = new android.app.AlertDialog.Builder(this)
                .setMessage("Creating your account...")
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
                                
                                Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                navigateToMainActivity();
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void savePhoneUserToFirestore(FirebaseUser user) {
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
        
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

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
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
                Toast.makeText(this, "Google Sign-Up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                
                                Toast.makeText(SignupActivity.this, "Signed up with Google successfully!", Toast.LENGTH_SHORT).show();
                                navigateToMainActivity();
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Google Sign-Up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void saveUserToFirestore(FirebaseUser user) {
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
        
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

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, 
                            "Verification email sent to " + user.getEmail(), 
                            Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignupActivity.this, 
                            "Failed to send verification email", 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void showTermsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Terms of Service")
               .setMessage("By creating an account, you agree to our Terms of Service and Privacy Policy. We will use your information to provide you with personalized news content and improve our services.")
               .setPositiveButton("I Agree", null)
               .show();
    }

    private void createUserProfileIfMissing(@NonNull FirebaseUser user, @NonNull String displayName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("users").document(user.getUid());

        userDoc.get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && snapshot.exists()) {
                // Touch updatedAt
                userDoc.set(new java.util.HashMap<String, Object>() {{
                    put("updatedAt", FieldValue.serverTimestamp());
                }}, SetOptions.merge());
                return;
            }

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

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
            isConfirmPasswordVisible = false;
        } else {
            etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility);
            isConfirmPasswordVisible = true;
        }
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!show);
        btnGoogleSignUp.setEnabled(!show);
        btnPhoneSignUp.setEnabled(!show);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onBackPressed() {
        navigateToLogin();
    }
}
