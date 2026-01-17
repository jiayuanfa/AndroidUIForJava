package com.example.bottomnav;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.Animatable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 底部导航栏组件：显示多个 Tab（图标 + 文字）。
 * 新手理解：它是一排按钮，点哪个就选哪个。
 */
public class BottomNavBar extends LinearLayout {
    /** 所有导航项的数据列表 */
    private final List<BottomNavItem> mItems = new ArrayList<>();
    /** 每个导航项对应的 View 缓存 */
    private final List<ViewHolder> mViewHolders = new ArrayList<>();

    /** 当前选中的索引 */
    private int mSelectedIndex = 0;
    /** 选中文字/图标颜色 */
    private int mSelectedColor = Color.parseColor("#1F1F1F");
    /** 未选中文字/图标颜色 */
    private int mUnselectedColor = Color.parseColor("#888888");
    /** 指示条颜色 */
    private int mIndicatorColor = Color.parseColor("#1F1F1F");
    /** 指示条高度（px） */
    private int mIndicatorHeightPx;
    /** 文字大小（sp） */
    private float mTextSizeSp = 12f;
    /** 是否显示指示条 */
    private boolean mIndicatorVisible = true;

    /** 选中监听回调 */
    private OnItemSelectedListener mOnItemSelectedListener;

    /** 代码创建时使用 */
    public BottomNavBar(@NonNull Context context) {
        this(context, null);
    }

