package com.example.progressbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeProviderCompat;

import com.example.progressbar.R;

/**
 * 自定义进度条组件
 * 
 * <p><b>功能特性：</b></p>
 * <ul>
 *   <li>支持线性和环形两种显示模式</li>
 *   <li>进度值范围：0-1（0表示0%，1表示100%）</li>
 *   <li>支持平滑动画效果，可自定义动画时长</li>
 *   <li>支持从XML设置初始进度和模式</li>
 *   <li>支持代码中动态修改进度</li>
 *   <li>默认进度：25%（0.25）</li>
 * </ul>
 * 
 * <p><b>实现原理：</b></p>
 * <ol>
 *   <li><b>双模式实现：</b>通过显示/隐藏不同的View实现线性和环形模式的切换
 *       <ul>
 *         <li>线性模式：使用两个View（背景View和前景View），通过动态修改前景View的宽度来显示进度</li>
 *         <li>环形模式：使用CircularProgressView自定义View，通过Canvas绘制圆形进度条</li>
 *       </ul>
 *   </li>
 *   <li><b>动画实现：</b>使用ValueAnimator实现平滑的进度变化动画
 *       <ul>
 *         <li>通过ValueAnimator.ofFloat()创建浮点数动画</li>
 *         <li>使用AccelerateDecelerateInterpolator实现先加速后减速的动画效果</li>
 *         <li>在动画更新回调中实时更新进度值并刷新UI</li>
 *       </ul>
 *   </li>
 *   <li><b>进度更新机制：</b>
 *       <ul>
 *         <li>线性模式：通过修改前景View的LayoutParams.width来改变宽度</li>
 *         <li>环形模式：调用CircularProgressView.setProgress()触发invalidate()重绘</li>
 *       </ul>
 *   </li>
 *   <li><b>XML属性支持：</b>通过TypedArray读取自定义属性，支持在XML中设置初始值</li>
 * </ol>
 * 
 * <p><b>使用示例：</b></p>
 * <pre>
 * // XML中使用
 * &lt;com.example.progressbar.CustomProgressBar
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:progress="0.5"
 *     app:progressMode="linear" /&gt;
 * 
 * // 代码中使用
 * CustomProgressBar progressBar = findViewById(R.id.progress_bar);
 * progressBar.setProgress(0.75f);  // 设置到75%，带动画
 * progressBar.setProgress(0.5f, false);  // 设置到50%，无动画
 * progressBar.setProgressMode(CustomProgressBar.MODE_CIRCULAR);  // 切换到环形模式
 * </pre>
 * 
 * @author AndroidUIForJava
 * @version 1.0
 */
public class CustomProgressBar extends FrameLayout {
    // 进度条模式
    public static final int MODE_LINEAR = 0;  // 线性模式
    public static final int MODE_CIRCULAR = 1; // 环形模式

    private View progressBackground;
    private View progressForeground;
    private CircularProgressView circularProgressView;
    private int progressMode = MODE_LINEAR; // 默认线性模式
    private float currentProgress = 0.25f; // 默认进度25%
    private ValueAnimator animator;
    private int animationDuration = 500; // 默认动画时长500ms
    private String accessibilityContentDescription; // 无障碍内容描述

    /**
     * 构造函数1：仅传入Context
     * 
     * <p><b>为什么需要这个构造函数？</b></p>
     * <ul>
     *   <li>当通过代码动态创建View时使用：<code>new CustomProgressBar(context)</code></li>
     *   <li>Android系统在某些情况下（如View的inflate过程）会调用这个构造函数</li>
     *   <li>这是View类的标准构造函数之一，必须提供以保持兼容性</li>
     * </ul>
     * 
     * <p><b>实现方式：</b>调用下一个构造函数，传入null作为attrs参数</p>
     * 
     * @param context 上下文对象，用于访问资源和系统服务
     */
    public CustomProgressBar(@NonNull Context context) {
        this(context, null);
    }

