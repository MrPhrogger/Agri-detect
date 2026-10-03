package com.example.agri_detect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class WaveOrbView extends View {

    private float percent = Float.NaN;

    private Paint bgCirclePaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF circleBounds;

    public WaveOrbView(Context context) {
        super(context);
        init();
    }

    public WaveOrbView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveOrbView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circleBounds = new RectF();

        bgCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgCirclePaint.setStyle(Paint.Style.STROKE);
        bgCirclePaint.setColor(0xFFE5E7EB);
        bgCirclePaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(0xFF6C8EA0); // Slate Blue
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF1F2937);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    public void setPercent(float percent) {
        this.percent = percent;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float strokeWidth = width * 0.12f;
        bgCirclePaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeWidth(strokeWidth);

        float padding = strokeWidth * 0.8f;
        circleBounds.set(padding, padding, width - padding, height - padding);

        // Background circle
        canvas.drawArc(circleBounds, 0, 360, false, bgCirclePaint);

        // Progress arc from top (-90 degrees)
        if (!Float.isNaN(percent)) {
            float p = Math.max(0f, Math.min(100f, percent));
            float sweepAngle = 360f * (p / 100f);
            canvas.drawArc(circleBounds, -90f, sweepAngle, false, progressPaint);
        }

        // Center text
        textPaint.setTextSize(width * 0.22f);
        float centerY = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        String text = Float.isNaN(percent)
                ? "--"
                : String.format(Locale.US, "%.0f%%", percent);
        canvas.drawText(text, width / 2f, centerY, textPaint);
    }
}