    /** XML 创建时会走到这里 */
    public BottomNavBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /** 真正的初始化入口 */
    public BottomNavBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 横向排列 + 居中显示
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.WHITE);
        mIndicatorHeightPx = dpToPx(3);
        setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6));
        if (attrs != null) {
            applyAttributes(context, attrs);
        }
    }

    /** 从 XML 中读取自定义属性 */
    private void applyAttributes(Context context, AttributeSet attrs) {
        android.content.res.TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.BottomNavBar);
        try {
            mIndicatorVisible = a.getBoolean(R.styleable.BottomNavBar_indicatorVisible, mIndicatorVisible);
            mIndicatorHeightPx = a.getDimensionPixelSize(R.styleable.BottomNavBar_indicatorHeight, mIndicatorHeightPx);
            mSelectedColor = a.getColor(R.styleable.BottomNavBar_selectedColor, mSelectedColor);
            mUnselectedColor = a.getColor(R.styleable.BottomNavBar_unselectedColor, mUnselectedColor);
            mIndicatorColor = a.getColor(R.styleable.BottomNavBar_indicatorColor, mIndicatorColor);
            if (a.hasValue(R.styleable.BottomNavBar_textSize)) {
                float px = a.getDimension(R.styleable.BottomNavBar_textSize, spToPx(mTextSizeSp));
                mTextSizeSp = px / getResources().getDisplayMetrics().scaledDensity;
            }
        } finally {
            a.recycle();
        }
    }

    /** 设置导航项列表 */
    public void setItems(@NonNull List<BottomNavItem> items) {
        mItems.clear();
        mItems.addAll(items);
        buildViews();
    }

    /** 设置当前选中项 */
    public void setSelectedIndex(int index) {
        if (index < 0 || index >= mItems.size()) {
            return;
        }
        if (mSelectedIndex == index) {
            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemReselected(index, mItems.get(index));
            }
            if (index < mViewHolders.size()) {
                playIconAnimation(mViewHolders.get(index));
            }
            return;
        }
        mSelectedIndex = index;
        updateSelection();
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onItemSelected(index, mItems.get(index));
        }
    }

    /** 设置选中颜色 */
    public void setSelectedColor(int color) {
        mSelectedColor = color;
        updateSelection();
    }

    /** 设置未选中颜色 */
    public void setUnselectedColor(int color) {
        mUnselectedColor = color;
        updateSelection();
    }

    /** 设置指示条颜色 */
    public void setIndicatorColor(int color) {
        mIndicatorColor = color;
        updateSelection();
    }

    /** 设置指示条高度（dp） */
    public void setIndicatorHeightDp(int dp) {
        mIndicatorHeightPx = dpToPx(dp);
        updateSelection();
    }

    /** 设置是否显示指示条 */
    public void setIndicatorVisible(boolean visible) {
        mIndicatorVisible = visible;
        updateSelection();
    }

    /** 设置文字大小（sp） */
    public void setTextSizeSp(float sizeSp) {
        mTextSizeSp = sizeSp;
        updateSelection();
    }

    /** 设置选中监听 */
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    /** 根据数据创建所有 item 的 View */
    private void buildViews() {
        removeAllViews();
        mViewHolders.clear();
        for (int i = 0; i < mItems.size(); i++) {
            BottomNavItem item = mItems.get(i);
            ViewHolder holder = createItemView(item, i);
            mViewHolders.add(holder);
            addView(holder.root);
        }
        updateSelection();
    }

    /** 创建单个 item 的 View */
    private ViewHolder createItemView(BottomNavItem item, int index) {
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(VERTICAL);
        root.setGravity(Gravity.CENTER);
        LayoutParams rootParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        root.setLayoutParams(rootParams);
        root.setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4));

        ImageView icon = new ImageView(getContext());
        if (item.getIconResId() != 0) {
            icon.setImageResource(item.getIconResId());
            LayoutParams iconParams = new LayoutParams(dpToPx(20), dpToPx(20));
            icon.setLayoutParams(iconParams);
            root.addView(icon);
        }

        TextView label = new TextView(getContext());
        label.setText(item.getTitle());
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSizeSp);
        LayoutParams textParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dpToPx(4);
        label.setLayoutParams(textParams);
        root.addView(label);

        View indicator = new View(getContext());
        LayoutParams indicatorParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                mIndicatorHeightPx
        );
        indicatorParams.topMargin = dpToPx(4);
        indicator.setLayoutParams(indicatorParams);
        root.addView(indicator);

        root.setOnClickListener(v -> setSelectedIndex(index));

        return new ViewHolder(root, icon, label, indicator);
    }

    /** 更新选中状态：颜色、指示条、动画 */
    private void updateSelection() {
        for (int i = 0; i < mViewHolders.size(); i++) {
            ViewHolder holder = mViewHolders.get(i);
            boolean selected = i == mSelectedIndex;
            int color = selected ? mSelectedColor : mUnselectedColor;
            holder.label.setTextColor(color);
            holder.label.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSizeSp);
            if (holder.icon != null) {
                holder.icon.setColorFilter(color);
            }
            ViewGroup.LayoutParams params = holder.indicator.getLayoutParams();
            params.height = mIndicatorVisible ? mIndicatorHeightPx : 0;
            holder.indicator.setLayoutParams(params);
            if (mIndicatorVisible) {
                holder.indicator.setBackgroundColor(selected ? mIndicatorColor : Color.TRANSPARENT);
            } else {
                holder.indicator.setBackgroundColor(Color.TRANSPARENT);
            }
            if (selected) {
                playIconAnimation(holder);
            }
        }
    }

    /** 播放图标动画（如果是 AnimatedVectorDrawable） */
    private void playIconAnimation(ViewHolder holder) {
        if (holder.icon == null) {
            return;
        }
        if (holder.icon.getDrawable() instanceof Animatable) {
            ((Animatable) holder.icon.getDrawable()).start();
        }
    }

    /** dp 转 px */
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    /** sp 转 px */
    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    /** 选中回调接口 */
    public interface OnItemSelectedListener {
        void onItemSelected(int index, BottomNavItem item);

        void onItemReselected(int index, BottomNavItem item);
    }

    /** 每个 item 的 View 缓存 */
    private static class ViewHolder {
        final LinearLayout root;
        final ImageView icon;
        final TextView label;
        final View indicator;

        ViewHolder(LinearLayout root, ImageView icon, TextView label, View indicator) {
            this.root = root;
            this.icon = icon;
            this.label = label;
            this.indicator = indicator;
        }
    }
}
