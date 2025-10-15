package com.way2.news.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class FoldingItemAnimator extends DefaultItemAnimator {
    private static final int ANIMATION_DURATION = 300;

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        View view = holder.itemView;
        
        // Set initial state
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setRotationY(15f);
        
        // Animate to final state
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(view, "rotationY", 15f, 0f);
        
        alphaAnim.setDuration(ANIMATION_DURATION);
        scaleXAnim.setDuration(ANIMATION_DURATION);
        scaleYAnim.setDuration(ANIMATION_DURATION);
        rotationAnim.setDuration(ANIMATION_DURATION);
        
        alphaAnim.setInterpolator(new DecelerateInterpolator());
        scaleXAnim.setInterpolator(new OvershootInterpolator(1.2f));
        scaleYAnim.setInterpolator(new OvershootInterpolator(1.2f));
        rotationAnim.setInterpolator(new DecelerateInterpolator());
        
        alphaAnim.start();
        scaleXAnim.start();
        scaleYAnim.start();
        rotationAnim.start();
        
        return true;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        View view = holder.itemView;
        
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f);
        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(view, "rotationY", 0f, -15f);
        
        alphaAnim.setDuration(ANIMATION_DURATION);
        scaleXAnim.setDuration(ANIMATION_DURATION);
        scaleYAnim.setDuration(ANIMATION_DURATION);
        rotationAnim.setDuration(ANIMATION_DURATION);
        
        alphaAnim.setInterpolator(new DecelerateInterpolator());
        scaleXAnim.setInterpolator(new DecelerateInterpolator());
        scaleYAnim.setInterpolator(new DecelerateInterpolator());
        rotationAnim.setInterpolator(new DecelerateInterpolator());
        
        alphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchRemoveFinished(holder);
            }
        });
        
        alphaAnim.start();
        scaleXAnim.start();
        scaleYAnim.start();
        rotationAnim.start();
        
        return true;
    }
}
