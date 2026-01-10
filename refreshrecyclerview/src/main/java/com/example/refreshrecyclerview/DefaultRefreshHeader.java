package com.example.refreshrecyclerview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 默认的刷新头部视图实现
 */
public class DefaultRefreshHeader implements RefreshHeader {
    
    private LinearLayout mHeaderView;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private TextView mArrowView; // 下拉箭头视图（使用TextView显示箭头符号）
    private Context mContext;
    
    // 动画相关
    private ObjectAnimator mRotationAnimator; // 箭头旋转动画
    private ObjectAnimator mProgressAnimator; // 加载动画
    private boolean mIsRefreshing = false;
    
    public DefaultRefreshHeader(Context context) {
        mContext = context;
        init();
    }
    
    private void init() {
        mHeaderView = new LinearLayout(mContext);
        mHeaderView.setOrientation(LinearLayout.HORIZONTAL);
        mHeaderView.setGravity(Gravity.CENTER);
        mHeaderView.setPadding(0, 40, 0, 40);
        // 设置背景色为黑色
        mHeaderView.setBackgroundColor(0xFF000000); // 黑色背景
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mHeaderView.setLayoutParams(params);
        
        // 创建箭头视图（使用TextView显示箭头符号）
        mArrowView = new TextView(mContext);
        mArrowView.setText("▼"); // 向下箭头符号
        mArrowView.setTextSize(18); // 增大字体大小，更容易看到（28sp）
        mArrowView.setTextColor(0xFFFFFFFF); // 白色箭头（在黑色背景上显示）
        mArrowView.setPadding(0, 0, 20, 0); // 只设置右边距，让箭头紧挨着文字
        mArrowView.setVisibility(View.VISIBLE); // 确保可见
        mArrowView.setGravity(Gravity.CENTER); // 居中显示
        // 不设置箭头背景色，让它在黑色头部背景上显示
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        arrowParams.gravity = Gravity.CENTER_VERTICAL; // 垂直居中对齐
        mArrowView.setLayoutParams(arrowParams);
        
        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleSmall);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.setMargins(0, 0, 20, 0); // 与箭头保持相同的右边距，确保切换时间距一致
        progressParams.gravity = Gravity.CENTER_VERTICAL; // 垂直居中对齐
        mProgressBar.setLayoutParams(progressParams);
        mProgressBar.setVisibility(View.GONE);
        
        mTextView = new TextView(mContext);
        mTextView.setText("下拉刷新");
        mTextView.setTextSize(18); // 增大字体大小，更容易看到（18sp）
        mTextView.setTextColor(0xFFFFFFFF); // 白色文字（在黑色背景上显示）
        mTextView.setVisibility(View.VISIBLE); // 确保可见
        mTextView.setGravity(Gravity.CENTER); // 居中显示
        // 不设置文字背景色，让它在黑色头部背景上显示
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER_VERTICAL; // 垂直居中对齐
        mTextView.setLayoutParams(textParams);
        
