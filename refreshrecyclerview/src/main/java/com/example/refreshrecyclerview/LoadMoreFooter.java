package com.example.refreshrecyclerview;

import android.view.View;

/**
 * 上拉加载更多底部视图接口
 */
public interface LoadMoreFooter {
    
    /**
     * 获取底部视图
     */
    View getFooterView();
    
    /**
     * 正在加载更多
     */
    void onLoading();
    
    /**
     * 加载完成
     */
    void onLoadComplete();
    
    /**
     * 没有更多数据
     */
    void onNoMoreData();
}
