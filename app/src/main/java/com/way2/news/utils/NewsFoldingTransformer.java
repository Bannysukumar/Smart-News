package com.way2.news.utils;

import android.view.View;
import androidx.viewpager2.widget.ViewPager2;

public class NewsFoldingTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.7f;
    private static final float MAX_ROTATION = 25f;
    private static final float FOLD_DEPTH = 0.2f;

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();
        int pageHeight = page.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // Page is completely off-screen to the left
            page.setAlpha(0f);
            page.setRotationY(0f);
            page.setScaleX(1f);
            page.setScaleY(1f);
            page.setTranslationX(0f);
            page.setTranslationZ(0f);
        } else if (position <= 0) { // [-1,0]
            // Page is moving out to the left or is the current page
            float alpha = 1f + position * (1f - MIN_ALPHA);
            page.setAlpha(Math.max(MIN_ALPHA, alpha));
            
            // Scale down as it moves out
            float scale = 1f + position * (1f - MIN_SCALE);
            page.setScaleX(Math.max(MIN_SCALE, scale));
            page.setScaleY(Math.max(MIN_SCALE, scale));
            
            // Slight rotation for outgoing pages
            float rotation = -position * 10f;
            page.setRotationY(rotation);
            page.setTranslationX(0f);
            page.setTranslationZ(0f);
            
        } else if (position <= 1) { // (0,1]
            // Page is coming in from the right
            float alpha = MIN_ALPHA + (1f - MIN_ALPHA) * (1f - position);
            page.setAlpha(alpha);
            
            // Scale up as it comes in
            float scale = MIN_SCALE + (1f - MIN_SCALE) * (1f - position);
            page.setScaleX(scale);
            page.setScaleY(scale);
            
            // Folding rotation effect
            float rotation = MAX_ROTATION * (1f - position);
            page.setRotationY(rotation);
            
            // Counteract the default slide transition
            page.setTranslationX(pageWidth * -position);
            
            // Add depth for 3D folding effect
            float depth = FOLD_DEPTH * position;
            page.setTranslationZ(-depth * pageWidth);
            
            // Set camera distance for perspective
            page.setCameraDistance(pageWidth * 5);
            
        } else { // (1,+Infinity]
            // Page is completely off-screen to the right
            page.setAlpha(0f);
            page.setRotationY(0f);
            page.setScaleX(1f);
            page.setScaleY(1f);
            page.setTranslationX(0f);
            page.setTranslationZ(0f);
        }
    }
}
