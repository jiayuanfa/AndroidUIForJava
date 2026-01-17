package com.example.androiduidemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.loading.Loading;
import com.example.loading.LoadingManager;

public class LoadingTestActivity extends AppCompatActivity {

    private Button btnActivityShow;
    private Button btnActivityHide;
    private Button btnActivityAutoHide;
    private Button btnFragmentShow;
    private Button btnFragmentHide;
    private Button btnViewShow;
    private Button btnViewHide;

    private FrameLayout fragmentContainer;
    private FrameLayout viewTestContainer;

    private TestFragment testFragment;
    private LoadingManager viewLoadingManager;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_test);

        initViews();
        setupFragment();
        setupListeners();
    }

    private void initViews() {
        btnActivityShow = findViewById(R.id.btn_activity_show);
        btnActivityHide = findViewById(R.id.btn_activity_hide);
        btnActivityAutoHide = findViewById(R.id.btn_activity_auto_hide);
        btnFragmentShow = findViewById(R.id.btn_fragment_show);
        btnFragmentHide = findViewById(R.id.btn_fragment_hide);
        btnViewShow = findViewById(R.id.btn_view_show);
        btnViewHide = findViewById(R.id.btn_view_hide);
        fragmentContainer = findViewById(R.id.fragment_container);
        viewTestContainer = findViewById(R.id.view_test_container);
    }

    private void setupFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        testFragment = new TestFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, testFragment);
        transaction.commit();
    }

    private void setupListeners() {
        // Activity中显示Loading
        btnActivityShow.setOnClickListener(v -> {
            Loading.show(this, "Activity加载中...");
        });

        // Activity中隐藏Loading
        btnActivityHide.setOnClickListener(v -> {
            Loading.hide();
        });

        // Activity中显示Loading，3秒后自动隐藏
        btnActivityAutoHide.setOnClickListener(v -> {
            Loading.show(this, "3秒后自动隐藏");
            handler.postDelayed(() -> {
                Loading.hide();
            }, 3000);
        });

        // Fragment中显示Loading
        btnFragmentShow.setOnClickListener(v -> {
            if (testFragment != null) {
                testFragment.showLoading();
            }
        });

        // Fragment中隐藏Loading
        btnFragmentHide.setOnClickListener(v -> {
            if (testFragment != null) {
                testFragment.hideLoading();
            }
        });

        // View/ViewGroup中显示Loading
        btnViewShow.setOnClickListener(v -> {
            if (viewLoadingManager == null) {
                viewLoadingManager = LoadingManager.with(viewTestContainer);
            }
            viewLoadingManager.show("View加载中...");
        });

        // View/ViewGroup中隐藏Loading
        btnViewHide.setOnClickListener(v -> {
            if (viewLoadingManager != null) {
                viewLoadingManager.hide();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        handler.removeCallbacksAndMessages(null);
        if (viewLoadingManager != null) {
            viewLoadingManager.release();
            viewLoadingManager = null;
        }
        Loading.release();
    }
}

