package com.example.refreshrecyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 支持下拉刷新和上拉加载更多的RecyclerView
 */
public class RefreshRecyclerView extends RecyclerView implements NestedScrollingChild2, NestedScrollingParent2 {
    
    // 刷新状态
    public static final int STATE_IDLE = 0;           // 空闲状态
    public static final int STATE_PULL_DOWN = 1;      // 下拉中
    public static final int STATE_RELEASE_TO_REFRESH = 2; // 释放刷新
    public static final int STATE_REFRESHING = 3;     // 刷新中
    public static final int STATE_PULL_UP = 4;        // 上拉中
    public static final int STATE_RELEASE_TO_LOAD_MORE = 5; // 释放加载更多
    public static final int STATE_LOADING_MORE = 6;   // 加载更多中
    
    private int mCurrentState = STATE_IDLE;
    
    // 下拉刷新相关
    private RefreshHeader mRefreshHeader;
    private boolean mEnablePullRefresh = true; // 是否启用下拉刷新
    private OnRefreshListener mOnRefreshListener;
    private int mHeaderHeight; // 头部高度
    private int mCurrentHeaderOffset; // 当前头部偏移量
    private int mTouchSlop; // 触摸滑动阈值
    private float mInitialDownY; // 初始按下Y坐标
    private boolean mIsBeingDragged; // 是否正在拖拽
    
    // 上拉加载更多相关
    private LoadMoreFooter mLoadMoreFooter;
    private boolean mEnableLoadMore = true; // 是否启用上拉加载更多
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean mIsLoadingMore = false; // 是否正在加载更多
    private boolean mHasMoreData = true; // 是否还有更多数据
    
    // NestedScrolling相关
    private NestedScrollingChildHelper mChildHelper;
    private NestedScrollingParentHelper mParentHelper;
    
    // 监听器
    private OnScrollListener mInternalScrollListener;
    
    // 动画相关
    private ValueAnimator mHeaderOffsetAnimator;
    private static final int MIN_ANIM_DURATION = 200; // 最小动画时长（毫秒）
    private static final int MAX_ANIM_DURATION = 400; // 最大动画时长（毫秒）
    
    public RefreshRecyclerView(@NonNull Context context) {
        this(context, null);
    }
    
