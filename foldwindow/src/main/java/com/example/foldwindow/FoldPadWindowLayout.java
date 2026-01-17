package com.example.foldwindow;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 折叠屏/Pad 小窗组件的主容器。
 * 目标：像搭积木一样，孩子也能理解。
 */
public class FoldPadWindowLayout extends FrameLayout {
    /** 侧边栏默认宽度：屏幕宽度的 30% */
    private static final float DEFAULT_SIDEBAR_WIDTH_PERCENT = 0.30f;
    /** 侧边栏最小宽度比例：不允许太窄 */
    private static final float DEFAULT_MIN_SIDEBAR_WIDTH_PERCENT = 0.25f;
    /** 侧边栏最大宽度比例：不允许太宽 */
    private static final float DEFAULT_MAX_SIDEBAR_WIDTH_PERCENT = 0.35f;
    /** 小窗高度比例：小窗高度 = 屏幕高度 * 0.60 */
    private static final float DEFAULT_MINI_HEIGHT_PERCENT = 0.60f;
    /** 动画时间：越大越慢，越小越快 */
    private static final int DEFAULT_ANIM_DURATION_MS = 220;

    /** 主内容外壳：负责全屏/小窗大小、位置 */
    private final FrameLayout mContentHost;
    /** 真正的内容容器：外部内容会放进这里 */
    private final FrameLayout mContentContainer;
    /** 左侧菜单栏容器 */
    private final FrameLayout mSidebar;
    /** 菜单栏右侧的拖拽条 */
    private final View mSidebarHandle;
    /** 顶部工具栏区域 */
    private final FrameLayout mTopBar;
    /** 顶部按钮组容器 */
    private final LinearLayout mControlBar;
    /** 菜单按钮 */
    private final TextView mMenuButton;
    /** 小窗按钮 */
    private final TextView mMiniButton;
    /** 全屏按钮 */
    private final TextView mFullButton;

    /** 是否处于小窗状态 */
    private boolean mIsMiniMode = false;
    /** 侧边栏是否展开 */
    private boolean mIsSidebarOpen = false;
    /** 侧边栏当前宽度比例 */
    private float mSidebarWidthPercent = DEFAULT_SIDEBAR_WIDTH_PERCENT;
    /** 侧边栏最小比例 */
    private float mMinSidebarWidthPercent = DEFAULT_MIN_SIDEBAR_WIDTH_PERCENT;
    /** 侧边栏最大比例 */
    private float mMaxSidebarWidthPercent = DEFAULT_MAX_SIDEBAR_WIDTH_PERCENT;
    /** 小窗高度比例 */
    private float mMiniHeightPercent = DEFAULT_MINI_HEIGHT_PERCENT;
    /** 动画时长 */
    private int mAnimDurationMs = DEFAULT_ANIM_DURATION_MS;
    /** 手指滑动阈值：小于这个就不当作拖拽 */
    private int mTouchSlop;
    /** 小窗是否正在拖拽 */
    private boolean mDraggingMini = false;
    /** 手指上一次的屏幕坐标 X */
    private float mLastTouchRawX;
    /** 手指上一次的屏幕坐标 Y */
    private float mLastTouchRawY;
    /** 拖拽开始时小窗的 X */
    private float mStartDragX;
    /** 拖拽开始时小窗的 Y */
    private float mStartDragY;
    /** 全屏/小窗切换动画 */
    private ValueAnimator mModeAnimator;
    /** 小窗圆角半径 */
    private float mMiniCornerRadius;

    /** 代码里直接 new 的入口 */
    public FoldPadWindowLayout(@NonNull Context context) {
        this(context, null);
    }

    /** XML 使用时会走到这里 */
    public FoldPadWindowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /** 真正的初始化入口 */
    public FoldPadWindowLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setClipChildren(false);
        mMiniCornerRadius = dpToPx(16);

        // 主内容外壳：它负责大小/位置变化
        mContentHost = new FrameLayout(context);
        mContentHost.setClipChildren(false);
        mContentHost.setBackgroundColor(Color.parseColor("#1F1F1F"));
        mContentHost.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        mContentHost.setClipToOutline(false);
        LayoutParams contentParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        addView(mContentHost, contentParams);

        // 内容容器：外部业务内容放进这里
        mContentContainer = new FrameLayout(context);
        mContentContainer.setBackgroundColor(Color.parseColor("#1F1F1F"));
        mContentHost.addView(mContentContainer, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // 顶部按钮栏
        mTopBar = new FrameLayout(context);
        LayoutParams topBarParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(140)
        );
        mContentHost.addView(mTopBar, topBarParams);

