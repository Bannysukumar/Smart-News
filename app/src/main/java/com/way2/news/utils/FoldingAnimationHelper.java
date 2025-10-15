package com.way2.news.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class FoldingAnimationHelper {
    
    public static void animateNewsItemAppear(View view, int delay) {
        // Set initial state
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setRotationY(30f);
        view.setTranslationZ(-200f);
        view.setCameraDistance(view.getWidth() * 6);
        
        // Animate to final state
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(view, "rotationY", 30f, 0f);
        ObjectAnimator translationZAnim = ObjectAnimator.ofFloat(view, "translationZ", -200f, 0f);
        
        alphaAnim.setDuration(400);
        scaleXAnim.setDuration(400);
        scaleYAnim.setDuration(400);
        rotationAnim.setDuration(400);
        translationZAnim.setDuration(400);
        
        alphaAnim.setStartDelay(delay);
        scaleXAnim.setStartDelay(delay);
        scaleYAnim.setStartDelay(delay);
        rotationAnim.setStartDelay(delay);
        translationZAnim.setStartDelay(delay);
        
        alphaAnim.setInterpolator(new DecelerateInterpolator());
        scaleXAnim.setInterpolator(new OvershootInterpolator(1.1f));
        scaleYAnim.setInterpolator(new OvershootInterpolator(1.1f));
        rotationAnim.setInterpolator(new DecelerateInterpolator());
        translationZAnim.setInterpolator(new DecelerateInterpolator());
        
        alphaAnim.start();
        scaleXAnim.start();
        scaleYAnim.start();
        rotationAnim.start();
        translationZAnim.start();
    }
    
    public static void animateNewsItemDisappear(View view, AnimatorListenerAdapter listener) {
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f);
        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(view, "rotationY", 0f, -30f);
        ObjectAnimator translationZAnim = ObjectAnimator.ofFloat(view, "translationZ", 0f, -200f);
        
        alphaAnim.setDuration(300);
        scaleXAnim.setDuration(300);
        scaleYAnim.setDuration(300);
        rotationAnim.setDuration(300);
        translationZAnim.setDuration(300);
        
        alphaAnim.setInterpolator(new DecelerateInterpolator());
        scaleXAnim.setInterpolator(new DecelerateInterpolator());
        scaleYAnim.setInterpolator(new DecelerateInterpolator());
        rotationAnim.setInterpolator(new DecelerateInterpolator());
        translationZAnim.setInterpolator(new DecelerateInterpolator());
        
        if (listener != null) {
            alphaAnim.addListener(listener);
        }
        
        alphaAnim.start();
        scaleXAnim.start();
        scaleYAnim.start();
        rotationAnim.start();
        translationZAnim.start();
    }
    
    public static void animatePageFold(View view, float foldAmount) {
        float scale = 1f - foldAmount * 0.15f;
        float alpha = 1f - foldAmount * 0.3f;
        float rotation = foldAmount * 20f;
        float translationZ = -foldAmount * 100f;
        
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .alpha(alpha)
            .rotationY(rotation)
            .translationZ(translationZ)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
    
    public static void animatePageUnfold(View view) {
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .rotationY(0f)
            .translationZ(0f)
            .setDuration(200)
            .setInterpolator(new OvershootInterpolator(1.1f))
            .start();
    }
}
