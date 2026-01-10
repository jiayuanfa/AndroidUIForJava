package com.example.androiduidemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.refreshrecyclerview.OnLoadMoreListener;
import com.example.refreshrecyclerview.OnRefreshListener;
import com.example.refreshrecyclerview.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RefreshRecyclerViewTestActivity extends AppCompatActivity {
    
    private RefreshRecyclerView mRefreshRecyclerView;
    private TestAdapter mAdapter;
    private List<String> mDataList;
    private int mPage = 1;
    private Handler mHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_recycler_view_test);
        
        mHandler = new Handler(Looper.getMainLooper());
        
        initToolbar();
        initViews();
        initData();
        setupListeners();
    }
    
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("RefreshRecyclerView测试");
            toolbar.setNavigationOnClickListener(v -> finish());
            // 如果需要显示返回箭头，可以设置导航图标
             toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        }
    }
    
    private void initViews() {
        mRefreshRecyclerView = findViewById(R.id.refresh_recycler_view);
        mRefreshRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        mDataList = new ArrayList<>();
        mAdapter = new TestAdapter(mDataList);
        mRefreshRecyclerView.setAdapter(mAdapter);
    }
    
    private void initData() {
        // 初始化第一页数据
        loadData(1, false);
    }
    
    private void setupListeners() {
        // 设置下拉刷新监听
        mRefreshRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 模拟网络请求
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPage = 1;
                        mDataList.clear();
                        loadData(mPage, false);
                        mRefreshRecyclerView.finishRefresh();
                        Toast.makeText(RefreshRecyclerViewTestActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                    }
                }, 1500); // 延迟1.5秒模拟网络请求
            }
        });
        
        // 设置上拉加载更多监听
        mRefreshRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                // 模拟网络请求
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPage++;
                        boolean hasMore = mPage <= 5; // 模拟只有5页数据
                        loadData(mPage, true);
                        mRefreshRecyclerView.finishLoadMore(hasMore);
                        if (hasMore) {
                            Toast.makeText(RefreshRecyclerViewTestActivity.this, "加载第" + mPage + "页成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RefreshRecyclerViewTestActivity.this, "没有更多数据了", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 1500); // 延迟1.5秒模拟网络请求
            }
        });
    }
    
    /**
     * 加载数据
     * @param page 页码
     * @param append 是否追加数据
     */
    private void loadData(int page, boolean append) {
        int startIndex = mDataList.size();
        int count = 20; // 每页20条数据
        
        for (int i = 0; i < count; i++) {
            int index = startIndex + i + 1;
            mDataList.add("第" + page + "页 - 项目 " + index);
        }
        
        if (append) {
            mAdapter.notifyItemRangeInserted(startIndex, count);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
}
