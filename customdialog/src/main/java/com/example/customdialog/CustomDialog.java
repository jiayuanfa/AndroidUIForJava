package com.example.customdialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class CustomDialog extends Dialog {
    private String title;
    private String message;
    private String positiveText;
    private String negativeText;
    private Dialog.OnClickListener positiveListener;
    private Dialog.OnClickListener negativeListener;
    private boolean cancelable = true;

    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    public CustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);
        
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }

        setCancelable(cancelable);
        setCanceledOnTouchOutside(cancelable);

        TextView titleView = findViewById(R.id.tv_title);
        TextView messageView = findViewById(R.id.tv_message);
        Button positiveButton = findViewById(R.id.btn_positive);
        Button negativeButton = findViewById(R.id.btn_negative);

        if (title != null && !title.isEmpty()) {
            titleView.setText(title);
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }

        if (message != null && !message.isEmpty()) {
            messageView.setText(message);
            messageView.setVisibility(View.VISIBLE);
        } else {
            messageView.setVisibility(View.GONE);
        }

        if (positiveText != null && !positiveText.isEmpty()) {
            positiveButton.setText(positiveText);
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setOnClickListener(v -> {
                if (positiveListener != null) {
                    positiveListener.onClick(CustomDialog.this, BUTTON_POSITIVE);
                }
                dismiss();
            });
        } else {
            positiveButton.setVisibility(View.GONE);
        }

        if (negativeText != null && !negativeText.isEmpty()) {
            negativeButton.setText(negativeText);
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setOnClickListener(v -> {
                if (negativeListener != null) {
                    negativeListener.onClick(CustomDialog.this, BUTTON_NEGATIVE);
                }
                dismiss();
            });
        } else {
            negativeButton.setVisibility(View.GONE);
        }
    }

    public CustomDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public CustomDialog setPositiveButton(String text, Dialog.OnClickListener listener) {
        this.positiveText = text;
        this.positiveListener = listener;
        return this;
    }

    public CustomDialog setNegativeButton(String text, Dialog.OnClickListener listener) {
        this.negativeText = text;
        this.negativeListener = listener;
        return this;
    }

    public CustomDialog setDialogCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        super.setCancelable(cancelable);
        return this;
    }
}