    /**
     * 构造函数2：传入Context和AttributeSet
     * 
     * <p><b>为什么需要这个构造函数？</b></p>
     * <ul>
     *   <li>当View从XML布局文件中inflate时，系统会调用这个构造函数</li>
     *   <li>AttributeSet包含了XML中定义的所有属性（如app:progress、app:progressMode等）</li>
     *   <li>这是最常用的构造函数，因为大多数View都是从XML布局创建的</li>
     * </ul>
     * 
     * <p><b>实现方式：</b>调用下一个构造函数，传入0作为defStyleAttr（使用默认样式）</p>
     * 
     * <p><b>使用场景：</b></p>
     * <pre>
     * // XML布局中
     * &lt;com.example.progressbar.CustomProgressBar
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     app:progress="0.5" /&gt;
     * // 系统会自动调用这个构造函数，并传入XML中的属性
     * </pre>
     * 
     * @param context 上下文对象
     * @param attrs XML属性集合，包含XML中定义的所有属性值
     */
    public CustomProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造函数3：完整的构造函数，包含所有参数
     * 
     * <p><b>为什么需要这个构造函数？</b></p>
     * <ul>
     *   <li>这是最完整的构造函数，包含了所有可能的参数</li>
     *   <li>当需要从主题中获取默认样式时使用（通过defStyleAttr）</li>
     *   <li>前两个构造函数最终都会调用这个构造函数，实现代码复用</li>
     *   <li>这是Android View系统的标准设计模式：构造函数链式调用</li>
     * </ul>
     * 
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li><b>context：</b>上下文，用于访问资源、系统服务等</li>
     *   <li><b>attrs：</b>XML属性集合，包含XML中定义的所有属性</li>
     *   <li><b>defStyleAttr：</b>默认样式属性，用于从主题中获取样式（这里传入0表示不使用主题样式）</li>
     * </ul>
     * 
     * <p><b>构造函数链式调用的优势：</b></p>
     * <ol>
     *   <li><b>代码复用：</b>所有初始化逻辑都在这一个构造函数中，避免重复代码</li>
     *   <li><b>灵活性：</b>支持多种创建方式（代码创建、XML创建、带样式创建）</li>
     *   <li><b>兼容性：</b>符合Android View系统的标准，确保在各种场景下都能正常工作</li>
     * </ol>
     * 
     * <p><b>初始化流程：</b></p>
     * <ol>
     *   <li>调用父类构造函数：<code>super(context, attrs, defStyleAttr)</code> - 初始化FrameLayout</li>
     *   <li>调用init(attrs)：初始化自定义View的各个组件和属性</li>
     * </ol>
     * 
     * @param context 上下文对象
     * @param attrs XML属性集合
     * @param defStyleAttr 默认样式属性资源ID，0表示不使用主题样式
     */
    public CustomProgressBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 初始化方法
     * 
     * <p><b>功能：</b>初始化CustomProgressBar的所有组件和属性</p>
     * 
     * <p><b>初始化步骤：</b></p>
     * <ol>
     *   <li>加载布局文件，inflate出线性和环形进度条的View</li>
     *   <li>从XML属性中读取配置（进度值、模式、动画设置等）</li>
     *   <li>根据读取的属性设置进度条模式和初始进度</li>
     * </ol>
     * 
     * <p><b>XML属性读取：</b></p>
     * <ul>
     *   <li>使用TypedArray读取自定义属性，这是Android读取XML属性的标准方式</li>
     *   <li>每个属性都有默认值，如果XML中未设置则使用默认值</li>
     *   <li>读取完成后必须调用recycle()释放资源</li>
     * </ul>
     * 
     * @param attrs XML属性集合，如果为null则使用默认值
     */
    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.custom_progress_bar, this, true);
        progressBackground = findViewById(R.id.progress_background);
        progressForeground = findViewById(R.id.progress_foreground);
        circularProgressView = findViewById(R.id.circular_progress_view);
        
        // 从XML读取属性
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomProgressBar);
            
            try {
                // 读取进度值（默认0.25，即25%）
                float xmlProgress = typedArray.getFloat(R.styleable.CustomProgressBar_progress, 0.25f);
                currentProgress = Math.max(0f, Math.min(1f, xmlProgress));
                
                // 读取进度条模式（默认线性）
                int mode = typedArray.getInt(R.styleable.CustomProgressBar_progressMode, MODE_LINEAR);
                progressMode = (mode == MODE_CIRCULAR) ? MODE_CIRCULAR : MODE_LINEAR;
                
                // 读取是否使用动画（默认false，避免初始化时的动画效果）
                boolean animate = typedArray.getBoolean(R.styleable.CustomProgressBar_animate, false);
                
                // 读取动画时长（默认500ms）
                animationDuration = typedArray.getInteger(R.styleable.CustomProgressBar_animationDuration, 500);
                animationDuration = Math.max(0, animationDuration);
                
        // 设置进度条模式（这会显示对应的view）
        setProgressMode(progressMode);
        
        // 设置初始进度（根据XML中的animate属性决定是否使用动画）
        if (animate) {
            // 如果XML中设置了animate=true，使用动画
            setProgress(currentProgress, true);
        } else {
            // 默认不使用动画，直接设置
            setProgress(currentProgress, false);
        }
        
    } finally {
        typedArray.recycle();
    }
} else {
    // 如果没有XML属性，使用默认值
    setProgressMode(progressMode);
    setProgress(currentProgress, false);
}

