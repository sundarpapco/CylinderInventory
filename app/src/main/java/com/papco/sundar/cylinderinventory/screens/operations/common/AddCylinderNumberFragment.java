package com.papco.sundar.cylinderinventory.screens.operations.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.R;

public class AddCylinderNumberFragment extends DialogFragment {

    public static Bundle getStartingArguments(boolean isEditing,int cylNumber,int position){

        Bundle args=new Bundle();
        args.putBoolean(KEY_MODE,isEditing);
        if(isEditing){
            args.putInt("cylinderNumber",cylNumber);
            args.putInt("position",position);
        }
        return args;

    }

    private static final String KEY_MODE="isEditing";

    private EditText cylinderNumberField;
    private Button btnSave;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view=getActivity().getLayoutInflater().inflate(R.layout.add_cylinder_operation,null);
        linkViews(view);
        initViews(view);

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();

    }

    private void linkViews(View view){

        cylinderNumberField=view.findViewById(R.id.add_cyl_operation_number);
        btnSave=view.findViewById(R.id.add_cyl_operation_btnSave);

    }

    private void initViews(View view){

        cylinderNumberField.setSelectAllOnFocus(true);
        if(isEditingMode())
            cylinderNumberField.setText(Integer.toString(getEditingCylinderNumber()));


        //Save button
        if(isEditingMode())
            btnSave.setText("SAVE");
        else
            btnSave.setText("ADD");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditingMode())
                    updateNumber();
                else
                    addNumber();
            }
        });


        //editField
        cylinderNumberField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId== EditorInfo.IME_ACTION_NEXT){
                    if(isEditingMode())
                        updateNumber();
                    else
                        addNumber();

                    return true;
                }

                return false;
            }
        });

    }

    private void updateNumber() {

        if(!validation())
            return;

        int no=Integer.parseInt(cylinderNumberField.getText().toString());
        ((OperationActivity)getActivity()).updateNumber(no,getEditingPosition());
        getDialog().dismiss();

    }

    private void addNumber() {

        if(!validation())
            return;

        int no=Integer.parseInt(cylinderNumberField.getText().toString());
        ((OperationActivity)getActivity()).addNumber(no);
        cylinderNumberField.setText("");

    }

    private boolean validation(){

        if(!isEditingMode()) {

            if (!((OperationActivity) getActivity()).canAddMoreCylinderNumbers()) {
                Msg.show(getActivity(), "Cannot add any more cylinders");
                return false;
            }
        }

        if(!isValidCylinderNumber(cylinderNumberField.getText().toString())){

            Msg.show(getActivity(),"Please enter a valid number");
            return false;

        }

        int no=Integer.parseInt(cylinderNumberField.getText().toString());
        if(isAlreadyAdded(no)){

            Msg.show(getActivity(),"This numbder already added!");
            return false;
        }

        return true;

    }

    private int getEditingCylinderNumber(){

        if(!isEditingMode() || getArguments()==null)
            return -1;

        return (getArguments().getInt("cylinderNumber",-1));

    }

    private int getEditingPosition(){

        if(!isEditingMode() || getArguments()==null)
            return -1;

        return (getArguments().getInt("position",-1));

    }

    private boolean isEditingMode(){

        if(getArguments()==null)
            return false;

        return getArguments().getBoolean(KEY_MODE,false);

    }

    private boolean isValidCylinderNumber(String number){

        if(TextUtils.isEmpty(number))
            return false;

        int no=Integer.parseInt(number);
        if(no <=0)
            return false;

        return true;

    }

    private boolean isAlreadyAdded(int number){

        return ((OperationActivity)getActivity()).isAlreadyAdded(number);

    }



}
