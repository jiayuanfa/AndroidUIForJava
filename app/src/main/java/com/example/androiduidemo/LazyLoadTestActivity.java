package com.example.androiduidemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lazyloadview.LazyLoadCardView;

/**
 * ViewStub懒加载测试Activity
 * 
 * 演示三种懒加载场景：
 * 1. 使用ViewStub的inflate()方法加载XML布局
 * 2. 使用代码动态创建布局并加载
 * 3. 完整加载示例（内容+操作区域）
 */
public class LazyLoadTestActivity extends AppCompatActivity {
    private static final String TAG = "LazyLoadTest";
    
    private LazyLoadCardView cardView1;
    private LazyLoadCardView cardView2;
    private LazyLoadCardView cardView3;
    private TextView tvStatus;
    private Button btnLoadContent;
    private Button btnLoadActions;
    private Button btnLoadFromCode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lazyload_test);
        
        initViews();
        setupListeners();
        setupCards();
    }
    
    private void initViews() {
        cardView1 = findViewById(R.id.card_view_1);
        cardView2 = findViewById(R.id.card_view_2);
        cardView3 = findViewById(R.id.card_view_3);
        tvStatus = findViewById(R.id.tv_status);
        btnLoadContent = findViewById(R.id.btn_load_content);
        btnLoadActions = findViewById(R.id.btn_load_actions);
        btnLoadFromCode = findViewById(R.id.btn_load_from_code);
    }
    
    private void setupListeners() {
        // 加载内容区域
        btnLoadContent.setOnClickListener(v -> {
            Log.d(TAG, "点击加载内容区域");
            View contentView = cardView1.loadContent();
            if (contentView != null) {
                updateStatus("卡片1：内容区域已加载");
                Toast.makeText(this, "内容区域加载成功", Toast.LENGTH_SHORT).show();
            } else {
                updateStatus("卡片1：内容区域加载失败");
                Toast.makeText(this, "内容区域加载失败", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 加载操作区域
        btnLoadActions.setOnClickListener(v -> {
            Log.d(TAG, "点击加载操作区域");
            View actionsView = cardView1.loadActions();
            if (actionsView != null) {
                updateStatus("卡片1：操作区域已加载");
                Toast.makeText(this, "操作区域加载成功", Toast.LENGTH_SHORT).show();
                
                // 设置操作按钮的点击事件
                setupActionButtons(actionsView);
            } else {
                updateStatus("卡片1：操作区域加载失败");
                Toast.makeText(this, "操作区域加载失败", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 代码动态加载布局
        btnLoadFromCode.setOnClickListener(v -> {
            Log.d(TAG, "点击代码动态加载布局");
            View contentView = cardView2.loadContentFromCode(com.example.lazyloadview.R.layout.card_content_layout);
            if (contentView != null) {
                updateStatus("卡片2：代码动态加载成功");
                Toast.makeText(this, "代码动态加载成功", Toast.LENGTH_SHORT).show();
            } else {
                updateStatus("卡片2：代码动态加载失败");
                Toast.makeText(this, "代码动态加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupCards() {
        // 设置卡片1的标题
        cardView1.setTitle("测试卡片1 - ViewStub懒加载");
        
        // 设置卡片2的标题
        cardView2.setTitle("测试卡片2 - 代码动态加载");
        
        // 设置卡片3的标题，并完整加载
        cardView3.setTitle("测试卡片3 - 完整加载示例");
        // 延迟加载内容区域（模拟实际场景）
        cardView3.postDelayed(() -> {
            cardView3.loadContent();
            updateStatus("卡片3：内容区域已自动加载");
        }, 1000);
        // 延迟加载操作区域
        cardView3.postDelayed(() -> {
            View actionsView = cardView3.loadActions();
            if (actionsView != null) {
                setupActionButtons(actionsView);
                updateStatus("卡片3：操作区域已自动加载");
            }
        }, 2000);
    }
    
    /**
     * 设置操作按钮的点击事件
     */
    private void setupActionButtons(View actionsView) {
        View btnCancel = actionsView.findViewById(com.example.lazyloadview.R.id.btn_cancel);
        View btnConfirm = actionsView.findViewById(com.example.lazyloadview.R.id.btn_confirm);
        
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                Toast.makeText(this, "点击了取消按钮", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "点击取消按钮");
            });
        }
        
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                Toast.makeText(this, "点击了确认按钮", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "点击确认按钮");
            });
        }
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatus(String status) {
        if (tvStatus != null) {
            tvStatus.setText("状态：" + status);
            Log.d(TAG, "状态更新: " + status);
        }
    }
}

