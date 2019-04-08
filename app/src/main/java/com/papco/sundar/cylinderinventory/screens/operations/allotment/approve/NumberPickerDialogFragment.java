package com.papco.sundar.cylinderinventory.screens.operations.allotment.approve;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import com.papco.sundar.cylinderinventory.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class NumberPickerDialogFragment extends DialogFragment {

    private static final String KEY_MAX_VALUE="max_picker_value";
    private static final String KEY_INITIAL_VALUE ="current_picker_value";
    private static final String KEY_EDITING_POSITION="current_editing_position";

    public static Bundle getStartingArgs(int initialValue,int maxValue,int position){

        Bundle args=new Bundle();
        args.putInt(KEY_MAX_VALUE,maxValue);
        args.putInt(KEY_INITIAL_VALUE,initialValue);
        args.putInt(KEY_EDITING_POSITION,position);

        return args;

    }

    private NumberPicker numberPicker;
    private Button btnSave;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        View view=getActivity().getLayoutInflater().inflate(R.layout.approval_picker_fragment,null);
        linkViews(view);
        initViews(view,savedInstanceState);
        builder.setView(view);
        return builder.create();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("spinner_value",numberPicker.getValue());
    }

    private void linkViews(View view) {
        numberPicker=view.findViewById(R.id.number_picker);
        btnSave=view.findViewById(R.id.btn_save);
    }

    private void initViews(View view,Bundle savedInstanceState) {
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(getMaxValue());
        if(savedInstanceState==null)
            numberPicker.setValue(getInitialValue());
        else
            numberPicker.setValue(savedInstanceState.getInt("spinner_value"));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveChanges();
            }
        });
    }

    private void onSaveChanges(){
        int selectedValue=numberPicker.getValue();
        ((ApproveAllotmentActivity)getActivity()).updateData(getEditingPosition(),selectedValue);
        getDialog().dismiss();
    }


    private int getMaxValue(){

        Bundle args=getArguments();
        if(args==null)
            return -1;

        return args.getInt(KEY_MAX_VALUE,-1);
    }

    private int getInitialValue(){

        Bundle args=getArguments();
        if(args==null)
            return -1;

        return args.getInt(KEY_INITIAL_VALUE,-1);
    }

    private int getEditingPosition(){

        Bundle args=getArguments();
        if(args==null)
            return -1;

        return args.getInt(KEY_EDITING_POSITION,-1);
    }

}
