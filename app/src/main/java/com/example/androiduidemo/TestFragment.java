package com.example.androiduidemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.loading.Loading;

public class TestFragment extends Fragment {

    private Button btnFragmentShow;
    private Button btnFragmentHide;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Fragment中的按钮在Activity中控制，这里不需要初始化
        // 但可以在这里添加Fragment内部的测试逻辑
    }

    public void showLoading() {
        Loading.show(this, "Fragment加载中...");
    }

    public void hideLoading() {
        Loading.hide();
    }
}

