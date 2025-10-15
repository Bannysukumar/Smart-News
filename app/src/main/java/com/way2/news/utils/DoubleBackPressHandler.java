package com.way2.news.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.way2.news.R;

public class DoubleBackPressHandler {
    private static final int BACK_PRESS_DELAY = 2000; // 2 seconds
    private static final String EXIT_MESSAGE = "Press back again to exit";
    
    private boolean doubleBackToExitPressedOnce = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Activity activity;
    private Runnable backPressRunnable;
    
    public DoubleBackPressHandler(Activity activity) {
        this.activity = activity;
        this.backPressRunnable = () -> doubleBackToExitPressedOnce = false;
    }
    
    public boolean handleBackPress() {
        if (doubleBackToExitPressedOnce) {
            // Second back press - exit the app
            exitApp();
            return true;
        }
        
        // First back press
        doubleBackToExitPressedOnce = true;
        showExitToast();
        
        // Reset the flag after delay
        handler.postDelayed(backPressRunnable, BACK_PRESS_DELAY);
        
        return true; // Always consume the back press to prevent immediate exit
    }
    
    private void showExitToast() {
        // Add haptic feedback
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
        
        // Show exit toast
        Toast.makeText(activity, "Press back again to exit", Toast.LENGTH_SHORT).show();
    }
    
    private void exitApp() {
        // Add stronger haptic feedback for exit
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(100);
            }
        }
        
        // Exit the app
        activity.finishAffinity(); // Close all activities in the task
        System.exit(0); // Force exit
    }
    
    private void animateExit() {
        // Create a more dramatic exit animation
        View decorView = activity.getWindow().getDecorView();
        
        // Use custom animation
        Animation exitAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out_exit);
        decorView.startAnimation(exitAnimation);
    }
    
    public void reset() {
        doubleBackToExitPressedOnce = false;
        handler.removeCallbacks(backPressRunnable);
    }
    
    public void cleanup() {
        handler.removeCallbacks(backPressRunnable);
    }
}
