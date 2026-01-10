package com.example.refreshrecyclerview;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 默认的加载更多底部视图实现
 */
public class DefaultLoadMoreFooter implements LoadMoreFooter {
    
    private LinearLayout mFooterView;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private Context mContext;
    
    public DefaultLoadMoreFooter(Context context) {
        mContext = context;
        init();
    }
    
    private void init() {
        mFooterView = new LinearLayout(mContext);
        mFooterView.setOrientation(LinearLayout.HORIZONTAL);
        mFooterView.setGravity(Gravity.CENTER);
        mFooterView.setPadding(0, 40, 0, 40);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mFooterView.setLayoutParams(params);
        
        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleSmall);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.setMargins(0, 0, 20, 0);
        mProgressBar.setLayoutParams(progressParams);
        mProgressBar.setVisibility(View.GONE);
        
        mTextView = new TextView(mContext);
        mTextView.setText("上拉加载更多");
        mTextView.setTextSize(14);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mTextView.setLayoutParams(textParams);
        
        mFooterView.addView(mProgressBar);
        mFooterView.addView(mTextView);
    }
    
    @Override
    public View getFooterView() {
        return mFooterView;
    }
    
    @Override
    public void onLoading() {
        if (mTextView != null) {
            mTextView.setText("正在加载更多...");
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onLoadComplete() {
        if (mTextView != null) {
            mTextView.setText("上拉加载更多");
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onNoMoreData() {
        if (mTextView != null) {
            mTextView.setText("没有更多数据了");
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
