package com.example.androiduidemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.progressbar.CustomProgressBar;

public class AccessibilityTestActivity extends AppCompatActivity {

    private CustomProgressBar progressBarDefault;
    private CustomProgressBar progressBarCustomDesc;
    private ProgressBar standardProgressBar;
    private Button btnUpdateProgress;

    private Handler handler = new Handler(Looper.getMainLooper());
    private float currentProgress = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility_test);

        initViews();
        setupListeners();
    }

    private void initViews() {
        progressBarDefault = findViewById(R.id.progress_bar_default);
        progressBarCustomDesc = findViewById(R.id.progress_bar_custom_desc);
        standardProgressBar = findViewById(R.id.standard_progress_bar);
        btnUpdateProgress = findViewById(R.id.btn_update_progress);

        // 设置自定义无障碍描述
        progressBarCustomDesc.setAccessibilityContentDescription("文件下载进度，当前75%");
    }

    private void setupListeners() {
        btnUpdateProgress.setOnClickListener(v -> {
            // 更新进度，测试无障碍事件
            currentProgress += 0.1f;
            if (currentProgress > 1f) {
                currentProgress = 0f;
            }

            // 更新自定义进度条
            progressBarDefault.setProgress(currentProgress, true);
            
            // 更新自定义描述的进度条
            int progressPercent = (int) (currentProgress * 100);
            progressBarCustomDesc.setProgress(currentProgress, true);
            progressBarCustomDesc.setAccessibilityContentDescription(
                String.format("文件下载进度，当前%d%%", progressPercent)
            );

            // 更新标准进度条
            standardProgressBar.setProgress((int) (currentProgress * 100));
            standardProgressBar.setContentDescription(
                String.format("标准进度条，当前进度%d%%", progressPercent)
            );
        });
    }
}

