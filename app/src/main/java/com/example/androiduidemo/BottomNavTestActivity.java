package com.example.androiduidemo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bottomnav.BottomNavBar;
import com.example.bottomnav.BottomNavItem;

import java.util.ArrayList;
import java.util.List;

public class BottomNavTestActivity extends AppCompatActivity {

    private TextView contentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav_test);

        contentText = findViewById(R.id.tv_nav_content);
        BottomNavBar bottomNav = findViewById(R.id.bottom_nav);

        List<BottomNavItem> items = new ArrayList<>();
        items.add(new BottomNavItem("首页", R.drawable.avd_nav_home));
        items.add(new BottomNavItem("发现", R.drawable.avd_nav_discover));
        items.add(new BottomNavItem("消息", R.drawable.avd_nav_message));
        items.add(new BottomNavItem("我的", R.drawable.avd_nav_profile));
        bottomNav.setItems(items);
        bottomNav.setSelectedIndex(0);
        bottomNav.setSelectedColor(Color.parseColor("#1F1F1F"));
        bottomNav.setUnselectedColor(Color.parseColor("#888888"));
        bottomNav.setIndicatorColor(Color.parseColor("#1F1F1F"));
        bottomNav.setTextSizeSp(12f);
        bottomNav.setOnItemSelectedListener(new BottomNavBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index, BottomNavItem item) {
                contentText.setText("当前选中：" + item.getTitle());
            }

            @Override
            public void onItemReselected(int index, BottomNavItem item) {
                contentText.setText("重复点击：" + item.getTitle());
            }
        });
    }
}
