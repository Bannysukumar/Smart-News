package com.way2.news.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.core.content.ContextCompat;

import com.way2.news.R;

public class AnimatedProgressBar extends View {
    private Paint paint;
    private RectF rectF;
    private float progress = 0f;
    private float rotationAngle = 0f;
    private boolean isAnimating = false;
    private int primaryColor;
    private int secondaryColor;
    
    public AnimatedProgressBar(Context context) {
        super(context);
        init();
    }
    
    public AnimatedProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AnimatedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        
        primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        secondaryColor = ContextCompat.getColor(getContext(), R.color.secondary);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (isAnimating) {
            drawAnimatedProgress(canvas);
        }
    }
    
    private void drawAnimatedProgress(Canvas canvas) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(getWidth(), getHeight()) / 2 - 20;
        
        // Draw background circle
        paint.setColor(secondaryColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setAlpha(50);
        canvas.drawCircle(centerX, centerY, radius, paint);
        
        // Draw progress arc
        paint.setColor(primaryColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setAlpha(255);
        paint.setStrokeCap(Paint.Cap.ROUND);
        
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        
        // Rotate canvas for spinning effect
        canvas.save();
        canvas.rotate(rotationAngle, centerX, centerY);
        
        // Draw progress arc
        float sweepAngle = progress * 360f;
        canvas.drawArc(rectF, -90f, sweepAngle, false, paint);
        
        canvas.restore();
        
        // Draw center dot
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(primaryColor);
        canvas.drawCircle(centerX, centerY, 8f, paint);
    }
    
    public void startAnimation() {
        isAnimating = true;
        
        // Progress animation
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(0f, 1f);
        progressAnimator.setDuration(2000);
        progressAnimator.setRepeatCount(ValueAnimator.INFINITE);
        progressAnimator.setRepeatMode(ValueAnimator.RESTART);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        // Rotation animation
        ValueAnimator rotationAnimator = ValueAnimator.ofFloat(0f, 360f);
        rotationAnimator.setDuration(1000);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.addUpdateListener(animation -> {
            rotationAngle = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        // Scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        progressAnimator.start();
        rotationAnimator.start();
        scaleX.start();
        scaleY.start();
    }
    
    public void stopAnimation() {
        isAnimating = false;
        clearAnimation();
        progress = 0f;
        rotationAngle = 0f;
        setScaleX(1f);
        setScaleY(1f);
        invalidate();
    }
    
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }
    
    public float getProgress() {
        return progress;
    }
}
