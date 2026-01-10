package com.example.customtoast;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_INFO = 2;
    public static final int TYPE_WARNING = 3;

    public static Toast makeText(Context context, String message, int duration) {
        return makeText(context, message, duration, TYPE_INFO);
    }

    public static Toast makeText(Context context, String message, int duration, int type) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_custom, null);

        TextView textView = layout.findViewById(R.id.tv_message);
        textView.setText(message);

        // 根据类型设置不同的背景颜色
        View container = layout.findViewById(R.id.toast_container);
        switch (type) {
            case TYPE_SUCCESS:
                container.setBackgroundResource(R.drawable.toast_success_bg);
                break;
            case TYPE_ERROR:
                container.setBackgroundResource(R.drawable.toast_error_bg);
                break;
            case TYPE_WARNING:
                container.setBackgroundResource(R.drawable.toast_warning_bg);
                break;
            case TYPE_INFO:
            default:
                container.setBackgroundResource(R.drawable.toast_info_bg);
                break;
        }

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(layout);
        return toast;
    }

    public static void showSuccess(Context context, String message) {
        makeText(context, message, LENGTH_SHORT, TYPE_SUCCESS).show();
    }

    public static void showError(Context context, String message) {
        makeText(context, message, LENGTH_SHORT, TYPE_ERROR).show();
    }

    public static void showInfo(Context context, String message) {
        makeText(context, message, LENGTH_SHORT, TYPE_INFO).show();
    }

    public static void showWarning(Context context, String message) {
        makeText(context, message, LENGTH_SHORT, TYPE_WARNING).show();
    }
}

