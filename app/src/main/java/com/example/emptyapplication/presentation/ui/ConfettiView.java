package com.example.emptyapplication.presentation.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiView extends View {

    private static class Particle {
        float x;
        float y;
        float size;
        float speedY;
        float speedX;
        float rotation;
        float rotationSpeed;
        int color;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private ValueAnimator animator;

    private static final int[] CONFETTI_COLORS = new int[]{
            Color.parseColor("#F5BE2B"), // Gold
            Color.parseColor("#853BEB"), // Primary
            Color.parseColor("#00B5D5"), // Accent
            Color.parseColor("#18BA82"), // Success
            Color.parseColor("#ED4040"), // Destructive
            Color.parseColor("#BA5BCF")  // Violet
    };

    public ConfettiView(Context context) {
        super(context);
        init();
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            setupParticles(w, h);
            startAnimation();
        }
    }

    private void setupParticles(int width, int height) {
        particles.clear();
        int count = 55;
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = random.nextFloat() * width;
            p.y = -random.nextFloat() * height;
            p.size = 14f + random.nextFloat() * 14f;
            p.speedY = 3.5f + random.nextFloat() * 4.5f;
            p.speedX = -1.5f + random.nextFloat() * 3.0f;
            p.rotation = random.nextFloat() * 360f;
            p.rotationSpeed = -4f + random.nextFloat() * 8f;
            p.color = CONFETTI_COLORS[random.nextInt(CONFETTI_COLORS.length)];
            particles.add(p);
        }
    }

    public void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(16);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            int h = getHeight();
            int w = getWidth();
            if (h <= 0 || w <= 0) return;

            for (Particle p : particles) {
                p.y += p.speedY;
                p.x += p.speedX;
                p.rotation += p.rotationSpeed;

                if (p.y > h) {
                    p.y = -p.size;
                    p.x = random.nextFloat() * w;
                }
                if (p.x < 0) {
                    p.x = w;
                } else if (p.x > w) {
                    p.x = 0;
                }
            }
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Particle p : particles) {
            paint.setColor(p.color);
            canvas.save();
            canvas.translate(p.x, p.y);
            canvas.rotate(p.rotation);
            RectF rect = new RectF(-p.size / 2, -p.size / 4, p.size / 2, p.size / 4);
            canvas.drawRoundRect(rect, 4f, 4f, paint);
            canvas.restore();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
