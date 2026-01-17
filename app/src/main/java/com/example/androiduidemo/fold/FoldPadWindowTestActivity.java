package com.example.androiduidemo.fold;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androiduidemo.R;
import com.example.foldwindow.FoldPadWindowLayout;

public class FoldPadWindowTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fold_pad_window_test);

        FoldPadWindowLayout layout = findViewById(R.id.fold_pad_window);
        layout.setContentView(buildDemoContent());
        layout.setSidebarContent(buildSidebarContent());
    }

    private LinearLayout buildDemoContent() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.parseColor("#1F1F1F"));
        container.setGravity(Gravity.CENTER);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView title = new TextView(this);
        title.setText("Fold / Pad Demo");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        title.setTextColor(Color.parseColor("#FFFFFF"));
        title.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.gravity = Gravity.CENTER_HORIZONTAL;
        title.setLayoutParams(titleParams);
        container.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Tap MINI to shrink, FULL to restore");
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        sub.setTextColor(Color.parseColor("#BBBBBB"));
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subParams.topMargin = dpToPx(8);
        sub.setLayoutParams(subParams);
        container.addView(sub);

        return container;
    }

    private LinearLayout buildSidebarContent() {
        LinearLayout sidebar = new LinearLayout(this);
        sidebar.setOrientation(LinearLayout.VERTICAL);
        sidebar.setBackgroundColor(Color.parseColor("#F2F2F2"));
        sidebar.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(24));
        sidebar.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        sidebar.addView(buildSidebarItem("Menu A"));
        sidebar.addView(buildSidebarItem("Menu B"));
        sidebar.addView(buildSidebarItem("Menu C"));
        return sidebar;
    }

    private TextView buildSidebarItem(String text) {
        TextView item = new TextView(this);
        item.setText(text);
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        item.setTextColor(Color.parseColor("#333333"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(12);
        item.setLayoutParams(params);
        return item;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
