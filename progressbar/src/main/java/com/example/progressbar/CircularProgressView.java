package com.example.progressbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 环形进度条View
 * 使用Canvas绘制圆形进度条
 */
public class CircularProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private float progress = 0f;
    private float strokeWidth = 20f;
    private int backgroundColor = 0xFFE0E0E0;
    private int progressColor = 0xFF2196F3;
    private RectF rectF;

    public CircularProgressView(@NonNull Context context) {
        this(context, null);
    }

    public CircularProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 背景画笔
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        // 进度画笔
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        rectF = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算绘制区域，留出strokeWidth的空间
        float padding = strokeWidth / 2;
        rectF.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rectF.width() <= 0 || rectF.height() <= 0) {
            return;
        }

        // 绘制背景圆环
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        // 绘制进度弧（从-90度开始，顺时针）
        float sweepAngle = progress * 360;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    /**
     * 设置进度值（0-1）
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    /**
     * 获取进度值
     */
    public float getProgress() {
        return progress;
    }

    /**
     * 设置线条宽度
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        backgroundPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeWidth(strokeWidth);
        // 重新计算绘制区域
        if (getWidth() > 0 && getHeight() > 0) {
            float padding = strokeWidth / 2;
            rectF.set(padding, padding, getWidth() - padding, getHeight() - padding);
        }
        invalidate();
    }

    /**
     * 获取线条宽度
     */
    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * 设置背景颜色
     */
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    /**
     * 设置进度颜色
     */
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }
}

