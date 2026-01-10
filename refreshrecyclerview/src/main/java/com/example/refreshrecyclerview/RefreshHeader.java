package com.example.refreshrecyclerview;

import android.view.View;

/**
 * 下拉刷新头部视图接口
 */
public interface RefreshHeader {
    
    /**
     * 获取头部视图
     */
    View getHeaderView();
    
    /**
     * 下拉中
     * @param progress 下拉进度，0-1之间
     */
    void onPullDown(float progress);
    
    /**
     * 释放刷新
     */
    void onReleaseToRefresh();
    
    /**
     * 正在刷新
     */
    void onRefreshing();
    
    /**
     * 刷新完成
     */
    void onRefreshComplete();
    
    /**
     * 空闲状态（重置到初始状态）
     */
    void onIdle();
}
