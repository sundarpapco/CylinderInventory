package com.papco.sundar.cylinderinventory.screens.operations.allotment.create;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.CylinderType;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentListItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class CreateAllotmentItemFragment extends DialogFragment {

    private static final String KEY_MODE="editing_mode";
    private static final String KEY_CYL_TYPE_NAME="cylinder_type_name";
    private static final String KEY_CYL_COUNT="cylinder_count";
    private static final String KEY_EDIT_POSITION="editing_position";

    public static Bundle getArguments(int position,AllotmentListItem allotmentListItem){

        Bundle args=new Bundle();

        if(allotmentListItem==null) {
            args.putBoolean(KEY_MODE, false);
            return args;
        }

        args.putBoolean(KEY_MODE,true);
        args.putString(KEY_CYL_TYPE_NAME,allotmentListItem.getCylinderTypeName());
        args.putInt(KEY_CYL_COUNT,allotmentListItem.getRequiredQuantity());
        args.putInt(KEY_EDIT_POSITION,position);
        return args;
    }

    private CreateAllotmentVM viewModel;
    private AllotmentListItem loadedListItem;
    private AppCompatSpinner spinner;
    private EditText cylinderCountField;
    private Button btnSave;
    private ArrayAdapter<CylinderType> spinnerAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel= ViewModelProviders.of(getActivity()).get(CreateAllotmentVM.class);
        spinnerAdapter=new ArrayAdapter<CylinderType>(getActivity(),R.layout.spinner_item,R.id.spinner_item_text);

        viewModel.getCylinderTypes().observe(this, new Observer<List<CylinderType>>() {
            @Override
            public void onChanged(List<CylinderType> cylinderTypes) {

                spinnerAdapter.clear();
                spinnerAdapter.addAll(cylinderTypes);

                if(savedInstanceState!=null){
                    spinner.setSelection(viewModel.getSpinnerSelectionPositionBackup());
                    return;
                }

                if(isEditingMode()){
                    AllotmentListItem loaded=getLoadedListItem();
                    cylinderCountField.setText(Integer.toString(loaded.getRequiredQuantity()));
                    CylinderType type=new CylinderType();
                    type.setName(loaded.getCylinderTypeName());
                    int position=spinnerAdapter.getPosition(type);
                    spinner.setSelection(position);
                }
            }
        });

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view=getActivity().getLayoutInflater().inflate(R.layout.create_allotment_item,null);
        linkViews(view);
        initViews(view);

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setView(view);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        viewModel.setSpinnerSelectionPositionBackup(spinner.getSelectedItemPosition());
    }

    private void linkViews(View view) {

        spinner=view.findViewById(R.id.allotment_item_spinner);
        cylinderCountField=view.findViewById(R.id.allotment_item_cylinder_count);
        btnSave=view.findViewById(R.id.allotment_item_btn_save);

    }

    private void initViews(View view) {

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAllDataValid())
                    onSaveAllotmentItem();
            }
        });
        spinner.setAdapter(spinnerAdapter);
        if(isEditingMode()) {
            AllotmentListItem loaded=getLoadedListItem();
            cylinderCountField.setText(Integer.toString(loaded.getRequiredQuantity()));
        }

        cylinderCountField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId== EditorInfo.IME_ACTION_NEXT){
                    if(isAllDataValid())
                        onSaveAllotmentItem();
                }

                return false;
            }
        });
    }


    private void onSaveAllotmentItem(){

        CreateAllotmentActivity parent=(CreateAllotmentActivity)getActivity();
        CylinderType selectedType=(CylinderType)spinner.getSelectedItem();
        AllotmentListItem item=new AllotmentListItem();
        item.setApprovedQuantity(0);
        item.setRequiredQuantity(Integer.parseInt(cylinderCountField.getText().toString().trim()));
        item.setCylinderTypeName(selectedType.getName());

        if(isEditingMode()) {
            parent.updateData(getEditingPosition(), item);
            getDialog().dismiss();
        }else {
            parent.addData(item);
            cylinderCountField.setText("");
            Msg.show(getActivity(),"Added successfully");
        }

    }

    private boolean isAllDataValid(){

        String enteredCount=cylinderCountField.getText().toString().trim();
        if(TextUtils.isEmpty(enteredCount)){
            Msg.show(getActivity(),"Please enter a valid cylinder count");
            return false;
        }

        int cylinderCount=Integer.parseInt(enteredCount);
        if(cylinderCount<=0){
            Msg.show(getActivity(),"Please enter a valid cylinder count");
            return false;
        }

        if(((CylinderType)spinner.getSelectedItem()).getName().equals("Default")){
            Msg.show(getActivity(),"You need a valid cylinder type");
            return false;
        }

        CreateAllotmentActivity parent=(CreateAllotmentActivity)getActivity();
        int position;
        String selectedCylinderName=((CylinderType)spinner.getSelectedItem()).getName();

        if(isEditingMode())
            position=getEditingPosition();
        else
            position=-1;

        if(parent.isDuplicate(position,selectedCylinderName)){
            Msg.show(getActivity(),"This cylinder type already exists");
            return false;
        }

        return true;

    }

    private boolean isEditingMode(){

        Bundle args=getArguments();
        if(args==null)
            return false;

        return args.getBoolean(KEY_MODE);

    }

    private AllotmentListItem getLoadedListItem(){

        if(loadedListItem!=null)
            return loadedListItem;

        if(!isEditingMode())
            return null;

        Bundle args=getArguments();
        loadedListItem=new AllotmentListItem();
        loadedListItem.setCylinderTypeName(args.getString(KEY_CYL_TYPE_NAME));
        loadedListItem.setRequiredQuantity(args.getInt(KEY_CYL_COUNT));
        loadedListItem.setApprovedQuantity(0);
        return loadedListItem;

    }

    private int getEditingPosition(){

        Bundle args=getArguments();
        if(args==null)
            return -1;

        return args.getInt(KEY_EDIT_POSITION);
    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cylinderCountField.getWindowToken(), 0);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        hideKeyboard();
    }
}
