package com.example.agri_detect;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

public class StatusOrbView extends View {

    public static final int UNKNOWN = 0;
    public static final int NORMAL = 1;
    public static final int GAS = 2;

    private int state = UNKNOWN;

    private Paint paintCenter;
    private Paint paintGlow;
    private Paint paintOuter;

    private float animFactor = 0f;
    private ValueAnimator pulseAnimator;

    public StatusOrbView(Context context) {
        super(context);
        init();
    }

    public StatusOrbView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusOrbView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenter.setStyle(Paint.Style.FILL);

        paintGlow = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGlow.setStyle(Paint.Style.FILL);

        paintOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintOuter.setStyle(Paint.Style.STROKE);

        pulseAnimator = ValueAnimator.ofFloat(0.8f, 1.2f);
        pulseAnimator.setDuration(1200);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animator -> {
            animFactor = (float) animator.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    public void setState(int state) {
        if (this.state != state) {
            this.state = state;
            invalidate();
        }
    }

    public int getState() {
        return state;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float cx = width / 2f;
        float cy = height / 2f;
        float baseRadius = Math.min(cx, cy) * 0.55f;

        int centerColor;
        int glowColor;
        int outerColor;

        switch (state) {
            case GAS:
                centerColor = 0xFFFF5D4D; // Soft Red
                glowColor = 0x40FF5D4D;
                outerColor = 0x80FF5D4D;
                break;
            case NORMAL:
                centerColor = 0xFF2E7D6B; // Deep Green
                glowColor = 0x302E7D6B;
                outerColor = 0x602E7D6B;
                break;
            case UNKNOWN:
            default:
                centerColor = 0xFFFFB547; // Warm Amber
                glowColor = 0x30FFB547;
                outerColor = 0x60FFB547;
                break;
        }

        float pulsedGlowRadius = baseRadius * 1.5f * (state == GAS ? animFactor : 1.2f);
        float pulsedOuterRadius = baseRadius * 1.8f * (state == GAS ? animFactor : 1.35f);

        // Draw outer ring
        paintOuter.setColor(outerColor);
        paintOuter.setStrokeWidth(baseRadius * 0.1f);
        canvas.drawCircle(cx, cy, pulsedOuterRadius, paintOuter);

        // Draw glow aura
        paintGlow.setColor(glowColor);
        canvas.drawCircle(cx, cy, pulsedGlowRadius, paintGlow);

        // Draw solid center orb
        paintCenter.setColor(centerColor);
        canvas.drawCircle(cx, cy, baseRadius, paintCenter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
    }
}
