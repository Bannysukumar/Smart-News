package com.way2.news.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class NewsItemAnimator extends DefaultItemAnimator {
    private static final int ANIMATION_DURATION = 400;
    private static final int ANIMATION_DELAY = 100;

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        View view = holder.itemView;
        
        // Set initial state for folding effect
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setRotationY(30f);
        view.setTranslationZ(-200f);
        view.setCameraDistance(view.getWidth() * 6);
        
        // Create folding animation
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.8f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1f);
        PropertyValuesHolder rotationY = PropertyValuesHolder.ofFloat("rotationY", 30f, 0f);
        PropertyValuesHolder translationZ = PropertyValuesHolder.ofFloat("translationZ", -200f, 0f);
        
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, alpha, scaleX, scaleY, rotationY, translationZ);
        animator.setDuration(ANIMATION_DURATION);
        animator.setStartDelay(ANIMATION_DELAY);
        animator.setInterpolator(new OvershootInterpolator(1.1f));
        
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchAddFinished(holder);
            }
        });
        
        animator.start();
        return true;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        View view = holder.itemView;
        
        // Create folding out animation
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.8f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.8f);
        PropertyValuesHolder rotationY = PropertyValuesHolder.ofFloat("rotationY", 0f, -30f);
        PropertyValuesHolder translationZ = PropertyValuesHolder.ofFloat("translationZ", 0f, -200f);
        
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, alpha, scaleX, scaleY, rotationY, translationZ);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchRemoveFinished(holder);
            }
        });
        
        animator.start();
        return true;
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        View view = holder.itemView;
        
        // Add slight folding effect during move
        view.setRotationY(5f);
        view.setTranslationZ(-50f);
        
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 5f, 0f);
        animator.setDuration(ANIMATION_DURATION / 2);
        animator.setInterpolator(new DecelerateInterpolator());
        
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchMoveFinished(holder);
            }
        });
        
        animator.start();
        return true;
    }
}