        mHeaderView.addView(mArrowView);
        mHeaderView.addView(mProgressBar);
        mHeaderView.addView(mTextView);
    }
    
    @Override
    public View getHeaderView() {
        return mHeaderView;
    }
    
    @Override
    public void onPullDown(float progress) {
        if (mIsRefreshing) {
            return; // 刷新中时不处理下拉
        }
        
        if (mTextView != null) {
            if (progress >= 1.0f) {
                mTextView.setText("释放刷新");
            } else {
                mTextView.setText("下拉刷新");
            }
        }
        
        // 显示箭头，隐藏进度条
        if (mArrowView != null) {
            // 确保箭头可见
            if (mArrowView.getVisibility() != View.VISIBLE) {
                mArrowView.setVisibility(View.VISIBLE);
            }
            // 根据进度改变箭头方向：progress < 1.0 时箭头向下，progress >= 1.0 时箭头向上
            String targetArrowText = progress >= 1.0f ? "▲" : "▼";
            String currentArrowText = mArrowView.getText() != null ? mArrowView.getText().toString() : "";
            // 只有当箭头方向需要改变时才改变文本
            if (!targetArrowText.equals(currentArrowText)) {
                mArrowView.setText(targetArrowText);
                // 添加旋转动画，增加平滑过渡效果
                if (mRotationAnimator != null && mRotationAnimator.isRunning()) {
                    mRotationAnimator.cancel();
                }
                float targetRotation = progress >= 1.0f ? 180f : 0f;
                mRotationAnimator = ObjectAnimator.ofFloat(mArrowView, "rotation", mArrowView.getRotation(), targetRotation);
                mRotationAnimator.setDuration(200);
                mRotationAnimator.start();
            }
            // 根据进度缩放箭头，增加视觉反馈
            float scale = 0.7f + (progress * 0.3f); // 从 0.7 缩放到 1.0
            mArrowView.setScaleX(scale);
            mArrowView.setScaleY(scale);
            // 强制刷新视图
            mArrowView.invalidate();
        }
        
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onReleaseToRefresh() {
        if (mIsRefreshing) {
            return;
        }
        
        if (mTextView != null) {
            mTextView.setText("释放刷新");
        }
        
        // 确保箭头向上
        if (mArrowView != null) {
            mArrowView.setText("▲"); // 向上箭头
            if (mArrowView.getRotation() < 180f) {
                if (mRotationAnimator != null && mRotationAnimator.isRunning()) {
                    mRotationAnimator.cancel();
                }
                mRotationAnimator = ObjectAnimator.ofFloat(mArrowView, "rotation", mArrowView.getRotation(), 180f);
                mRotationAnimator.setDuration(200);
                mRotationAnimator.start();
            }
        }
    }
    
    @Override
    public void onRefreshing() {
        mIsRefreshing = true;
        
        if (mTextView != null) {
            mTextView.setText("正在刷新...");
            // 触发TextView重新测量和布局
            mTextView.invalidate();
            mTextView.requestLayout();
            // 触发头部视图重新测量和布局
            if (mHeaderView != null) {
                mHeaderView.invalidate();
                mHeaderView.requestLayout();
            }
        }
        
        // 隐藏箭头，显示进度条
        if (mArrowView != null) {
            mArrowView.setVisibility(View.GONE);
            // 停止箭头旋转动画
            if (mRotationAnimator != null && mRotationAnimator.isRunning()) {
                mRotationAnimator.cancel();
            }
        }
        
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            // 启动加载动画（如果需要，可以添加旋转动画）
            if (mProgressAnimator == null) {
                // ProgressBar 自带旋转动画，这里可以添加额外的动画效果
                mProgressAnimator = ObjectAnimator.ofFloat(mProgressBar, "rotation", 0f, 360f);
                mProgressAnimator.setDuration(1000);
                mProgressAnimator.setRepeatCount(ValueAnimator.INFINITE);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
            }
            if (!mProgressAnimator.isRunning()) {
                mProgressAnimator.start();
            }
        }
    }
    
    @Override
    public void onRefreshComplete() {
        // 停止加载动画
        if (mProgressAnimator != null && mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
        
        if (mTextView != null) {
            mTextView.setText("刷新完成");
        }
        
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        
        // 短暂显示"刷新完成"后，会在 onIdle 中重置
    }
    
    @Override
    public void onIdle() {
        mIsRefreshing = false;
        
        // 停止所有动画
        if (mRotationAnimator != null && mRotationAnimator.isRunning()) {
            mRotationAnimator.cancel();
        }
        if (mProgressAnimator != null && mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
        
        if (mTextView != null) {
            mTextView.setText("下拉刷新");
        }
        
        // 重置箭头状态
        if (mArrowView != null) {
            mArrowView.setVisibility(View.VISIBLE);
            mArrowView.setText("▼"); // 重置为向下箭头
            mArrowView.setRotation(0f);
            mArrowView.setScaleX(1.0f);
            mArrowView.setScaleY(1.0f);
        }
        
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
