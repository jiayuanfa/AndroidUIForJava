package com.example.lazyloadview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 懒加载卡片视图组件
 * 
 * <p><b>功能特性：</b></p>
 * <ul>
 *   <li>使用ViewStub实现布局的懒加载，提升性能</li>
 *   <li>标题区域立即加载，内容区域按需加载</li>
 *   <li>支持代码动态创建布局并懒加载</li>
 *   <li>自动防止重复加载，确保ViewStub只加载一次</li>
 * </ul>
 * 
 * <p><b>实现原理：</b></p>
 * <ol>
 *   <li><b>ViewStub懒加载机制：</b>
 *       <ul>
 *         <li>ViewStub是一个轻量级的占位符，不会立即inflate布局</li>
 *         <li>只有当调用inflate()时，才会真正加载布局资源</li>
 *         <li>加载后，ViewStub会被替换为实际的View，ViewStub本身会从视图树中移除</li>
 *       </ul>
 *   </li>
 *   <li><b>防止重复加载：</b>
 *       <ul>
 *         <li>通过检查ViewStub的parent是否为null来判断是否已加载</li>
 *         <li>如果parent为null，说明ViewStub已被替换，布局已加载</li>
 *         <li>如果parent不为null，说明ViewStub还在，可以安全加载</li>
 *       </ul>
 *   </li>
 *   <li><b>代码创建布局：</b>
 *       <ul>
 *         <li>使用LayoutInflater.from(context).inflate()创建View</li>
 *         <li>将创建的View添加到ViewStub的位置</li>
 *         <li>移除ViewStub，完成懒加载</li>
 *       </ul>
 *   </li>
 * </ol>
 * 
 * <p><b>使用示例：</b></p>
 * <pre>
 * // XML中使用
 * &lt;com.example.lazyloadview.LazyLoadCardView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:title="卡片标题" /&gt;
 * 
 * // 代码中使用
 * LazyLoadCardView cardView = findViewById(R.id.card_view);
 * cardView.setTitle("我的卡片");
 * cardView.loadContent();  // 懒加载内容区域
 * cardView.loadActions();  // 懒加载操作区域（可选）
 * </pre>
 * 
 * @author AndroidUIForJava
 * @version 1.0
 */
public class LazyLoadCardView extends FrameLayout {
    private static final String TAG = "LazyLoadCardView";
    
    // 立即加载的View
    private TextView titleView;
    
    // ViewStub引用（懒加载）
    private ViewStub contentViewStub;
    private ViewStub actionsViewStub;
    
    // 已加载的View引用
    private View loadedContentView;
    private View loadedActionsView;
    
    // 标记是否已加载
    private boolean isContentLoaded = false;
    private boolean isActionsLoaded = false;
    
    /**
     * 构造函数1：仅传入Context
     * 
     * <p><b>为什么需要这个构造函数？</b></p>
     * <ul>
     *   <li>当通过代码动态创建View时使用：<code>new LazyLoadCardView(context)</code></li>
     *   <li>Android系统在某些情况下（如View的inflate过程）会调用这个构造函数</li>
     *   <li>这是View类的标准构造函数之一，必须提供以保持兼容性</li>
     * </ul>
     */
    public LazyLoadCardView(@NonNull Context context) {
        this(context, null);
    }
    
    /**
     * 构造函数2：传入Context和AttributeSet
     * 
     * <p><b>为什么需要这个构造函数？</b></p>
     * <ul>
     *   <li>当从XML布局文件中创建View时使用</li>
     *   <li>AttributeSet包含XML中定义的所有属性</li>
     *   <li>这是最常用的构造函数，用于读取XML属性并初始化View</li>
     * </ul>
     */
    public LazyLoadCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * 构造函数3：传入Context、AttributeSet和defStyleAttr
     * 
     * <p><b>为什么需要这个构造函数？</b></p>
     * <ul>
     *   <li>当需要应用主题样式时使用</li>
     *   <li>defStyleAttr是主题中定义的样式属性引用</li>
     *   <li>允许View从主题中获取默认样式，提供更好的样式继承机制</li>
     *   <li>这是最完整的构造函数，支持所有初始化场景</li>
     * </ul>
     */
    public LazyLoadCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    /**
     * 初始化方法
     * 加载布局并初始化立即需要的View
     */
    private void init() {
        Log.d(TAG, "[初始化] init() - 开始初始化LazyLoadCardView");
        
        // 加载主布局（包含ViewStub）
        LayoutInflater.from(getContext()).inflate(R.layout.lazy_load_card_view, this, true);
        
        // 初始化立即加载的View
        titleView = findViewById(R.id.tv_title);
        
        // 获取ViewStub引用（此时还未加载）
        contentViewStub = findViewById(R.id.vs_content);
        actionsViewStub = findViewById(R.id.vs_actions);
        
        Log.d(TAG, "[初始化] init() - LazyLoadCardView初始化完成，ViewStub已准备就绪");
    }
    
    /**
     * 设置标题
     * 
     * @param title 标题文本
     */
    public void setTitle(String title) {
        if (titleView != null) {
            titleView.setText(title);
            Log.d(TAG, "[设置] setTitle() - 标题设置为: " + title);
        }
    }
    