// 初始化无障碍支持
initAccessibility();
    }

    /**
     * 布局方法重写
     * 
     * <p><b>为什么需要重写onLayout？</b></p>
     * <ul>
     *   <li>当View的尺寸或位置发生变化时，需要重新计算进度条的显示</li>
     *   <li>线性进度条需要根据背景View的实际宽度来计算前景View的宽度</li>
     *   <li>确保在布局完成后，进度条能正确显示</li>
     * </ul>
     * 
     * <p><b>实现原理：</b></p>
     * <ol>
     *   <li>调用父类的onLayout完成基础布局</li>
     *   <li>如果布局发生变化（changed=true），调用updateProgress()更新进度显示</li>
     *   <li>这样可以确保进度条在View尺寸变化后仍能正确显示</li>
     * </ol>
     * 
     * @param changed 布局是否发生变化
     * @param left 左边界位置
     * @param top 上边界位置
     * @param right 右边界位置
     * @param bottom 下边界位置
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateProgress();
        }
    }

    /**
     * 设置进度值（0-1）
     * 
     * <p><b>功能：</b>设置进度条的进度值，默认使用动画</p>
     * 
     * <p><b>实现原理：</b>调用重载方法，animate参数默认为true</p>
     * 
     * @param progress 进度值，范围0-1（0表示0%，1表示100%）
     *                 如果超出范围会自动限制在0-1之间
     */
    public void setProgress(float progress) {
        setProgress(progress, true);
    }

    /**
     * 设置进度值（0-1）
     * 
     * <p><b>功能：</b>设置进度条的进度值，可选择是否使用动画</p>
     * 
     * <p><b>实现原理：</b>
     * <ol>
     *   <li>首先限制进度值在0-1范围内：<code>Math.max(0f, Math.min(1f, progress))</code></li>
     *   <li>如果使用动画：调用animateProgress()创建ValueAnimator实现平滑过渡</li>
     *   <li>如果不使用动画：直接更新currentProgress并调用updateProgress()立即刷新</li>
     * </ol>
     * 
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>带动画：用户交互、数据加载等需要视觉反馈的场景</li>
     *   <li>无动画：初始化设置、快速更新等不需要动画的场景</li>
     * </ul>
     * 
     * @param progress 进度值，范围0-1（0表示0%，1表示100%）
     * @param animate 是否使用动画，true=平滑动画，false=立即更新
     */
    public void setProgress(float progress, boolean animate) {
        // 限制范围在0-1
        progress = Math.max(0f, Math.min(1f, progress));

        if (animate) {
            animateProgress(currentProgress, progress);
        } else {
            currentProgress = progress;
            updateProgress();
        }
    }

    /**
     * 设置进度条模式
     * 
     * <p><b>功能：</b>在线性模式和环形模式之间切换</p>
     * 
     * <p><b>实现原理：</b>
     * <ol>
     *   <li>验证模式参数的有效性（必须是MODE_LINEAR或MODE_CIRCULAR）</li>
     *   <li>通过显示/隐藏不同的View实现模式切换：
     *       <ul>
     *         <li>线性模式：显示progressBackground和progressForeground，隐藏circularProgressView</li>
     *         <li>环形模式：隐藏progressBackground和progressForeground，显示circularProgressView</li>
     *       </ul>
     *   </li>
     *   <li>切换模式后立即更新进度显示，确保新模式下进度值正确</li>
     * </ol>
     * 
     * <p><b>设计优势：</b></p>
     * <ul>
     *   <li>两种模式的View都预先创建，切换时只需显示/隐藏，性能好</li>
     *   <li>切换模式时进度值保持不变，用户体验好</li>
     *   <li>不需要重新创建View，避免内存分配和GC</li>
     * </ul>
     * 
     * @param mode 进度条模式：MODE_LINEAR（0）= 线性模式，MODE_CIRCULAR（1）= 环形模式
     *             如果传入无效值，方法会直接返回，不进行任何操作
     */
    public void setProgressMode(int mode) {
        if (mode != MODE_LINEAR && mode != MODE_CIRCULAR) {
            return;
        }
        
        this.progressMode = mode;
        
        if (progressBackground != null && progressForeground != null && circularProgressView != null) {
            if (mode == MODE_LINEAR) {
                // 显示线性进度条
                progressBackground.setVisibility(VISIBLE);
                progressForeground.setVisibility(VISIBLE);
                circularProgressView.setVisibility(GONE);
            } else {
                // 显示环形进度条
                progressBackground.setVisibility(GONE);
                progressForeground.setVisibility(GONE);
                circularProgressView.setVisibility(VISIBLE);
            }
            // 更新进度显示
            updateProgress();
        }
    }

    /**
     * 获取进度条模式
     */
    public int getProgressMode() {
        return progressMode;
    }

    /**
     * 获取当前进度值
     * @return 当前进度值（0-1）
     */
    public float getProgress() {
        return currentProgress;
    }

    /**
     * 设置动画时长
     * @param duration 动画时长（毫秒）
     */
    public void setAnimationDuration(int duration) {
        this.animationDuration = Math.max(0, duration);
    }

    /**
     * 获取动画时长
     * @return 动画时长（毫秒）
     */
    public int getAnimationDuration() {
        return animationDuration;
    }

    /**
     * 动画更新进度
     * 
     * <p><b>功能：</b>使用ValueAnimator实现平滑的进度变化动画</p>
     * 
     * <p><b>实现原理：</b>
     * <ol>
     *   <li><b>取消之前的动画：</b>如果已有动画在运行，先取消它，避免动画冲突</li>
     *   <li><b>创建ValueAnimator：</b>使用ofFloat(from, to)创建浮点数动画</li>
     *   <li><b>设置动画属性：</b>
     *       <ul>
     *         <li>duration：动画时长（可自定义）</li>
     *         <li>interpolator：使用AccelerateDecelerateInterpolator实现先加速后减速的效果</li>
     *       </ul>
     *   </li>
     *   <li><b>动画更新回调：</b>在addUpdateListener中实时更新currentProgress并刷新UI</li>
     *   <li><b>启动动画：</b>调用start()开始动画</li>
     * </ol>
     * 
     * <p><b>为什么使用AccelerateDecelerateInterpolator？</b></p>
     * <ul>
     *   <li>提供自然的动画效果：开始慢→中间快→结束慢</li>
     *   <li>符合用户的视觉习惯，看起来更流畅</li>
     *   <li>比线性动画（LinearInterpolator）更有质感</li>
     * </ul>
     * 
     * <p><b>性能优化：</b></p>
     * <ul>
     *   <li>每次创建新动画前都会取消旧动画，避免内存泄漏</li>
     *   <li>动画更新时只更新必要的View，不会造成过度绘制</li>
     * </ul>
     * 
     * @param from 起始进度值
     * @param to 目标进度值
     */
    private void animateProgress(float from, float to) {
        // 取消之前的动画
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(animationDuration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            currentProgress = (float) animation.getAnimatedValue();
            updateProgress();
        });
        animator.start();
    }

    /**
     * 更新进度显示
     * 
     * <p><b>功能：</b>根据当前模式更新进度条的显示</p>
     * 
     * <p><b>实现原理：</b>根据progressMode的值，调用对应的更新方法</p>
     * 
     * <p><b>设计模式：</b>策略模式 - 根据不同的模式采用不同的更新策略</p>
     */
    private void updateProgress() {
        if (progressMode == MODE_LINEAR) {
            updateLinearProgress();
        } else {
            updateCircularProgress();
        }
        
        // 更新无障碍信息
        updateAccessibilityInfo();
    }

    /**
     * 初始化无障碍支持
     * 
     * <p><b>功能：</b>设置无障碍相关的属性和监听</p>
     * 
     * <p><b>无障碍支持内容：</b></p>
     * <ul>
     *   <li>设置内容描述（ContentDescription）</li>
     *   <li>设置为可访问的View</li>
     *   <li>支持TalkBack朗读</li>
     *   <li>支持无障碍事件</li>
     * </ul>
     */
    private void initAccessibility() {
        // 设置为可访问的View
        ViewCompat.setAccessibilityDelegate(this, new androidx.core.view.AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                
                // 设置角色为进度条
                info.setClassName(android.widget.ProgressBar.class.getName());
                
                // 设置进度信息
                int progressPercent = (int) (currentProgress * 100);
                info.setRangeInfo(
                    AccessibilityNodeInfoCompat.RangeInfoCompat.obtain(
                        AccessibilityNodeInfoCompat.RangeInfoCompat.RANGE_TYPE_INT,
                        0f,  // min
                        100f, // max
                        (float) progressPercent  // current
                    )
                );
                
                // 设置内容描述
                String contentDesc = getAccessibilityContentDescription();
                if (contentDesc != null && !contentDesc.isEmpty()) {
                    info.setContentDescription(contentDesc);
                } else {
                    // 默认描述
                    String modeText = progressMode == MODE_LINEAR ? "线性" : "环形";
                    info.setContentDescription(String.format("%s进度条，当前进度%d%%", modeText, progressPercent));
                }
            }
        });
        
        // 设置为重要（不会被忽略）
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    /**
     * 更新无障碍信息
     * 
     * <p><b>功能：</b>当进度变化时，更新无障碍信息并发送事件</p>
     */
    private void updateAccessibilityInfo() {
        // 发送无障碍事件，通知屏幕阅读器进度已更新
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        
        // 更新内容描述
        int progressPercent = (int) (currentProgress * 100);
        String contentDesc = getAccessibilityContentDescription();
        if (contentDesc == null || contentDesc.isEmpty()) {
            String modeText = progressMode == MODE_LINEAR ? "线性" : "环形";
            setContentDescription(String.format("%s进度条，当前进度%d%%", modeText, progressPercent));
        }
    }

    /**
     * 设置无障碍内容描述
     * 
     * <p><b>功能：</b>自定义屏幕阅读器朗读的内容</p>
     * 
     * <p><b>使用场景：</b>当默认描述不够准确时，可以自定义</p>
     * 
     * @param description 内容描述，例如："下载进度，当前50%"
     */
    public void setAccessibilityContentDescription(String description) {
        this.accessibilityContentDescription = description;
        updateAccessibilityInfo();
    }

    /**
     * 获取无障碍内容描述
     */
    public String getAccessibilityContentDescription() {
        return accessibilityContentDescription;
    }

    /**
     * 更新线性进度条宽度
     * 
     * <p><b>功能：</b>更新线性模式下前景View的宽度，以显示当前进度</p>
     * 
     * <p><b>实现原理：</b>
     * <ol>
     *   <li>获取背景View的实际宽度（backgroundWidth）</li>
     *   <li>计算目标宽度：<code>targetWidth = backgroundWidth * currentProgress</code></li>
     *   <li>修改前景View的LayoutParams.width</li>
     *   <li>调用setLayoutParams()应用新的宽度</li>
     * </ol>
     * 
     * <p><b>为什么需要检查宽度？</b></p>
     * <ul>
     *   <li>在View的onLayout完成之前，getWidth()可能返回0</li>
     *   <li>如果宽度为0，使用post()延迟更新，等待布局完成</li>
     *   <li>这确保了进度条在任何时候都能正确显示</li>
     * </ul>
     * 
     * <p><b>性能优化：</b></p>
     * <ul>
     *   <li>只有当宽度发生变化时才更新LayoutParams，避免不必要的布局计算</li>
     *   <li>使用post()而不是直接更新，避免在布局过程中修改布局参数</li>
     * </ul>
     */
    private void updateLinearProgress() {
        if (progressForeground == null || progressBackground == null) {
            return;
        }

        int backgroundWidth = progressBackground.getWidth();
        if (backgroundWidth > 0) {
            int targetWidth = (int) (backgroundWidth * currentProgress);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) progressForeground.getLayoutParams();
            if (params != null) {
                if (params.width != targetWidth) {
                    params.width = targetWidth;
                    progressForeground.setLayoutParams(params);
                }
            }
        } else {
            // 如果宽度还没确定，延迟更新
            post(() -> updateLinearProgress());
        }
    }

    /**
     * 更新环形进度条
     * 
     * <p><b>功能：</b>更新环形模式下CircularProgressView的进度显示</p>
     * 
     * <p><b>实现原理：</b>
     * <ol>
     *   <li>调用CircularProgressView.setProgress()设置进度值</li>
     *   <li>CircularProgressView内部会调用invalidate()触发重绘</li>
     *   <li>在onDraw()中根据进度值绘制对应的圆弧</li>
     * </ol>
     * 
     * <p><b>为什么环形模式使用invalidate()？</b></p>
     * <ul>
     *   <li>环形进度条使用Canvas绘制，需要重绘才能更新显示</li>
     *   <li>invalidate()会触发View的onDraw()方法，重新绘制进度条</li>
     *   <li>这是自定义绘制View的标准更新方式</li>
     * </ul>
     */
    private void updateCircularProgress() {
        if (circularProgressView != null) {
            circularProgressView.setProgress(currentProgress);
        }
    }

    /**
     * View从窗口分离时调用
     * 
     * <p><b>为什么需要重写onDetachedFromWindow？</b></p>
     * <ul>
     *   <li><b>防止内存泄漏：</b>如果动画还在运行，View被移除后动画可能持有View的引用</li>
     *   <li><b>避免异常：</b>View被销毁后，动画更新回调中访问View会导致异常</li>
     *   <li><b>资源清理：</b>及时释放动画资源，避免不必要的CPU和内存占用</li>
     * </ul>
     * 
     * <p><b>实现原理：</b>
     * <ol>
     *   <li>调用父类方法完成基础清理</li>
     *   <li>如果动画正在运行，调用cancel()取消动画</li>
     *   <li>将animator设置为null，释放引用</li>
     * </ol>
     * 
     * <p><b>调用时机：</b></p>
     * <ul>
     *   <li>Activity/Fragment销毁时</li>
     *   <li>View从父容器中移除时</li>
     *   <li>View被替换时</li>
     * </ul>
     * 
     * <p><b>最佳实践：</b>所有使用动画的自定义View都应该重写此方法进行清理</p>
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 清理动画
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}

