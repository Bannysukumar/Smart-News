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
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.way2.news.R;

public class LikeAnimationHelper {
    
    public static void animateLike(ImageView likeIcon, TextView likeCount, boolean isLiked, Context context) {
        if (likeIcon == null) return;
        
        // Create animation set
        AnimatorSet animatorSet = new AnimatorSet();
        
        // Scale animation with bounce effect
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(likeIcon, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(likeIcon, "scaleY", 1f, 1.3f, 1f);
        
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.setInterpolator(new BounceInterpolator());
        scaleY.setInterpolator(new BounceInterpolator());
        
        // Color transition animation
        ValueAnimator colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(300);
        colorAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            if (isLiked) {
                // Transition to red color
                int color = interpolateColor(
                    ContextCompat.getColor(context, R.color.textSecondary),
                    ContextCompat.getColor(context, R.color.like_color),
                    progress
                );
                likeIcon.setColorFilter(color);
            } else {
                // Transition back to default color
                int color = interpolateColor(
                    ContextCompat.getColor(context, R.color.like_color),
                    ContextCompat.getColor(context, R.color.textSecondary),
                    progress
                );
                likeIcon.setColorFilter(color);
            }
        });
        
        // Count animation
        if (likeCount != null) {
            ObjectAnimator countScaleX = ObjectAnimator.ofFloat(likeCount, "scaleX", 1f, 1.2f, 1f);
            ObjectAnimator countScaleY = ObjectAnimator.ofFloat(likeCount, "scaleY", 1f, 1.2f, 1f);
            
            countScaleX.setDuration(200);
            countScaleY.setDuration(200);
            countScaleX.setInterpolator(new OvershootInterpolator(1.2f));
            countScaleY.setInterpolator(new OvershootInterpolator(1.2f));
            
            animatorSet.playTogether(scaleX, scaleY, colorAnimator, countScaleX, countScaleY);
        } else {
            animatorSet.playTogether(scaleX, scaleY, colorAnimator);
        }
        
        animatorSet.start();
    }
    
    public static void animateHeartBurst(ImageView likeIcon, Context context) {
        if (likeIcon == null) return;
        
        // Create heart burst effect
        AnimatorSet burstSet = new AnimatorSet();
        
        // Main heart animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(likeIcon, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(likeIcon, "scaleY", 1f, 1.5f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(likeIcon, "rotation", 0f, 15f, -15f, 0f);
        
        scaleX.setDuration(400);
        scaleY.setDuration(400);
        rotation.setDuration(400);
        
        scaleX.setInterpolator(new OvershootInterpolator(1.5f));
        scaleY.setInterpolator(new OvershootInterpolator(1.5f));
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Color pulse animation
        ValueAnimator colorPulse = ValueAnimator.ofFloat(0f, 1f, 0f);
        colorPulse.setDuration(400);
        colorPulse.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            int color = interpolateColor(
                ContextCompat.getColor(context, R.color.like_color),
                Color.WHITE,
                progress
            );
            likeIcon.setColorFilter(color);
        });
        
        burstSet.playTogether(scaleX, scaleY, rotation, colorPulse);
        burstSet.start();
    }
    
    public static void animateDislike(ImageView dislikeIcon, TextView dislikeCount, boolean isDisliked, Context context) {
        if (dislikeIcon == null) return;
        
        AnimatorSet animatorSet = new AnimatorSet();
        
        // Scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dislikeIcon, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dislikeIcon, "scaleY", 1f, 1.2f, 1f);
        
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        
        // Color transition
        ValueAnimator colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(200);
        colorAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            if (isDisliked) {
                int color = interpolateColor(
                    ContextCompat.getColor(context, R.color.textSecondary),
                    ContextCompat.getColor(context, R.color.error),
                    progress
                );
                dislikeIcon.setColorFilter(color);
            } else {
                int color = interpolateColor(
                    ContextCompat.getColor(context, R.color.error),
                    ContextCompat.getColor(context, R.color.textSecondary),
                    progress
                );
                dislikeIcon.setColorFilter(color);
            }
        });
        
        // Count animation
        if (dislikeCount != null) {
            ObjectAnimator countScaleX = ObjectAnimator.ofFloat(dislikeCount, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator countScaleY = ObjectAnimator.ofFloat(dislikeCount, "scaleY", 1f, 1.1f, 1f);
            
            countScaleX.setDuration(150);
            countScaleY.setDuration(150);
            countScaleX.setInterpolator(new DecelerateInterpolator());
            countScaleY.setInterpolator(new DecelerateInterpolator());
            
            animatorSet.playTogether(scaleX, scaleY, colorAnimator, countScaleX, countScaleY);
        } else {
            animatorSet.playTogether(scaleX, scaleY, colorAnimator);
        }
        
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