        // 按钮组（MENU / MINI / FULL）
        mControlBar = new LinearLayout(context);
        mControlBar.setOrientation(LinearLayout.HORIZONTAL);
        mControlBar.setGravity(android.view.Gravity.CENTER);
        FrameLayout.LayoutParams controlParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        controlParams.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;
        controlParams.topMargin = dpToPx(50);
        mTopBar.addView(mControlBar, controlParams);

        // 菜单按钮
        mMenuButton = buildActionButton(context, "MENU");
        mMenuButton.setContentDescription("打开或关闭菜单");
        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        menuParams.rightMargin = dpToPx(12);
        mControlBar.addView(mMenuButton, menuParams);

        // 小窗按钮
        mMiniButton = buildActionButton(context, "MINI");
        mMiniButton.setContentDescription("切换为小窗");
        LinearLayout.LayoutParams miniParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        miniParams.rightMargin = dpToPx(12);
        mControlBar.addView(mMiniButton, miniParams);

        // 全屏按钮
        mFullButton = buildActionButton(context, "FULL");
        mFullButton.setContentDescription("恢复为全屏");
        LinearLayout.LayoutParams fullParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mControlBar.addView(mFullButton, fullParams);
        mFullButton.setVisibility(GONE);

        // 左侧侧边栏
        mSidebar = new FrameLayout(context);
        mSidebar.setBackgroundColor(Color.parseColor("#F2F2F2"));
        mSidebar.setElevation(dpToPx(6));
        LayoutParams sidebarParams = new LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        addView(mSidebar, sidebarParams);

