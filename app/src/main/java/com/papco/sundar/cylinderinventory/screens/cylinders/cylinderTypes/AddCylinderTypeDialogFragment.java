package com.papco.sundar.cylinderinventory.screens.cylinders.cylinderTypes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionDialogFragment;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.CylinderType;
import com.papco.sundar.cylinderinventory.logic.Transactions.AddCylinderTypeTransaction;
import com.papco.sundar.cylinderinventory.logic.Transactions.EditCylinderTypeTransaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddCylinderTypeDialogFragment extends TransactionDialogFragment {

    private static final String KEY_MODE = "isEditing";
    private static final String KEY_TYPE_NAME = "cylinderTypeName";
    private static final String KEY_CYLINDER_COUNT = "cylinderTypeCylinderCount";

    public static Bundle getStartingArgs(boolean isEditing, CylinderType cylinderType) {

        Bundle args = new Bundle();
        args.putBoolean(KEY_MODE, isEditing);
        if (cylinderType != null) {
            args.putString(KEY_TYPE_NAME, cylinderType.getName());
            args.putInt(KEY_CYLINDER_COUNT, cylinderType.getNoOfCylinders());
        }
        return args;

    }

    private CylinderType editingCylinderType;
    private TextView heading, cylinderTypeName;
    private ProgressBar progressBar;
    private Button btnSave;

    private String successMsg = "Cylinder type saved successfully";
    private String progressMsg = "Saving Cylinder type";
    private String failureMsg = "Failed to save cylinder type. Check internet connection";

    private final int REQ_CODE_ADD = 1;
    private final int REQ_CODE_EDIT = 2;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.new_destination, null);
        linkViews(view);
        initViews(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();
    }

    private void linkViews(View view) {

        heading = view.findViewById(R.id.dest_new_heading);
        cylinderTypeName = view.findViewById(R.id.dest_new_name);
        btnSave = view.findViewById(R.id.dest_new_btnSave);
        progressBar = view.findViewById(R.id.dest_new_progressBar);
    }

    private void initViews(View view) {

        heading.setText("Cylinder type");
        if (isEditingMode() && getCylinderType() != null) {
            cylinderTypeName.setText(getCylinderType().getName());
        } else
            cylinderTypeName.setText("");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameIsValid())
                    onSaveCylinderType();
            }
        });

    }


    private boolean nameIsValid() {

        String enteredName = cylinderTypeName.getText().toString().trim();

        if (TextUtils.isEmpty(enteredName)) {
            Msg.show(getActivity(), "Please enter a valid cylinder type");
            return false;
        }

        if (!isEditingMode())
            if (((CylinderTypeActivity) getActivity()).isDuplicate(enteredName)) {
                Msg.show(getActivity(), "This name already exist. Please choose a different name");
                return false;
            }

        return true;
    }

    private boolean isEditingMode() {

        if (getArguments() != null)
            return getArguments().getBoolean(KEY_MODE, false);

        return false;
    }

    private CylinderType getCylinderType() {

        if (editingCylinderType != null)
            return editingCylinderType;

        Bundle args = getArguments();
        if (args == null)
            return null;

        editingCylinderType = new CylinderType();
        editingCylinderType.setName(args.getString(KEY_TYPE_NAME, ""));
        editingCylinderType.setNoOfCylinders(args.getInt(KEY_CYLINDER_COUNT, 0));

        return editingCylinderType;

    }

    protected String getEnteredName() {

        return cylinderTypeName.getText().toString().trim();
    }

    private void onSaveCylinderType() {

        if (isEditingMode()) {
            startTransaction(successMsg, progressMsg, failureMsg, REQ_CODE_EDIT);
        } else {
            startTransaction(successMsg, progressMsg, failureMsg, REQ_CODE_ADD);
        }

    }

    @Override
    public void showTransactionProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTransactionProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {

        if (requestCode == REQ_CODE_ADD) {
            CylinderType cylinderType = new CylinderType();
            cylinderType.setName(getEnteredName());
            cylinderType.setNoOfCylinders(0);
            return new AddCylinderTypeTransaction(cylinderType);
        }

        if (requestCode == REQ_CODE_EDIT) {

            CylinderType cylinderType = getCylinderType();
            cylinderType.setName(getEnteredName());
            return new EditCylinderTypeTransaction(cylinderType);

        }

        return null;

    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if (task.isSuccessful()) {
            Msg.show(requireContext(), successMsg);
            getDialog().dismiss();

        } else {
            if(task.getException()!=null)
                Msg.show(getActivity(),task.getException().getMessage());
            else
                Msg.show(requireContext(), failureMsg);
        }
    }


}
