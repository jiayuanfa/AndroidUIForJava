package com.example.searchbox;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class CustomSearchBox extends LinearLayout {
    private EditText editText;
    private ImageButton clearButton;
    private ImageButton searchButton;
    private OnSearchListener searchListener;
    private OnTextChangedListener textChangedListener;

    public CustomSearchBox(Context context) {
        super(context);
        init(context);
    }

    public CustomSearchBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomSearchBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.search_box, this, true);
        editText = findViewById(R.id.et_search);
        clearButton = findViewById(R.id.btn_clear);
        searchButton = findViewById(R.id.btn_search);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearButton.setVisibility(VISIBLE);
                } else {
                    clearButton.setVisibility(GONE);
                }
                if (textChangedListener != null) {
                    textChangedListener.onTextChanged(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        clearButton.setOnClickListener(v -> {
            editText.setText("");
            clearButton.setVisibility(GONE);
        });

        searchButton.setOnClickListener(v -> {
            if (searchListener != null) {
                searchListener.onSearch(editText.getText().toString());
            }
        });

        clearButton.setVisibility(GONE);
    }

    public void setHint(String hint) {
        editText.setHint(hint);
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setText(String text) {
        editText.setText(text);
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.searchListener = listener;
    }

    public void setOnTextChangedListener(OnTextChangedListener listener) {
        this.textChangedListener = listener;
    }

    public interface OnSearchListener {
        void onSearch(String keyword);
    }

    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }
}

