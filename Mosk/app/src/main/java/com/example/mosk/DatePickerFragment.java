package com.example.mosk;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "Log";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        if (MainActivity.dateset == false){
            MainActivity.year = c.get(Calendar.YEAR);
            MainActivity.month = c.get(Calendar.MONTH);
            MainActivity.day = c.get(Calendar.DAY_OF_MONTH);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),this,MainActivity.year,MainActivity.month,MainActivity.day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - (24 * 60 * 60 * 1000) * 14); // 최소 2주 전 까지 날짜 선택 가능
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // 최대 오늘까지 날짜 선택 가능

        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
        MainActivity activity = (MainActivity)getActivity();
        activity.processDatePickerResult(y,m,d);
        MainActivity.year = y;
        MainActivity.month = m;
        MainActivity.day = d;
        Log.d(TAG, "onDateSet : "+MainActivity.day);
        MainActivity.dateset = true;
    }
}