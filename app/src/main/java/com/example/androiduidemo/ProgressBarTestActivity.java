package com.example.androiduidemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.progressbar.CustomProgressBar;

public class ProgressBarTestActivity extends AppCompatActivity {

    private CustomProgressBar progressBar1;
    private CustomProgressBar progressBar2;
    private CustomProgressBar progressBar3;
    private CustomProgressBar progressBar4;
    private CustomProgressBar progressBar5;
    private CustomProgressBar progressBarCircular;

    private Button btnProgress0;
    private Button btnProgress25;
    private Button btnProgress50;
    private Button btnProgress100;
    private Button btnAnimateProgress;
    private Button btnNoAnimate;
    private Button btnFastAnimate;
    private Button btnSlowAnimate;
    private Button btnContinuousAnimate;
    private Button btnSwitchMode;
    private Button btnCircularAnimate;

    private TextView tvProgressValue;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progressbar_test);

        initViews();
        setupListeners();
    }

    private void initViews() {
        progressBar1 = findViewById(R.id.progress_bar_1);
        progressBar2 = findViewById(R.id.progress_bar_2);
        progressBar3 = findViewById(R.id.progress_bar_3);
        progressBar4 = findViewById(R.id.progress_bar_4);
        progressBar5 = findViewById(R.id.progress_bar_5);
        progressBarCircular = findViewById(R.id.progress_bar_circular);

        btnProgress0 = findViewById(R.id.btn_progress_0);
        btnProgress25 = findViewById(R.id.btn_progress_25);
        btnProgress50 = findViewById(R.id.btn_progress_50);
        btnProgress100 = findViewById(R.id.btn_progress_100);
        btnAnimateProgress = findViewById(R.id.btn_animate_progress);
        btnNoAnimate = findViewById(R.id.btn_no_animate);
        btnFastAnimate = findViewById(R.id.btn_fast_animate);
        btnSlowAnimate = findViewById(R.id.btn_slow_animate);
        btnContinuousAnimate = findViewById(R.id.btn_continuous_animate);
        btnSwitchMode = findViewById(R.id.btn_switch_mode);
        btnCircularAnimate = findViewById(R.id.btn_circular_animate);

        tvProgressValue = findViewById(R.id.tv_progress_value);
        
        // 注意：环形进度条的模式和进度已经在XML中设置了
        // 这里不需要再次设置，除非需要修改

        // 添加进度监听（通过定时更新显示）
        startProgressMonitor();
    }

    private void setupListeners() {
        // 基础测试 - 设置不同进度值
        btnProgress0.setOnClickListener(v -> {
            progressBar1.setProgress(0f);
            updateProgressValue(progressBar1);
        });

        btnProgress25.setOnClickListener(v -> {
            progressBar1.setProgress(0.25f);
            updateProgressValue(progressBar1);
        });

        btnProgress50.setOnClickListener(v -> {
            progressBar1.setProgress(0.5f);
            updateProgressValue(progressBar1);
        });

        btnProgress100.setOnClickListener(v -> {
            progressBar1.setProgress(1f);
            updateProgressValue(progressBar1);
        });

        // 动画测试 - 从0到100%
        btnAnimateProgress.setOnClickListener(v -> {
            progressBar2.setProgress(0f, false); // 先无动画设置到0
            handler.postDelayed(() -> {
                progressBar2.setProgress(1f, true); // 然后动画到100%
                updateProgressValue(progressBar2);
            }, 100);
        });

        // 无动画测试
        btnNoAnimate.setOnClickListener(v -> {
            progressBar3.setProgress(0.5f, false);
            updateProgressValue(progressBar3);
        });

        // 快速动画测试
        btnFastAnimate.setOnClickListener(v -> {
            progressBar4.setAnimationDuration(200);
            progressBar4.setProgress(0f, false);
            handler.postDelayed(() -> {
                progressBar4.setProgress(1f, true);
                updateProgressValue(progressBar4);
            }, 100);
        });

        // 慢速动画测试
        btnSlowAnimate.setOnClickListener(v -> {
            progressBar4.setAnimationDuration(2000);
            progressBar4.setProgress(0f, false);
            handler.postDelayed(() -> {
                progressBar4.setProgress(1f, true);
                updateProgressValue(progressBar4);
            }, 100);
        });

        // 连续动画测试
        btnContinuousAnimate.setOnClickListener(v -> {
            progressBar5.setProgress(0f, false);
            handler.postDelayed(() -> progressBar5.setProgress(0.25f, true), 100);
            handler.postDelayed(() -> progressBar5.setProgress(0.5f, true), 700);
            handler.postDelayed(() -> progressBar5.setProgress(0.75f, true), 1300);
            handler.postDelayed(() -> {
                progressBar5.setProgress(1f, true);
                updateProgressValue(progressBar5);
            }, 1900);
        });

        // 切换模式
        btnSwitchMode.setOnClickListener(v -> {
            int currentMode = progressBarCircular.getProgressMode();
            if (currentMode == CustomProgressBar.MODE_LINEAR) {
                progressBarCircular.setProgressMode(CustomProgressBar.MODE_CIRCULAR);
                btnSwitchMode.setText("切换到线性");
            } else {
                progressBarCircular.setProgressMode(CustomProgressBar.MODE_LINEAR);
                btnSwitchMode.setText("切换到环形");
            }
            updateProgressValue(progressBarCircular);
        });

        // 环形动画测试
        btnCircularAnimate.setOnClickListener(v -> {
            progressBarCircular.setProgress(0f, false);
            handler.postDelayed(() -> {
                progressBarCircular.setProgress(1f, true);
                updateProgressValue(progressBarCircular);
            }, 100);
        });
    }

    /**
     * 更新进度值显示
     */
    private void updateProgressValue(CustomProgressBar progressBar) {
        float progress = progressBar.getProgress();
        tvProgressValue.setText(String.format("%.2f (%.0f%%)", progress, progress * 100));
    }

    /**
     * 开始进度监控（定时更新显示）
     */
    private void startProgressMonitor() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // 显示第一个进度条的当前值
                if (progressBar1 != null) {
                    updateProgressValue(progressBar1);
                }
                handler.postDelayed(this, 100); // 每100ms更新一次
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}

