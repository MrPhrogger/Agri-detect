package com.example.agri_detect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GasStripView extends View {

    private long[] timestamps;
    private boolean[] gas;
    private int count = 0;

    private Paint normalPaint;
    private Paint gasPaint;
    private Paint bgPaint;
    private Paint textPaint;

    private RectF rectF;

    public GasStripView(Context context) {
        super(context);
        init();
    }

    public GasStripView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GasStripView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rectF = new RectF();

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFFF3F4F6);
        bgPaint.setStyle(Paint.Style.FILL);

        normalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        normalPaint.setColor(0xFF2E7D6B); // Deep Green
        normalPaint.setStyle(Paint.Style.FILL);

        gasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gasPaint.setColor(0xFFFF5D4D); // Soft Red
        gasPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF9CA3AF);
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(long[] t, boolean[] gas, int size) {
        this.timestamps = t;
        this.gas = gas;
        this.count = size;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        rectF.set(0, 0, width, height);
        canvas.drawRoundRect(rectF, 12f, 12f, bgPaint);

        if (count <= 0 || gas == null || timestamps == null) {
            float centerY = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
            canvas.drawText("No gas data", width / 2f, centerY, textPaint);
            return;
        }

        float segWidth = width / (float) count;
        float gap = Math.min(2f, segWidth * 0.1f);

        for (int i = 0; i < count; i++) {
            float left = i * segWidth + gap / 2f;
            float right = (i + 1) * segWidth - gap / 2f;
            rectF.set(left, 4f, right, height - 4f);
            Paint p = gas[i] ? gasPaint : normalPaint;
            canvas.drawRoundRect(rectF, 4f, 4f, p);
        }
    }
}