        // 默认的菜单内容占位（可被 setSidebarContent 替换）
        LinearLayout sidebarContent = new LinearLayout(context);
        sidebarContent.setOrientation(LinearLayout.VERTICAL);
        sidebarContent.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(24));
        mSidebar.addView(sidebarContent, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        sidebarContent.addView(buildSidebarItem(context, "Menu 1"));
        sidebarContent.addView(buildSidebarItem(context, "Menu 2"));
        sidebarContent.addView(buildSidebarItem(context, "Menu 3"));

        // 侧边栏拖拽条
        mSidebarHandle = new View(context);
        mSidebarHandle.setBackgroundColor(Color.parseColor("#DDDDDD"));
        mSidebarHandle.setContentDescription("拖拽调整菜单宽度");
        FrameLayout.LayoutParams handleParams = new FrameLayout.LayoutParams(
                dpToPx(8),
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        handleParams.gravity = android.view.Gravity.END;
        mSidebar.addView(mSidebarHandle, handleParams);

        // 绑定点击/拖拽逻辑
        setupInteractions();
    }

    @Override
    public boolean performClick() {
        // 让无障碍服务知道这里“被点击了”
        return super.performClick();
    }

    /** 放入你的主要内容（右侧内容区） */
    public void setContentView(@NonNull View view) {
        mContentContainer.removeAllViews();
        mContentContainer.addView(view, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    /** 设置小窗圆角（dp） */
    public void setMiniCornerRadiusDp(float radiusDp) {
        mMiniCornerRadius = dpToPx(Math.round(radiusDp));
        if (mIsMiniMode) {
            enableMiniCorner(true);
        }
    }

    /** 设置小窗圆角（px） */
    public void setMiniCornerRadiusPx(float radiusPx) {
        mMiniCornerRadius = Math.max(0f, radiusPx);
        if (mIsMiniMode) {
            enableMiniCorner(true);
        }
    }

    /** 设置所有动画时长（毫秒） */
    public void setAnimationDurationMs(int durationMs) {
        mAnimDurationMs = Math.max(0, durationMs);
    }

    /** 设置侧边栏宽度比例 */
    public void setSidebarWidthPercent(float percent) {
        mSidebarWidthPercent = clamp(percent, mMinSidebarWidthPercent, mMaxSidebarWidthPercent);
        updateSidebarWidth(false);
        applySidebarEffect(false);
    }

    /** 设置侧边栏宽度可拖拽范围 */
    public void setSidebarWidthRangePercent(float minPercent, float maxPercent) {
        float min = Math.max(0f, minPercent);
        float max = Math.max(min, maxPercent);
        mMinSidebarWidthPercent = min;
        mMaxSidebarWidthPercent = max;
        mSidebarWidthPercent = clamp(mSidebarWidthPercent, mMinSidebarWidthPercent, mMaxSidebarWidthPercent);
        updateSidebarWidth(false);
        applySidebarEffect(false);
    }

    /** 设置小窗高度比例（建议 0.3 ~ 0.9） */
    public void setMiniHeightPercent(float percent) {
        mMiniHeightPercent = clamp(percent, 0.3f, 0.9f);
    }

    /** 放入你的菜单内容（左侧菜单区） */
    public void setSidebarContent(@NonNull View view) {
        mSidebar.removeAllViews();
        mSidebar.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        FrameLayout.LayoutParams handleParams = new FrameLayout.LayoutParams(
                dpToPx(8),
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        handleParams.gravity = android.view.Gravity.END;
        mSidebar.addView(mSidebarHandle, handleParams);
    }

    /** 绑定按钮点击、拖拽逻辑 */
    private void setupInteractions() {
        mMenuButton.setOnClickListener(v -> toggleSidebar());
        mMiniButton.setOnClickListener(v -> enterMiniMode(true));
        mFullButton.setOnClickListener(v -> exitMiniMode(true));

        // 小窗拖拽：只有在小窗模式下允许移动
        mContentHost.setOnTouchListener((v, event) -> {
            if (!mIsMiniMode) {
                return false;
            }
            if (isTouchOnView(event, mMenuButton) || isTouchOnView(event, mMiniButton) || isTouchOnView(event, mFullButton)) {
                return false;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mDraggingMini = false;
                    mLastTouchRawX = event.getRawX();
                    mLastTouchRawY = event.getRawY();
                    mStartDragX = mContentHost.getX();
                    mStartDragY = mContentHost.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - mLastTouchRawX;
                    float dy = event.getRawY() - mLastTouchRawY;
                    if (!mDraggingMini && Math.hypot(dx, dy) < mTouchSlop) {
                        return true;
                    }
                    mDraggingMini = true;
                    float targetX = mStartDragX + (event.getRawX() - mLastTouchRawX);
                    float targetY = mStartDragY + (event.getRawY() - mLastTouchRawY);
                    Rect bounds = getMiniBounds();
                    targetX = clamp(targetX, bounds.left, bounds.right);
                    targetY = clamp(targetY, bounds.top, bounds.bottom);
                    mContentHost.setX(targetX);
                    mContentHost.setY(targetY);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mDraggingMini = false;
                    v.performClick();
                    return true;
                default:
                    return false;
            }
        });

        // 菜单拖拽条：改变菜单宽度比例
        mSidebarHandle.setOnTouchListener((v, event) -> {
            if (!mIsSidebarOpen) {
                return false;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int parentLeft = getLocationOnScreenX(this);
                    float rawX = event.getRawX();
                    float newWidth = rawX - parentLeft;
                    float parentWidth = getWidth();
                    if (parentWidth <= 0) {
                        return true;
                    }
                    float percent = newWidth / parentWidth;
                    mSidebarWidthPercent = clamp(percent, mMinSidebarWidthPercent, mMaxSidebarWidthPercent);
                    updateSidebarWidth(false);
                    applySidebarEffect(false);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.performClick();
                    return true;
                default:
                    return false;
            }
        });
    }

    /** 切换菜单：开/关 */
    private void toggleSidebar() {
        if (mIsSidebarOpen) {
            closeSidebar(true);
        } else {
            openSidebar(true);
        }
    }

    /** 打开菜单 */
    private void openSidebar(boolean animate) {
        mIsSidebarOpen = true;
        updateSidebarWidth(false);
        applySidebarEffect(animate);
        if (animate) {
            mSidebar.animate()
                    .translationX(0f)
                    .setDuration(mAnimDurationMs)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            mSidebar.setTranslationX(0f);
        }
    }

    /** 关闭菜单 */
    private void closeSidebar(boolean animate) {
        mIsSidebarOpen = false;
        int width = mSidebar.getLayoutParams().width;
        applySidebarEffect(animate);
        if (animate) {
            mSidebar.animate()
                    .translationX(-width)
                    .setDuration(mAnimDurationMs)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            mSidebar.setTranslationX(-width);
        }
    }

    /** 更新菜单宽度（不改动菜单开关状态） */
    private void updateSidebarWidth(boolean keepOpenState) {
        int parentWidth = getWidth();
        if (parentWidth <= 0) {
            return;
        }
        int targetWidth = Math.round(parentWidth * mSidebarWidthPercent);
        ViewGroup.LayoutParams params = mSidebar.getLayoutParams();
        params.width = targetWidth;
        mSidebar.setLayoutParams(params);
        if (mIsSidebarOpen || keepOpenState) {
            mSidebar.setTranslationX(0f);
        } else {
            mSidebar.setTranslationX(-targetWidth);
        }
    }

    /** 菜单展开时挤压右侧内容 */
    private void applySidebarEffect(boolean animate) {
        int parentWidth = getWidth();
        if (parentWidth <= 0) {
            return;
        }
        if (mIsMiniMode) {
            return;
        }
        int sidebarWidth = Math.round(parentWidth * mSidebarWidthPercent);
        int targetWidth = mIsSidebarOpen ? parentWidth - sidebarWidth : parentWidth;
        int targetHeight = getHeight();
        float targetX = mIsSidebarOpen ? sidebarWidth : 0f;
        if (!animate) {
            ViewGroup.LayoutParams params = mContentHost.getLayoutParams();
            params.width = targetWidth;
            params.height = targetHeight;
            mContentHost.setLayoutParams(params);
            mContentHost.setX(targetX);
            mContentHost.setY(0f);
            mContentHost.requestLayout();
            return;
        }
        int startWidth = mContentHost.getWidth() > 0 ? mContentHost.getWidth() : parentWidth;
        int startHeight = mContentHost.getHeight() > 0 ? mContentHost.getHeight() : targetHeight;
        float startX = mContentHost.getX();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(mAnimDurationMs);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int width = lerp(startWidth, targetWidth, fraction);
            int height = lerp(startHeight, targetHeight, fraction);
            float x = lerp(startX, targetX, fraction);
            ViewGroup.LayoutParams params = mContentHost.getLayoutParams();
            params.width = width;
            params.height = height;
            mContentHost.setLayoutParams(params);
            mContentHost.setX(x);
            mContentHost.setY(0f);
            mContentHost.requestLayout();
        });
        animator.start();
    }

    /** 切换到小窗模式 */
    private void enterMiniMode(boolean animate) {
        if (mIsMiniMode) {
            return;
        }
        mIsMiniMode = true;
        mMenuButton.setVisibility(GONE);
        mMiniButton.setVisibility(GONE);
        mFullButton.setVisibility(VISIBLE);
        if (mIsSidebarOpen) {
            closeSidebar(animate);
        }
        mContentHost.setElevation(dpToPx(10));
        enableMiniCorner(true);
        applySidebarEffect(false);
        Rect start = getCurrentContentRect();
        Rect end = getTargetMiniRect();
        animateContentRect(start, end, animate);
    }

    /** 切回全屏模式 */
    private void exitMiniMode(boolean animate) {
        if (!mIsMiniMode) {
            return;
        }
        mIsMiniMode = false;
        mMenuButton.setVisibility(VISIBLE);
        mMiniButton.setVisibility(VISIBLE);
        mFullButton.setVisibility(GONE);
        Rect start = getCurrentContentRect();
        Rect end = getTargetFullRect();
        animateContentRect(start, end, animate);
        mContentHost.setElevation(0f);
        enableMiniCorner(false);
        applySidebarEffect(false);
    }

    /** 用动画把内容区域从 start 移动到 end */
    private void animateContentRect(Rect start, Rect end, boolean animate) {
        if (!animate) {
            applyContentRect(end);
            return;
        }
        if (mModeAnimator != null) {
            mModeAnimator.cancel();
        }
        mModeAnimator = ValueAnimator.ofFloat(0f, 1f);
        mModeAnimator.setDuration(mAnimDurationMs);
        mModeAnimator.setInterpolator(new DecelerateInterpolator());
        mModeAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int width = lerp(start.width(), end.width(), fraction);
            int height = lerp(start.height(), end.height(), fraction);
            float x = lerp(start.left, end.left, fraction);
            float y = lerp(start.top, end.top, fraction);
            applyContentRect(new Rect(Math.round(x), Math.round(y), Math.round(x) + width, Math.round(y) + height));
        });
        mModeAnimator.start();
    }

    /** 立即应用内容区域的位置和大小 */
    private void applyContentRect(Rect rect) {
        ViewGroup.LayoutParams params = mContentHost.getLayoutParams();
        params.width = rect.width();
        params.height = rect.height();
        mContentHost.setLayoutParams(params);
        mContentHost.setX(rect.left);
        mContentHost.setY(rect.top);
    }

    /** 读取当前内容区域（位置 + 大小） */
    private Rect getCurrentContentRect() {
        int width = mContentHost.getWidth() > 0 ? mContentHost.getWidth() : getWidth();
        int height = mContentHost.getHeight() > 0 ? mContentHost.getHeight() : getHeight();
        int left = Math.round(mContentHost.getX());
        int top = Math.round(mContentHost.getY());
        return new Rect(left, top, left + width, top + height);
    }

    /** 计算小窗的目标矩形（16:9，居中） */
    private Rect getTargetMiniRect() {
        int parentWidth = getWidth();
        int parentHeight = getHeight();
        int margin = dpToPx(16);
        int targetHeight = Math.round(parentHeight * mMiniHeightPercent);
        int maxHeight = parentHeight - margin * 2;
        int minHeight = dpToPx(240);
        targetHeight = clamp(targetHeight, minHeight, maxHeight);
        int targetWidth = Math.round(targetHeight * 9f / 16f);
        int maxWidth = parentWidth - margin * 2;
        if (targetWidth > maxWidth) {
            targetWidth = maxWidth;
            targetHeight = Math.round(targetWidth * 16f / 9f);
        }
        int left = (parentWidth - targetWidth) / 2;
        int top = (parentHeight - targetHeight) / 2;
        left = clamp(left, margin, parentWidth - targetWidth - margin);
        top = clamp(top, margin, parentHeight - targetHeight - margin);
        return new Rect(left, top, left + targetWidth, top + targetHeight);
    }

    /** 计算全屏的目标矩形（考虑菜单挤压） */
    private Rect getTargetFullRect() {
        int parentWidth = getWidth();
        int parentHeight = getHeight();
        if (!mIsSidebarOpen) {
            return new Rect(0, 0, parentWidth, parentHeight);
        }
        int sidebarWidth = Math.round(parentWidth * mSidebarWidthPercent);
        return new Rect(sidebarWidth, 0, sidebarWidth + (parentWidth - sidebarWidth), parentHeight);
    }

    /** 小窗拖拽的边界范围 */
    private Rect getMiniBounds() {
        int parentWidth = getWidth();
        int parentHeight = getHeight();
        int margin = dpToPx(8);
        int width = mContentHost.getWidth();
        int height = mContentHost.getHeight();
        return new Rect(
                margin,
                margin,
                parentWidth - width - margin,
                parentHeight - height - margin
        );
    }

    /** 创建顶部按钮样式 */
    private TextView buildActionButton(Context context, String text) {
        TextView button = new TextView(context);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        button.setBackgroundColor(Color.parseColor("#222222"));
        button.setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6));
        return button;
    }

    /** 创建一个默认菜单项 */
    private TextView buildSidebarItem(Context context, String text) {
        TextView item = new TextView(context);
        item.setText(text);
        item.setTextColor(Color.parseColor("#333333"));
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(12);
        item.setLayoutParams(params);
        return item;
    }

    /** 判断手指是否点到了某个 View */
    private boolean isTouchOnView(MotionEvent event, View view) {
        if (view.getVisibility() != VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        return rawX >= location[0]
                && rawX <= location[0] + view.getWidth()
                && rawY >= location[1]
                && rawY <= location[1] + view.getHeight();
    }

    /** dp 转 px：让不同屏幕尺寸看起来一致 */
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    /** 获取 View 在屏幕上的 X 坐标 */
    private int getLocationOnScreenX(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0];
    }

    /** 数值插值：让动画更平滑 */
    private int lerp(int start, int end, float fraction) {
        return Math.round(start + (end - start) * fraction);
    }

    /** 浮点插值：让动画更平滑 */
    private float lerp(float start, float end, float fraction) {
        return start + (end - start) * fraction;
    }

    /** 把数值限制在 min~max 之间 */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** 把小数限制在 min~max 之间 */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    /** 尺寸改变时（比如旋转屏幕）重新计算布局 */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateSidebarWidth(false);
        if (mIsMiniMode) {
            applyContentRect(getTargetMiniRect());
        } else {
            applyContentRect(new Rect(0, 0, w, h));
        }
        enableMiniCorner(mIsMiniMode);
        applySidebarEffect(false);
    }

    /** 开关小窗圆角（使用系统裁剪，性能更好） */
    private void enableMiniCorner(boolean enable) {
        if (!enable) {
            mContentHost.setClipToOutline(false);
            mContentHost.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            return;
        }
        mContentHost.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();
                if (width <= 0 || height <= 0) {
                    return;
                }
                outline.setRoundRect(0, 0, width, height, mMiniCornerRadius);
            }
        });
        mContentHost.setClipToOutline(true);
    }
}