    public RefreshRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public RefreshRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 在super之后初始化helper
        mChildHelper = new NestedScrollingChildHelper(this);
        mParentHelper = new NestedScrollingParentHelper(this);
        init(context);
    }
    
    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setNestedScrollingEnabled(true);
        setClipToPadding(false); // 允许在 padding 区域绘制，这样头部视图可以显示在 padding 区域
        
        // 延迟初始化头部和底部视图，避免在构造函数中出错
        // mRefreshHeader 和 mLoadMoreFooter 将在首次使用时初始化
        
        // 添加滚动监听
        mInternalScrollListener = new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                checkLoadMore();
            }
        };
        addOnScrollListener(mInternalScrollListener);
    }
    
    /**
     * 确保头部视图已初始化（不再添加到父容器，只通过Canvas绘制）
     */
    private void ensureHeaderViewInParent() {
        // 不再将头部视图添加到父容器，只确保头部视图已初始化
        if (mRefreshHeader == null) {
            ensureRefreshHeader(getContext());
        }
    }
    
    /**
     * 确保头部视图已初始化
     */
    private void ensureRefreshHeader(Context context) {
        if (mRefreshHeader == null) {
            mRefreshHeader = new DefaultRefreshHeader(context);
        }
    }
    
    /**
     * 确保底部视图已初始化
     */
    private void ensureLoadMoreFooter(Context context) {
        if (mLoadMoreFooter == null) {
            mLoadMoreFooter = new DefaultLoadMoreFooter(context);
        }
    }
    
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        // 如果头部视图还未初始化，先初始化它
        if (mEnablePullRefresh && mRefreshHeader == null) {
            ensureRefreshHeader(getContext());
        }
        // 测量头部视图高度
        if (mRefreshHeader != null && mHeaderHeight == 0) {
            View headerView = mRefreshHeader.getHeaderView();
            if (headerView != null) {
                int headerWidthSpec = MeasureSpec.makeMeasureSpec(
                        MeasureSpec.getSize(widthSpec), 
                        MeasureSpec.getMode(widthSpec)
                );
                int headerHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                headerView.measure(headerWidthSpec, headerHeightSpec);
                mHeaderHeight = headerView.getMeasuredHeight();
            }
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 头部视图只通过Canvas绘制，不需要在这里布局
    }
    
    /**
     * 布局头部视图（已废弃，不再将头部视图添加到父容器，只通过Canvas绘制）
     * 保留此方法用于确保头部视图已测量和布局
     */
    private void layoutHeaderView() {
        // 不再将头部视图添加到父容器，只确保头部视图已测量和布局
        if (mRefreshHeader == null || mCurrentHeaderOffset <= 0) {
            return;
        }
        
        View headerView = mRefreshHeader.getHeaderView();
        if (headerView == null) {
            return;
        }
        
        // 始终重新测量头部视图，因为文字可能已经改变
        int width = getWidth();
        if (width > 0) {
            int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            headerView.measure(widthSpec, heightSpec);
            mHeaderHeight = headerView.getMeasuredHeight();
        }
        
        // 布局头部视图，确保其子视图正确布局
        if (mHeaderHeight > 0 && headerView.getMeasuredWidth() > 0) {
            headerView.layout(0, 0, headerView.getMeasuredWidth(), headerView.getMeasuredHeight());
        }
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // 头部视图只通过Canvas绘制，不添加到父容器，避免覆盖导航条
        // 如果头部视图需要显示，确保它已经被布局和绘制
        if (mRefreshHeader != null && mCurrentHeaderOffset > 0 && mHeaderHeight > 0 && isAttachedToWindow()) {
            // 确保头部视图被正确测量和布局
            layoutHeaderView();
            // 直接绘制头部视图，确保它只在RecyclerView内部显示
            drawHeaderView(canvas);
        }
    }
    
    /**
     * 直接绘制头部视图到 canvas
     */
    private void drawHeaderView(Canvas canvas) {
        if (mRefreshHeader == null || mCurrentHeaderOffset <= 0 || mHeaderHeight == 0) {
            return;
        }
        
        View headerView = mRefreshHeader.getHeaderView();
        if (headerView == null) {
            return;
        }
        
        // 确保头部视图已测量和布局
        int width = getWidth();
        if (width > 0) {
            if (headerView.getWidth() == 0 || headerView.getHeight() == 0) {
                int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                headerView.measure(widthSpec, heightSpec);
                mHeaderHeight = headerView.getMeasuredHeight();
            }
            
            if (mHeaderHeight > 0 && headerView.getMeasuredWidth() > 0) {
                // 布局头部视图，确保其子视图正确布局
                int headerWidth = headerView.getMeasuredWidth();
                int headerHeight = headerView.getMeasuredHeight();
                headerView.layout(0, 0, headerWidth, headerHeight);
                
                // 确保头部视图及其子视图可见
                headerView.setVisibility(View.VISIBLE);
                if (headerView instanceof ViewGroup) {
                    ViewGroup headerGroup = (ViewGroup) headerView;
                    for (int i = 0; i < headerGroup.getChildCount(); i++) {
                        View child = headerGroup.getChildAt(i);
                        if (child != null) {
                            child.setVisibility(View.VISIBLE);
                        }
                    }
                }
                
                // 保存 canvas 状态
                canvas.save();
                try {
                    // 计算头部视图的绘制位置（在 padding 区域）
                    float top = mCurrentHeaderOffset - mHeaderHeight;
                    // 将头部视图绘制在 padding 区域
                    canvas.translate(0, top);
                    
                    // 绘制头部视图及其子视图
                    headerView.draw(canvas);
                } catch (Exception e) {
                    // 绘制失败，忽略
                } finally {
                    canvas.restore();
                }
            }
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 在视图附加到窗口时，预初始化头部视图，以便能够正确测量和显示
        if (mRefreshHeader == null && mEnablePullRefresh) {
            ensureRefreshHeader(getContext());
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        // 先取消动画，避免在视图销毁时继续更新
        cancelHeaderOffsetAnimation();
        
        // 重置状态
        mCurrentHeaderOffset = 0;
        mCurrentState = STATE_IDLE;
        
        // 头部视图不再添加到父容器，不需要移除
        // 只重置状态即可
        
        super.onDetachedFromWindow();
    }
    
    /**
     * 设置刷新头部视图
     */
    public void setRefreshHeader(RefreshHeader header) {
        mRefreshHeader = header;
        if (mRefreshHeader != null) {
            View headerView = mRefreshHeader.getHeaderView();
            if (headerView.getParent() == null) {
                // 头部视图将通过padding方式显示，不直接添加到RecyclerView
                // 重新测量头部高度
                if (getWidth() > 0) {
                    headerView.measure(
                            MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    );
                    mHeaderHeight = headerView.getMeasuredHeight();
                }
            }
        }
        requestLayout();
    }
    
    /**
     * 设置加载更多底部视图
     */
    public void setLoadMoreFooter(LoadMoreFooter footer) {
        mLoadMoreFooter = footer;
    }
    
    /**
     * 设置是否启用下拉刷新
     */
    public void setEnablePullRefresh(boolean enable) {
        mEnablePullRefresh = enable;
    }
    
    /**
     * 设置是否启用上拉加载更多
     */
    public void setEnableLoadMore(boolean enable) {
        mEnableLoadMore = enable;
    }
    
    /**
     * 设置刷新监听器
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }
    
    /**
     * 设置加载更多监听器
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (!mEnablePullRefresh || mRefreshHeader == null) {
            return super.onInterceptTouchEvent(e);
        }
        
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialDownY = e.getY();
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = e.getY() - mInitialDownY;
                if (Math.abs(deltaY) > mTouchSlop) {
                    // 向下滑动且在顶部
                    if (deltaY > 0 && !canScrollVertically(-1) && mCurrentState != STATE_REFRESHING) {
                        mIsBeingDragged = true;
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                break;
        }
        
        return super.onInterceptTouchEvent(e);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!mEnablePullRefresh || mRefreshHeader == null) {
            return super.onTouchEvent(e);
        }
        
        if (mCurrentState == STATE_REFRESHING) {
            return super.onTouchEvent(e);
        }
        
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialDownY = e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsBeingDragged || (!canScrollVertically(-1) && e.getY() - mInitialDownY > mTouchSlop)) {
                    float deltaY = e.getY() - mInitialDownY;
                    handlePullDown(deltaY);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrentState == STATE_RELEASE_TO_REFRESH) {
                    startRefresh();
                } else if (mCurrentState == STATE_PULL_DOWN) {
                    resetRefreshState();
                }
                mIsBeingDragged = false;
                break;
        }
        
        return super.onTouchEvent(e);
    }
    
    /**
     * 处理下拉刷新
     */
    private void handlePullDown(float deltaY) {
        if (mCurrentState == STATE_REFRESHING) {
            return;
        }
        
        // 延迟初始化头部视图
        ensureRefreshHeader(getContext());
        
        if (mRefreshHeader == null) {
            return;
        }
        
        // 确保头部高度已测量
        if (mHeaderHeight == 0) {
            View headerView = mRefreshHeader.getHeaderView();
            if (headerView != null) {
                int widthSpec = MeasureSpec.makeMeasureSpec(getWidth() > 0 ? getWidth() : 1080, MeasureSpec.EXACTLY);
                int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                headerView.measure(widthSpec, heightSpec);
                mHeaderHeight = headerView.getMeasuredHeight();
            }
        }
        
        if (mHeaderHeight == 0) {
            return; // 如果头部高度仍为0，无法处理下拉
        }
        
        // 限制最大下拉距离为头部高度的2倍
        float maxPullDistance = mHeaderHeight * 2.0f;
        float pullDistance = Math.min(deltaY, maxPullDistance);
        
        mCurrentHeaderOffset = (int) pullDistance;
        setPadding(0, mCurrentHeaderOffset, 0, 0);
        setClipToPadding(false); // 允许在 padding 区域绘制
        
        // 确保头部视图已初始化
        if (mCurrentHeaderOffset > 0) {
            // 确保头部视图已初始化
            ensureHeaderViewInParent();
            // 确保头部视图已测量和布局（用于Canvas绘制）
            layoutHeaderView();
        }
        
        float progress = pullDistance / mHeaderHeight;
        
        if (pullDistance > mHeaderHeight) {
            if (mCurrentState != STATE_RELEASE_TO_REFRESH) {
                mCurrentState = STATE_RELEASE_TO_REFRESH;
                mRefreshHeader.onReleaseToRefresh();
            }
        } else {
            if (mCurrentState != STATE_PULL_DOWN) {
                mCurrentState = STATE_PULL_DOWN;
            }
            mRefreshHeader.onPullDown(progress);
        }
        
        invalidate(); // 请求重绘以显示头部
    }
    
    /**
     * 开始刷新
     */
    private void startRefresh() {
        if (mCurrentState == STATE_REFRESHING || mRefreshHeader == null) {
            return;
        }
        
        // 取消之前的动画
        cancelHeaderOffsetAnimation();
        
        if (mHeaderHeight == 0) {
            // 如果头部高度未测量，直接设置状态
            mCurrentState = STATE_REFRESHING;
            mRefreshHeader.onRefreshing();
            if (mOnRefreshListener != null) {
                mOnRefreshListener.onRefresh();
            }
            return;
        }
        
        int startOffset = mCurrentHeaderOffset;
        int targetOffset = mHeaderHeight;
        
        // 计算动画时长：基于距离，距离越大，时间越长
        int distance = Math.abs(targetOffset - startOffset);
        int duration = calculateAnimationDuration(distance);
        
        // 创建动画
        mHeaderOffsetAnimator = ValueAnimator.ofInt(startOffset, targetOffset);
        mHeaderOffsetAnimator.setDuration(duration);
        mHeaderOffsetAnimator.setInterpolator(new DecelerateInterpolator());
        mHeaderOffsetAnimator.addUpdateListener(animation -> {
            // 检查视图是否还附加到窗口
            if (!isAttachedToWindow() || mRefreshHeader == null) {
                cancelHeaderOffsetAnimation();
                return;
            }
            try {
                mCurrentHeaderOffset = (Integer) animation.getAnimatedValue();
                setPadding(0, mCurrentHeaderOffset, 0, 0);
                invalidate();
                requestLayout();
            } catch (Exception e) {
                // 如果发生异常，取消动画
                cancelHeaderOffsetAnimation();
            }
        });
        mHeaderOffsetAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // 检查视图是否还附加到窗口
                if (!isAttachedToWindow() || mRefreshHeader == null) {
                    return;
                }
                try {
                    mCurrentState = STATE_REFRESHING;
                    mRefreshHeader.onRefreshing();
                    // 文字改变后，需要重新测量头部视图高度，并触发重绘
                    if (mRefreshHeader != null) {
                        View headerView = mRefreshHeader.getHeaderView();
                        if (headerView != null && getWidth() > 0) {
                            int widthSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
                            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                            headerView.measure(widthSpec, heightSpec);
                            mHeaderHeight = headerView.getMeasuredHeight();
                        }
                    }
                    invalidate(); // 触发重绘，显示新的文字
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onRefresh();
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
            }
            
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                // 动画被取消时的处理
            }
        });
        mHeaderOffsetAnimator.start();
    }
    
    /**
     * 完成刷新
     */
    public void finishRefresh() {
        if (mCurrentState != STATE_REFRESHING) {
            return;
        }
        // 先显示"刷新完成"状态
        if (mRefreshHeader != null) {
            mRefreshHeader.onRefreshComplete();
        }
        // 延迟一点后再回到空闲状态，让用户看到"刷新完成"的提示
        postDelayed(() -> {
            if (mCurrentState == STATE_REFRESHING) {
                resetRefreshState();
            }
        }, 500); // 500ms 后重置
    }
    
    /**
     * 重置刷新状态
     */
    private void resetRefreshState() {
        // 取消之前的动画
        cancelHeaderOffsetAnimation();
        
        if (mCurrentHeaderOffset == 0) {
            // 如果已经在初始位置，直接设置状态
            mCurrentState = STATE_IDLE;
            if (mRefreshHeader != null) {
                mRefreshHeader.onIdle();
            }
            return;
        }
        
        int startOffset = mCurrentHeaderOffset;
        int targetOffset = 0;
        
        // 计算动画时长：基于距离，距离越大，时间越长
        int distance = Math.abs(targetOffset - startOffset);
        int duration = calculateAnimationDuration(distance);
        
        // 创建动画
        mHeaderOffsetAnimator = ValueAnimator.ofInt(startOffset, targetOffset);
        mHeaderOffsetAnimator.setDuration(duration);
        mHeaderOffsetAnimator.setInterpolator(new DecelerateInterpolator());
        mHeaderOffsetAnimator.addUpdateListener(animation -> {
            // 检查视图是否还附加到窗口
            if (!isAttachedToWindow() || mRefreshHeader == null) {
                cancelHeaderOffsetAnimation();
                return;
            }
            try {
                mCurrentHeaderOffset = (Integer) animation.getAnimatedValue();
                setPadding(0, mCurrentHeaderOffset, 0, 0);
                invalidate();
                requestLayout();
            } catch (Exception e) {
                // 如果发生异常，取消动画
                cancelHeaderOffsetAnimation();
            }
        });
        mHeaderOffsetAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // 检查视图是否还附加到窗口
                if (!isAttachedToWindow() || mRefreshHeader == null) {
                    return;
                }
                try {
                    mCurrentState = STATE_IDLE;
                    // 先调用 onRefreshComplete（如果有的话），然后调用 onIdle
                    if (mRefreshHeader != null) {
                        mRefreshHeader.onRefreshComplete();
                        // 延迟一点调用 onIdle，让用户看到"刷新完成"的提示
                        if (isAttachedToWindow()) {
                            postDelayed(() -> {
                                if (isAttachedToWindow() && mRefreshHeader != null && mCurrentState == STATE_IDLE) {
                                    try {
                                        mRefreshHeader.onIdle();
                                    } catch (Exception e) {
                                        // 忽略异常
                                    }
                                }
                            }, 500); // 500ms 后重置到空闲状态
                        }
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
            }
            
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                // 动画被取消时的处理
            }
        });
        mHeaderOffsetAnimator.start();
    }
    
    /**
     * 根据距离计算动画时长
     * @param distance 动画距离
     * @return 动画时长（毫秒）
     */
    private int calculateAnimationDuration(int distance) {
        if (mHeaderHeight == 0) {
            return MIN_ANIM_DURATION;
        }
        // 基于头部高度的比例计算时长
        float ratio = (float) distance / mHeaderHeight;
        // 将比例映射到 MIN_ANIM_DURATION 到 MAX_ANIM_DURATION 之间
        int duration = (int) (MIN_ANIM_DURATION + (MAX_ANIM_DURATION - MIN_ANIM_DURATION) * ratio);
        return Math.max(MIN_ANIM_DURATION, Math.min(MAX_ANIM_DURATION, duration));
    }
    
    /**
     * 取消头部偏移动画
     */
    private void cancelHeaderOffsetAnimation() {
        if (mHeaderOffsetAnimator != null) {
            if (mHeaderOffsetAnimator.isRunning()) {
                mHeaderOffsetAnimator.cancel();
            }
            // 移除所有监听器，避免在视图销毁后仍然被调用
            mHeaderOffsetAnimator.removeAllListeners();
            mHeaderOffsetAnimator.removeAllUpdateListeners();
            mHeaderOffsetAnimator = null;
        }
    }
    
    /**
     * 检查是否需要加载更多
     */
    private void checkLoadMore() {
        // 如果正在刷新，不检查加载更多
        if (mCurrentState == STATE_REFRESHING || 
            mCurrentState == STATE_RELEASE_TO_REFRESH || 
            mCurrentState == STATE_PULL_DOWN) {
            return;
        }
        
        if (!mEnableLoadMore || mIsLoadingMore || !mHasMoreData) {
            return;
        }
        
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || !(layoutManager instanceof LinearLayoutManager)) {
            return;
        }
        
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
        int totalItemCount = getAdapter() != null ? getAdapter().getItemCount() : 0;
        
        // 需要至少有一些数据才能加载更多
        if (totalItemCount == 0) {
            return;
        }
        
        // 当滚动到倒数第3个item时，触发加载更多
        if (lastVisiblePosition >= totalItemCount - 3 && lastVisiblePosition >= 0) {
            startLoadMore();
        }
    }
    
    /**
     * 开始加载更多
     */
    private void startLoadMore() {
        if (mIsLoadingMore || !mHasMoreData) {
            return;
        }
        mIsLoadingMore = true;
        mCurrentState = STATE_LOADING_MORE;
        if (mLoadMoreFooter != null) {
            mLoadMoreFooter.onLoading();
        }
        // 将footer添加到adapter中
        addFooterToAdapter();
        if (mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onLoadMore();
        }
    }
    
    /**
     * 完成加载更多
     */
    public void finishLoadMore() {
        finishLoadMore(true);
    }
    
    /**
     * 完成加载更多
     * @param hasMore 是否还有更多数据
     */
    public void finishLoadMore(boolean hasMore) {
        mIsLoadingMore = false;
        mHasMoreData = hasMore;
        mCurrentState = STATE_IDLE;
        // 移除footer
        removeFooterFromAdapter();
        if (mLoadMoreFooter != null) {
            if (hasMore) {
                mLoadMoreFooter.onLoadComplete();
            } else {
                mLoadMoreFooter.onNoMoreData();
            }
        }
    }
    
    /**
     * 将footer添加到adapter中
     */
    private void addFooterToAdapter() {
        Adapter adapter = getAdapter();
        if (adapter != null && mLoadMoreFooter != null) {
            WrapAdapter wrapAdapter;
            if (!(adapter instanceof WrapAdapter)) {
                wrapAdapter = new WrapAdapter(adapter);
                setAdapter(wrapAdapter);
            } else {
                wrapAdapter = (WrapAdapter) adapter;
            }
            wrapAdapter.setHasFooter(true);
        }
    }
    
    /**
     * 从adapter中移除footer
     */
    private void removeFooterFromAdapter() {
        Adapter adapter = getAdapter();
        if (adapter instanceof WrapAdapter && mLoadMoreFooter != null) {
            WrapAdapter wrapAdapter = (WrapAdapter) adapter;
            wrapAdapter.setHasFooter(false);
        }
    }
    
    /**
     * 获取当前状态
     */
    public int getCurrentState() {
        return mCurrentState;
    }
    
    /**
     * 包装Adapter以支持底部视图
     */
    private class WrapAdapter extends Adapter {
        private final Adapter mInnerAdapter;
        private static final int TYPE_FOOTER = -999;
        private boolean mHasFooter = false;
        
        public WrapAdapter(Adapter adapter) {
            mInnerAdapter = adapter;
            mInnerAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    notifyDataSetChanged();
                }
                
                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    notifyItemRangeChanged(positionStart, itemCount);
                }
                
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    notifyItemRangeInserted(positionStart, itemCount);
                }
                
                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    notifyItemRangeRemoved(positionStart, itemCount);
                }
            });
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER && mLoadMoreFooter != null) {
                // 为footer创建一个新的View
                View footerView = mLoadMoreFooter.getFooterView();
                ViewGroup parentView = (ViewGroup) footerView.getParent();
                if (parentView != null) {
                    parentView.removeView(footerView);
                }
                return new FooterViewHolder(footerView);
            }
            return mInnerAdapter.onCreateViewHolder(parent, viewType);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_FOOTER) {
                return;
            }
            mInnerAdapter.onBindViewHolder(holder, position);
        }
        
        @Override
        public int getItemCount() {
            int count = mInnerAdapter.getItemCount();
            if (mHasFooter && mLoadMoreFooter != null) {
                count += 1;
            }
            return count;
        }
        
        @Override
        public int getItemViewType(int position) {
            if (mHasFooter && mLoadMoreFooter != null && position >= mInnerAdapter.getItemCount()) {
                return TYPE_FOOTER;
            }
            return mInnerAdapter.getItemViewType(position);
        }
        
        public void setHasFooter(boolean hasFooter) {
            if (mHasFooter != hasFooter) {
                mHasFooter = hasFooter;
                if (hasFooter) {
                    notifyItemInserted(mInnerAdapter.getItemCount());
                } else {
                    notifyItemRemoved(mInnerAdapter.getItemCount());
                }
            }
        }
        
        private class FooterViewHolder extends ViewHolder {
            public FooterViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
    
    // NestedScrollingChild2 实现
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        if (mChildHelper != null) {
            mChildHelper.setNestedScrollingEnabled(enabled);
        }
    }
    
    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper != null && mChildHelper.isNestedScrollingEnabled();
    }
    
    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mChildHelper != null && mChildHelper.startNestedScroll(axes, type);
    }
    
    @Override
    public void stopNestedScroll(int type) {
        if (mChildHelper != null) {
            mChildHelper.stopNestedScroll(type);
        }
    }
    
    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper != null && mChildHelper.hasNestedScrollingParent(type);
    }
    
    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type) {
        return mChildHelper != null && mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }
    
    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
        return mChildHelper != null && mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }
    
    // NestedScrollingParent2 实现
    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && mEnablePullRefresh && mRefreshHeader != null;
    }
    
    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);
    }
    
    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mParentHelper.onStopNestedScroll(target, type);
        if (mCurrentState == STATE_RELEASE_TO_REFRESH) {
            startRefresh();
        } else if (mCurrentState == STATE_PULL_DOWN) {
            resetRefreshState();
        }
    }
    
    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        // 处理未消耗的滚动
    }
    
    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (dy > 0 && !canScrollVertically(-1) && mEnablePullRefresh && mCurrentState != STATE_REFRESHING) {
            // 确保头部视图已初始化
            ensureRefreshHeader(getContext());
            
            if (mRefreshHeader == null) {
                return;
            }
            
            // 确保头部高度已测量
            if (mHeaderHeight == 0) {
                View headerView = mRefreshHeader.getHeaderView();
                if (headerView != null) {
                    int width = getWidth() > 0 ? getWidth() : getMeasuredWidth();
                    if (width > 0) {
                        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                        int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                        headerView.measure(widthSpec, heightSpec);
                        mHeaderHeight = headerView.getMeasuredHeight();
                    }
                }
            }
            
            if (mHeaderHeight == 0) {
                return;
            }
            
            float pullDistance = Math.min(dy, mHeaderHeight * 2.0f);
            mCurrentHeaderOffset = (int) pullDistance;
            setPadding(0, mCurrentHeaderOffset, 0, 0);
            
            // 布局头部视图，确保它可见
            if (mCurrentHeaderOffset > 0) {
                layoutHeaderView();
            }
            
            float progress = pullDistance / mHeaderHeight;
            
            if (pullDistance > mHeaderHeight) {
                if (mCurrentState != STATE_RELEASE_TO_REFRESH) {
                    mCurrentState = STATE_RELEASE_TO_REFRESH;
                    mRefreshHeader.onReleaseToRefresh();
                }
            } else {
                if (mCurrentState != STATE_PULL_DOWN) {
                    mCurrentState = STATE_PULL_DOWN;
                }
                mRefreshHeader.onPullDown(progress);
            }
            consumed[1] = dy;
            invalidate();
            requestLayout();
        }
    }
}
