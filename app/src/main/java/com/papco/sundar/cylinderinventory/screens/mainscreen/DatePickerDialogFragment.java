package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.papco.sundar.cylinderinventory.common.Msg;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Calendar calendar=Calendar.getInstance();
        int date=calendar.get(Calendar.DATE);
        int month=calendar.get(Calendar.MONTH);
        int year=calendar.get(Calendar.YEAR);

        DatePickerDialog dialog= new DatePickerDialog(getActivity(),(MainActivity)getActivity(),year,month,date);
        dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis()-86400000);
        return dialog;
    }

}
