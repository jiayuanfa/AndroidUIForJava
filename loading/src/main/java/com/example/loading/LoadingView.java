package com.example.loading;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LoadingView extends FrameLayout {
    private static final String TAG = "LoadingView";
    private ProgressBar progressBar;
    private TextView textView;
    private LinearLayout loadingContainer;
    private boolean isSizeCalculated = false;

    public LoadingView(@NonNull Context context) {
        this(context, null);
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Log.d(TAG, "[创建] init() - 初始化LoadingView");
        LayoutInflater.from(getContext()).inflate(R.layout.loading_view, this, true);
        progressBar = findViewById(R.id.progress_bar);
        textView = findViewById(R.id.tv_loading_text);
        loadingContainer = findViewById(R.id.loading_container);
        Log.d(TAG, "[创建] init() - LoadingView初始化完成");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "[尺寸] onSizeChanged() - 尺寸变化: " + oldw + "x" + oldh + " -> " + w + "x" + h);
        if (w > 0 && h > 0) {
            // 每次尺寸变化时都重新计算，确保尺寸一致
            calculateAndSetSizes(w, h);
            isSizeCalculated = true;
            Log.d(TAG, "[尺寸] onSizeChanged() - 尺寸计算完成");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "[窗口] onAttachedToWindow() - LoadingView已添加到窗口");
        // 每次添加到窗口后都重新计算尺寸，确保尺寸一致
        // 使用post确保在布局完成后才计算尺寸
        post(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                Log.d(TAG, "[窗口] onAttachedToWindow() - 首次尺寸计算: " + getWidth() + "x" + getHeight());
                calculateAndSetSizes(getWidth(), getHeight());
                isSizeCalculated = true;
            } else {
                // 如果尺寸还是0，再延迟一次
                post(() -> {
                    if (getWidth() > 0 && getHeight() > 0) {
                        Log.d(TAG, "[窗口] onAttachedToWindow() - 延迟尺寸计算: " + getWidth() + "x" + getHeight());
                        calculateAndSetSizes(getWidth(), getHeight());
                        isSizeCalculated = true;
                    }
                });
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "[窗口] onDetachedFromWindow() - LoadingView已从窗口移除");
    }

    /**
     * 根据父容器大小计算并设置loading容器和动画的尺寸
     * 规则：
     * 1. 遮罩层全覆盖（已通过match_parent实现）
     * 2. 黑色loading背景：宽高相同，宽度为持有者的1/3
     * 3. 内部loading动画：大小为黑色loading背景的1/2
     */
    private void calculateAndSetSizes(int parentWidth, int parentHeight) {
        if (loadingContainer == null || progressBar == null) {
            Log.w(TAG, "[尺寸] calculateAndSetSizes() - loadingContainer或progressBar为null");
            return;
        }

        Log.d(TAG, "[尺寸] calculateAndSetSizes() - 开始计算尺寸, 父容器: " + parentWidth + "x" + parentHeight);

        // 计算loading容器的尺寸：宽度为父容器宽度的1/3，宽高相同（正方形）
        int containerSize = parentWidth / 3;
        
        // 确保最小尺寸
        containerSize = Math.max(containerSize, 80);
        Log.d(TAG, "[尺寸] calculateAndSetSizes() - 容器尺寸: " + containerSize + "x" + containerSize);
        
        // 设置loading容器的尺寸（强制设置，确保每次都是相同尺寸）
        ViewGroup.LayoutParams containerParams = loadingContainer.getLayoutParams();
        if (containerParams == null) {
            containerParams = new FrameLayout.LayoutParams(containerSize, containerSize);
        } else {
            containerParams.width = containerSize;
            containerParams.height = containerSize;
        }
        loadingContainer.setLayoutParams(containerParams);
        
        // 计算ProgressBar的尺寸：为容器尺寸的1/2
        int progressBarSize = containerSize / 2;
        // 确保最小尺寸
        progressBarSize = Math.max(progressBarSize, 24);
        Log.d(TAG, "[尺寸] calculateAndSetSizes() - ProgressBar尺寸: " + progressBarSize + "x" + progressBarSize);
        
        // 设置ProgressBar的尺寸（强制设置，确保每次都是相同尺寸）
        ViewGroup.LayoutParams progressParams = progressBar.getLayoutParams();
        if (progressParams == null) {
            progressParams = new LinearLayout.LayoutParams(progressBarSize, progressBarSize);
        } else {
            progressParams.width = progressBarSize;
            progressParams.height = progressBarSize;
        }
        progressBar.setLayoutParams(progressParams);
        
        // 根据容器大小调整padding
        int padding = containerSize / 8;
        padding = Math.max(padding, 16);
        loadingContainer.setPadding(padding, padding, padding, padding);
        Log.d(TAG, "[尺寸] calculateAndSetSizes() - Padding: " + padding);
        
        // 强制请求布局，确保尺寸生效
        loadingContainer.requestLayout();
        progressBar.requestLayout();
        Log.d(TAG, "[尺寸] calculateAndSetSizes() - 尺寸计算完成并请求布局");
    }

    public void setLoadingText(String text) {
        if (text != null && !text.isEmpty()) {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    public void setLoadingText(int resId) {
        if (resId != 0) {
            textView.setText(resId);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * 重置尺寸计算标志，用于重新计算尺寸
     */
    public void resetSizeCalculation() {
        Log.d(TAG, "[尺寸] resetSizeCalculation() - 重置尺寸计算标志");
        isSizeCalculated = false;
    }

    /**
     * 强制重新计算尺寸（在添加到父容器后调用）
     */
    public void forceRecalculateSize() {
        Log.d(TAG, "[尺寸] forceRecalculateSize() - 强制重新计算尺寸");
        isSizeCalculated = false;
        if (getWidth() > 0 && getHeight() > 0) {
            Log.d(TAG, "[尺寸] forceRecalculateSize() - 立即计算: " + getWidth() + "x" + getHeight());
            calculateAndSetSizes(getWidth(), getHeight());
            isSizeCalculated = true;
        } else {
            // 如果尺寸还没确定，等待布局完成
            Log.d(TAG, "[尺寸] forceRecalculateSize() - 尺寸未确定，等待布局完成");
            post(() -> {
                if (getWidth() > 0 && getHeight() > 0) {
                    Log.d(TAG, "[尺寸] forceRecalculateSize() - 延迟计算: " + getWidth() + "x" + getHeight());
                    calculateAndSetSizes(getWidth(), getHeight());
                    isSizeCalculated = true;
                } else {
                    // 再延迟一次
                    post(() -> {
                        if (getWidth() > 0 && getHeight() > 0) {
                            Log.d(TAG, "[尺寸] forceRecalculateSize() - 二次延迟计算: " + getWidth() + "x" + getHeight());
                            calculateAndSetSizes(getWidth(), getHeight());
                            isSizeCalculated = true;
                        }
                    });
                }
            });
        }
    }
}

