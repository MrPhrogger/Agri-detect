package com.example.agri_detect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LineChartView extends View {

    private long[] timestamps;
    private float[] values;
    private int count = 0;
    private int lineColor = 0xFF2E7D6B;
    private String unit = "";
    private float yPadding = 4f;

    private Paint linePaint;
    private Paint fillPaint;
    private Paint gridPaint;
    private Paint textPaint;

    private Path linePath;
    private Path fillPath;

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(0xFFE5E7EB);
        gridPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF9CA3AF);
        textPaint.setTextSize(28f);

        linePath = new Path();
        fillPath = new Path();
    }

    public void setData(long[] t, float[] values, int size, int color, String unit, float yPadding, boolean animate) {
        this.timestamps = t;
        this.values = values;
        this.count = size;
        this.lineColor = color;
        this.unit = unit;
        this.yPadding = yPadding;

        linePaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        if (count <= 0 || values == null || timestamps == null) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No data available", width / 2f, height / 2f, textPaint);
            return;
        }

        float minVal = Float.MAX_VALUE;
        float maxVal = -Float.MAX_VALUE;
        int validCount = 0;

        for (int i = 0; i < count; i++) {
            if (!Float.isNaN(values[i])) {
                if (values[i] < minVal) minVal = values[i];
                if (values[i] > maxVal) maxVal = values[i];
                validCount++;
            }
        }

        if (validCount == 0) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No valid data points", width / 2f, height / 2f, textPaint);
            return;
        }

        if (minVal == maxVal) {
            minVal -= yPadding;
            maxVal += yPadding;
        } else {
            minVal -= yPadding;
            maxVal += yPadding;
        }

        float paddingLeft = 30f;
        float paddingRight = 30f;
        float paddingTop = 30f;
        float paddingBottom = 30f;

        float chartW = width - paddingLeft - paddingRight;
        float chartH = height - paddingTop - paddingBottom;

        linePath.reset();
        fillPath.reset();

        boolean firstPoint = true;
        float lastX = 0f;
        float lastY = 0f;

        for (int i = 0; i < count; i++) {
            if (Float.isNaN(values[i])) continue;

            float x = paddingLeft + (chartW * i / (float) Math.max(1, count - 1));
            float fraction = (values[i] - minVal) / (maxVal - minVal);
            float y = paddingTop + chartH * (1f - fraction);

            if (firstPoint) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, height - paddingBottom);
                fillPath.lineTo(x, y);
                firstPoint = false;
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
            lastX = x;
            lastY = y;
        }

        if (!firstPoint) {
            fillPath.lineTo(lastX, height - paddingBottom);
            fillPath.close();

            int alphaColor = (lineColor & 0x00FFFFFF) | 0x30000000;
            fillPaint.setShader(new LinearGradient(0, paddingTop, 0, height - paddingBottom,
                    alphaColor, 0x00FFFFFF, Shader.TileMode.CLAMP));

            canvas.drawPath(fillPath, fillPaint);
            canvas.drawPath(linePath, linePaint);
        }
    }
}
