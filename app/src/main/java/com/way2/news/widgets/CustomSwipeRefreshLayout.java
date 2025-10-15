package com.way2.news.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.way2.news.R;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    private Paint paint;
    private Path path;
    private float rotationAngle = 0f;
    private float scale = 1f;
    private boolean isRefreshing = false;
    
    public CustomSwipeRefreshLayout(Context context) {
        super(context);
        init();
    }
    
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(4f);
        
        path = new Path();
        
        // Set custom colors
        setColorSchemeColors(
            ContextCompat.getColor(getContext(), R.color.primary),
            ContextCompat.getColor(getContext(), R.color.secondary),
            ContextCompat.getColor(getContext(), R.color.like_color)
        );
        
        // Set progress background
        setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(getContext(), R.color.background_white)
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (isRefreshing) {
            drawCustomRefreshIndicator(canvas);
        }
    }
    
    private void drawCustomRefreshIndicator(Canvas canvas) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = 30;
        
        // Save canvas state
        canvas.save();
        
        // Rotate canvas
        canvas.rotate(rotationAngle, centerX, centerY);
        
        // Scale canvas
        canvas.scale(scale, scale, centerX, centerY);
        
        // Draw custom refresh icon (news icon)
        drawNewsIcon(canvas, centerX, centerY, radius);
        
        // Restore canvas state
        canvas.restore();
    }
    
    private void drawNewsIcon(Canvas canvas, int centerX, int centerY, int radius) {
        // Draw newspaper icon
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        
        // Main rectangle (newspaper)
        canvas.drawRect(
            centerX - radius/2, centerY - radius,
            centerX + radius/2, centerY + radius,
            paint
        );
        
        // Lines on newspaper
        paint.setColor(ContextCompat.getColor(getContext(), R.color.background_white));
        paint.setStrokeWidth(2f);
        paint.setStyle(Paint.Style.STROKE);
        
        for (int i = 0; i < 3; i++) {
            int y = centerY - radius/2 + (i * radius/3);
            canvas.drawLine(
                centerX - radius/3, y,
                centerX + radius/3, y,
                paint
            );
        }
        
        // Reset paint
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.primary));
    }
    
    public void startCustomAnimation() {
        isRefreshing = true;
        
        // Rotation animation
        RotateAnimation rotateAnimation = new RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        
        // Scale animation
        ScaleAnimation scaleAnimation = new ScaleAnimation(
            1f, 1.2f, 1f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(2000);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        
        startAnimation(rotateAnimation);
        startAnimation(scaleAnimation);
        
        invalidate();
    }
    
    public void stopCustomAnimation() {
        isRefreshing = false;
        clearAnimation();
        invalidate();
    }
    
    @Override
    public void setRefreshing(boolean refreshing) {
        super.setRefreshing(refreshing);
        
        if (refreshing) {
            startCustomAnimation();
        } else {
            stopCustomAnimation();
        }
    }
}
