package com.example.datepicker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

import java.util.Calendar;

public class CustomDatePicker {
    private Context context;
    private OnDateSelectedListener listener;
    private Calendar calendar;
    private int year, month, dayOfMonth;

    public CustomDatePicker(Context context) {
        this.context = context;
        this.calendar = Calendar.getInstance();
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH);
        this.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public CustomDatePicker setInitialDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    public CustomDatePicker setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
        return this;
    }

    public void show() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            context,
            (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                if (listener != null) {
                    listener.onDateSelected(selectedYear, selectedMonth + 1, selectedDayOfMonth);
                }
            },
            year, month, dayOfMonth
        );
        datePickerDialog.show();
    }

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int dayOfMonth);
    }
}