    /**
     * 懒加载内容区域
     * 使用ViewStub的inflate()方法加载布局
     * 
     * <p><b>最佳实践：</b></p>
     * <ul>
     *   <li>在真正需要显示内容时才调用此方法</li>
     *   <li>方法内部会自动检查是否已加载，避免重复加载</li>
     *   <li>加载后，ViewStub会被替换为实际View，ViewStub本身会从视图树中移除</li>
     * </ul>
     * 
     * @return 加载后的内容View，如果已加载则返回之前加载的View
     */
    public View loadContent() {
        if (isContentLoaded) {
            Log.d(TAG, "[懒加载] loadContent() - 内容区域已加载，直接返回");
            return loadedContentView;
        }
        
        if (contentViewStub == null) {
            Log.w(TAG, "[懒加载] loadContent() - ViewStub为null，无法加载");
            return null;
        }
        
        // 检查ViewStub是否还在视图树中（未加载时parent不为null）
        // 如果parent为null，说明ViewStub已被替换，布局已加载
        if (contentViewStub.getParent() == null) {
            Log.d(TAG, "[懒加载] loadContent() - ViewStub已被替换，尝试查找已加载的View");
            // ViewStub已被替换，尝试通过ID查找已加载的View
            loadedContentView = findViewById(R.id.content_container);
            if (loadedContentView != null) {
                isContentLoaded = true;
                Log.d(TAG, "[懒加载] loadContent() - 找到已加载的内容View");
                return loadedContentView;
            }
        }
        
        try {
            Log.d(TAG, "[懒加载] loadContent() - 开始加载内容区域布局");
            // 调用inflate()加载ViewStub引用的布局
            // inflate()会返回加载后的根View，并将ViewStub从视图树中移除
            loadedContentView = contentViewStub.inflate();
            isContentLoaded = true;
            contentViewStub = null; // 清空引用，因为ViewStub已被移除
            Log.d(TAG, "[懒加载] loadContent() - 内容区域加载成功");
            return loadedContentView;
        } catch (Exception e) {
            Log.e(TAG, "[懒加载] loadContent() - 加载失败: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 懒加载操作区域（可选）
     * 
     * @return 加载后的操作View，如果已加载则返回之前加载的View
     */
    public View loadActions() {
        if (isActionsLoaded) {
            Log.d(TAG, "[懒加载] loadActions() - 操作区域已加载，直接返回");
            return loadedActionsView;
        }
        
        if (actionsViewStub == null) {
            Log.w(TAG, "[懒加载] loadActions() - ViewStub为null，无法加载");
            return null;
        }
        
        // 检查ViewStub是否还在视图树中
        if (actionsViewStub.getParent() == null) {
            Log.d(TAG, "[懒加载] loadActions() - ViewStub已被替换，尝试查找已加载的View");
            loadedActionsView = findViewById(R.id.actions_container);
            if (loadedActionsView != null) {
                isActionsLoaded = true;
                Log.d(TAG, "[懒加载] loadActions() - 找到已加载的操作View");
                return loadedActionsView;
            }
        }
        
        try {
            Log.d(TAG, "[懒加载] loadActions() - 开始加载操作区域布局");
            loadedActionsView = actionsViewStub.inflate();
            isActionsLoaded = true;
            actionsViewStub = null; // 清空引用
            Log.d(TAG, "[懒加载] loadActions() - 操作区域加载成功");
            return loadedActionsView;
        } catch (Exception e) {
            Log.e(TAG, "[懒加载] loadActions() - 加载失败: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 使用代码动态创建布局并懒加载
     * 
     * <p><b>最佳实践：</b></p>
     * <ul>
     *   <li>当布局需要根据数据动态生成时使用此方法</li>
     *   <li>避免在XML中定义大量可能不使用的布局</li>
     *   <li>在真正需要时才创建View，节省内存和初始化时间</li>
     * </ul>
     * 
     * @param layoutResId 要加载的布局资源ID
     * @return 加载后的View
     */
    public View loadContentFromCode(int layoutResId) {
        if (isContentLoaded) {
            Log.d(TAG, "[代码加载] loadContentFromCode() - 内容区域已加载，直接返回");
            return loadedContentView;
        }
        
        if (contentViewStub == null || contentViewStub.getParent() == null) {
            Log.w(TAG, "[代码加载] loadContentFromCode() - ViewStub不可用，无法加载");
            return null;
        }
        
        try {
            Log.d(TAG, "[代码加载] loadContentFromCode() - 开始从代码加载布局: " + layoutResId);
            
            // 使用LayoutInflater创建View
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View contentView = inflater.inflate(layoutResId, this, false);
            
            // 获取ViewStub在父容器中的位置
            int index = indexOfChild(contentViewStub);
            
            // 移除ViewStub
            removeView(contentViewStub);
            
            // 在相同位置添加新创建的View
            addView(contentView, index);
            
            loadedContentView = contentView;
            isContentLoaded = true;
            contentViewStub = null; // 清空引用
            
            Log.d(TAG, "[代码加载] loadContentFromCode() - 代码加载成功");
            return loadedContentView;
        } catch (Exception e) {
            Log.e(TAG, "[代码加载] loadContentFromCode() - 加载失败: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 检查内容区域是否已加载
     * 
     * @return true表示已加载，false表示未加载
     */
    public boolean isContentLoaded() {
        return isContentLoaded;
    }
    
    /**
     * 检查操作区域是否已加载
     * 
     * @return true表示已加载，false表示未加载
     */
    public boolean isActionsLoaded() {
        return isActionsLoaded;
    }
    
    /**
     * 获取已加载的内容View
     * 如果未加载，返回null
     * 
     * @return 内容View或null
     */
    @Nullable
    public View getContentView() {
        return loadedContentView;
    }
    
    /**
     * 获取已加载的操作View
     * 如果未加载，返回null
     * 
     * @return 操作View或null
     */
    @Nullable
    public View getActionsView() {
        return loadedActionsView;
    }
}

