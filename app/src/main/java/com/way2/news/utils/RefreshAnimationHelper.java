package com.way2.news.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.way2.news.R;

public class RefreshAnimationHelper {
    
    public static void animateRefreshStart(SwipeRefreshLayout swipeRefreshLayout, ImageView refreshIcon, TextView refreshText) {
        if (swipeRefreshLayout == null) return;
        
        // Start the default refresh animation
        swipeRefreshLayout.setRefreshing(true);
        
        // Add custom icon animation
        if (refreshIcon != null) {
            animateSpinningIcon(refreshIcon);
        }
        
        // Add text animation
        if (refreshText != null) {
            animateRefreshText(refreshText);
        }
    }
    
    public static void animateRefreshEnd(SwipeRefreshLayout swipeRefreshLayout, ImageView refreshIcon, TextView refreshText) {
        if (swipeRefreshLayout == null) return;
        
        // Stop the default refresh animation
        swipeRefreshLayout.setRefreshing(false);
        
        // Stop custom animations
        if (refreshIcon != null) {
            refreshIcon.clearAnimation();
            refreshIcon.setRotation(0f);
        }
        
        if (refreshText != null) {
            refreshText.clearAnimation();
            refreshText.setAlpha(1f);
            refreshText.setScaleX(1f);
            refreshText.setScaleY(1f);
        }
    }
    
    private static void animateSpinningIcon(ImageView icon) {
        // Create continuous rotation animation
        ObjectAnimator rotation = ObjectAnimator.ofFloat(icon, "rotation", 0f, 360f);
        rotation.setDuration(1000);
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.setInterpolator(new LinearInterpolator());
        
        // Add scale pulse effect
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Start animations
        rotation.start();
        scaleX.start();
        scaleY.start();
    }
    
    private static void animateRefreshText(TextView textView) {
        // Create pulsing text animation
        ObjectAnimator alpha = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0.5f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.05f, 1f);
        
        alpha.setDuration(1500);
        scaleX.setDuration(1500);
        scaleY.setDuration(1500);
        
        alpha.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        alpha.start();
        scaleX.start();
        scaleY.start();
    }
    
    public static void animateNewsItemRefresh(View newsItem, int delay) {
        if (newsItem == null) return;
        
        // Set initial state
        newsItem.setAlpha(0f);
        newsItem.setScaleX(0.9f);
        newsItem.setScaleY(0.9f);
        newsItem.setTranslationY(50f);
        
        // Create refresh animation
        ObjectAnimator alpha = ObjectAnimator.ofFloat(newsItem, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(newsItem, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(newsItem, "scaleY", 0.9f, 1f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(newsItem, "translationY", 50f, 0f);
        
        alpha.setDuration(300);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        translationY.setDuration(300);
        
        alpha.setStartDelay(delay);
        scaleX.setStartDelay(delay);
        scaleY.setStartDelay(delay);
        translationY.setStartDelay(delay);
        
        alpha.setInterpolator(new DecelerateInterpolator());
        scaleX.setInterpolator(new OvershootInterpolator(1.1f));
        scaleY.setInterpolator(new OvershootInterpolator(1.1f));
        translationY.setInterpolator(new DecelerateInterpolator());
        
        alpha.start();
        scaleX.start();
        scaleY.start();
        translationY.start();
    }
    
    public static void animateRefreshSuccess(View container, Context context) {
        if (container == null) return;
        
        // Create success animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(container, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(container, "scaleY", 1f, 1.05f, 1f);
        
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.setInterpolator(new OvershootInterpolator(1.2f));
        scaleY.setInterpolator(new OvershootInterpolator(1.2f));
        
        // Add color flash effect
        ValueAnimator colorFlash = ValueAnimator.ofFloat(0f, 1f, 0f);
        colorFlash.setDuration(400);
        colorFlash.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            int color = interpolateColor(
                ContextCompat.getColor(context, R.color.background_white),
                ContextCompat.getColor(context, R.color.success),
                progress
            );
            container.setBackgroundColor(color);
        });
        
        colorFlash.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.background_white));
            }
        });
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, colorFlash);
        animatorSet.start();
    }
    
    public static void animateRefreshError(View container, Context context) {
        if (container == null) return;
        
        // Create error shake animation
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(container, "translationX", 0f, -10f, 10f, -10f, 10f, 0f);
        shakeX.setDuration(500);
        shakeX.setInterpolator(new DecelerateInterpolator());
        
        // Add error color flash
        ValueAnimator colorFlash = ValueAnimator.ofFloat(0f, 1f, 0f);
        colorFlash.setDuration(500);
        colorFlash.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            int color = interpolateColor(
                ContextCompat.getColor(context, R.color.background_white),
                ContextCompat.getColor(context, R.color.error),
                progress
            );
            container.setBackgroundColor(color);
        });
        
        colorFlash.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.background_white));
            }
        });
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(shakeX, colorFlash);
        animatorSet.start();
    }
    
    private static int interpolateColor(int color1, int color2, float ratio) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        
        return Color.rgb(r, g, b);
    }
}
