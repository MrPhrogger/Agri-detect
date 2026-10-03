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

public class ArcGaugeView extends View {

    private float valueCelsius = Float.NaN;

    private Paint arcBgPaint;
    private Paint arcValuePaint;
    private Paint textValPaint;
    private RectF arcBounds;

    public ArcGaugeView(Context context) {
        super(context);
        init();
    }

    public ArcGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcBounds = new RectF();

        arcBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcBgPaint.setStyle(Paint.Style.STROKE);
        arcBgPaint.setColor(0xFFE5E7EB);
        arcBgPaint.setStrokeCap(Paint.Cap.ROUND);

        arcValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcValuePaint.setStyle(Paint.Style.STROKE);
        arcValuePaint.setColor(0xFF2E7D6B); // Deep Green
        arcValuePaint.setStrokeCap(Paint.Cap.ROUND);

        textValPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textValPaint.setColor(0xFF1F2937);
        textValPaint.setTextAlign(Paint.Align.CENTER);
        textValPaint.setFakeBoldText(true);
    }

    public void setValueCelsius(float value) {
        this.valueCelsius = value;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float strokeWidth = width * 0.12f;
        arcBgPaint.setStrokeWidth(strokeWidth);
        arcValuePaint.setStrokeWidth(strokeWidth);

        float padding = strokeWidth * 0.8f;
        arcBounds.set(padding, padding, width - padding, height - padding);

        float startAngle = 135f;
        float totalSweep = 270f;

        // Draw background arc
        canvas.drawArc(arcBounds, startAngle, totalSweep, false, arcBgPaint);

        // Draw progress arc
        if (!Float.isNaN(valueCelsius)) {
            float minTemp = 0f;
            float maxTemp = 50f;
            float fraction = Math.max(0f, Math.min(1f, (valueCelsius - minTemp) / (maxTemp - minTemp)));
            float sweepAngle = totalSweep * fraction;
            canvas.drawArc(arcBounds, startAngle, sweepAngle, false, arcValuePaint);
        }

        // Draw center value text
        textValPaint.setTextSize(width * 0.22f);
        float centerY = height / 2f - (textValPaint.descent() + textValPaint.ascent()) / 2f;
        String text = Float.isNaN(valueCelsius)
                ? "--"
                : String.format(Locale.US, "%.1f°C", valueCelsius);
        canvas.drawText(text, width / 2f, centerY, textValPaint);
    }
}
