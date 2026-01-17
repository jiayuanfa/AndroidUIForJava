package com.example.androiduidemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customdialog.CustomDialog;
import com.example.customtoast.CustomToast;
import com.example.datepicker.CustomDatePicker;
import com.example.searchbox.CustomSearchBox;

public class MainActivity extends AppCompatActivity {

    private CustomSearchBox searchBox;
    private Button btnDialog;
    private Button btnToastSuccess;
    private Button btnToastError;
    private Button btnToastInfo;
    private Button btnToastWarning;
    private Button btnDatePicker;
    private Button btnRefreshRecyclerView;
    private Button btnFoldPadWindow;
    private TextView tvSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
    }

    private void initViews() {
        searchBox = findViewById(R.id.search_box);
        btnDialog = findViewById(R.id.btn_dialog);
        btnToastSuccess = findViewById(R.id.btn_toast_success);
        btnToastError = findViewById(R.id.btn_toast_error);
        btnToastInfo = findViewById(R.id.btn_toast_info);
        btnToastWarning = findViewById(R.id.btn_toast_warning);
        btnDatePicker = findViewById(R.id.btn_date_picker);
        btnRefreshRecyclerView = findViewById(R.id.btn_refresh_recycler_view);
        btnFoldPadWindow = findViewById(R.id.btn_fold_pad_window);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
    }

    private void setupListeners() {
        // 自定义搜索框
        searchBox.setOnSearchListener(keyword -> {
            if (keyword.isEmpty()) {
                CustomToast.showInfo(this, "请输入搜索关键词");
            } else {
                CustomToast.showSuccess(this, "搜索: " + keyword);
            }
        });

        searchBox.setOnTextChangedListener(text -> {
            // 可以在这里实现实时搜索建议等功能
        });

        // 自定义弹窗
        btnDialog.setOnClickListener(v -> {
            new CustomDialog(this)
                    .setTitle("提示")
                    .setMessage("这是一个自定义弹窗，您确定要继续吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        CustomToast.showSuccess(this, "您点击了确定");
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        CustomToast.showInfo(this, "您点击了取消");
                    })
                    .setDialogCancelable(true)
                    .show();
        });

        // 自定义Toast - 成功
        btnToastSuccess.setOnClickListener(v -> {
            CustomToast.showSuccess(this, "操作成功！");
        });

        // 自定义Toast - 错误
        btnToastError.setOnClickListener(v -> {
            CustomToast.showError(this, "操作失败！");
        });

        // 自定义Toast - 信息
        btnToastInfo.setOnClickListener(v -> {
            CustomToast.showInfo(this, "这是一条信息提示");
        });

        // 自定义Toast - 警告
        btnToastWarning.setOnClickListener(v -> {
            CustomToast.showWarning(this, "请注意！");
        });

        // 自定义日期选择器
        btnDatePicker.setOnClickListener(v -> {
            new CustomDatePicker(this)
                    .setOnDateSelectedListener((year, month, dayOfMonth) -> {
                        String date = String.format("%d年%d月%d日", year, month, dayOfMonth);
                        tvSelectedDate.setText("选择的日期: " + date);
                        CustomToast.showSuccess(this, "已选择日期: " + date);
                    })
                    .show();
        });
        
        // 刷新RecyclerView测试
        btnRefreshRecyclerView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RefreshRecyclerViewTestActivity.class);
            startActivity(intent);
        });

        // 折叠屏/Pad 小窗组件测试
        btnFoldPadWindow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,
                    com.example.androiduidemo.fold.FoldPadWindowTestActivity.class);
            startActivity(intent);
        });
    }
}

