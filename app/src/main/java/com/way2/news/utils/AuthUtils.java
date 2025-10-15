package com.way2.news.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthUtils {
    
    /**
     * Check if user is currently logged in
     */
    public static boolean isUserLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }
    
    /**
     * Get current Firebase user
     */
    public static FirebaseUser getCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser();
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        // Password must be at least 6 characters
        return password.length() >= 6;
    }
    
    /**
     * Validate name
     */
    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }
    
    /**
     * Validate phone number (basic validation)
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        // Check if it has 10-15 digits (international format)
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
    }
    
    /**
     * Sign out current user
     */
    public static void signOut() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
    }
    
    /**
     * Get user display name
     */
    public static String getUserDisplayName() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (!TextUtils.isEmpty(displayName)) {
                return displayName;
            }
            // Fallback to email if no display name
            String email = user.getEmail();
            if (!TextUtils.isEmpty(email)) {
                return email.split("@")[0];
            }
        }
        return "User";
    }
    
    /**
     * Get user email
     */
    public static String getUserEmail() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
    
    /**
     * Get user ID
     */
    public static String getUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    /**
     * Check if email is verified
     */
    public static boolean isEmailVerified() {
        FirebaseUser user = getCurrentUser();
        return user != null && user.isEmailVerified();
    }
    
    /**
     * Get error message for Firebase Auth exceptions
     */
    public static String getAuthErrorMessage(String errorCode) {
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                return "Invalid email address";
            case "ERROR_WRONG_PASSWORD":
                return "Incorrect password";
            case "ERROR_USER_NOT_FOUND":
                return "No account found with this email";
            case "ERROR_USER_DISABLED":
                return "This account has been disabled";
            case "ERROR_TOO_MANY_REQUESTS":
                return "Too many failed attempts. Please try again later";
            case "ERROR_OPERATION_NOT_ALLOWED":
                return "This operation is not allowed";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "An account already exists with this email";
            case "ERROR_WEAK_PASSWORD":
                return "Password is too weak";
            case "ERROR_INVALID_CREDENTIAL":
                return "Invalid credentials";
            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                return "An account already exists with the same email but different sign-in credentials";
            default:
                return "Authentication failed. Please try again";
        }
    }
    
    /**
     * Save user data to preferences
     */
    public static void saveUserDataToPreferences(Context context) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            PreferenceManager preferenceManager = new PreferenceManager(context);
            preferenceManager.setUserId(user.getUid());
            preferenceManager.setUserEmail(user.getEmail());
            preferenceManager.setUserName(user.getDisplayName());
        }
    }
    
    /**
     * Clear user data from preferences
     */
    public static void clearUserDataFromPreferences(Context context) {
        PreferenceManager preferenceManager = new PreferenceManager(context);
        preferenceManager.clearUserData();
    }
}
