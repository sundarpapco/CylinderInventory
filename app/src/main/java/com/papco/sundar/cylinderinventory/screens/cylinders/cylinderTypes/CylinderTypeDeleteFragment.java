package com.papco.sundar.cylinderinventory.screens.cylinders.cylinderTypes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Task;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionDialogFragment;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.logic.Transactions.DeleteCylinderTypeTransaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CylinderTypeDeleteFragment extends TransactionDialogFragment {


    private static final String KEY_TYPE_ID="cyinder_type_id";

    public static Bundle getStartingArgs(String typeId){

        Bundle args=new Bundle();
        args.putString(KEY_TYPE_ID,typeId);
        return args;

    }


    private Button btnDelete;
    private ProgressBar progressBar;
    private final String successMsg="Cylinder type deleted successfully";
    private final String progressMsg="Deleting cylinder type";
    private final String failureMsg="Deleting cylinder type failed. Please check internet connection";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.alert_fragment, null);
        linkViews(view);
        initViews(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();

    }

    private void linkViews(View view) {

        btnDelete=view.findViewById(R.id.alert_fragment_btnPositive);
        progressBar=view.findViewById(R.id.alert_fragment_progressBar);
    }

    private void initViews(View view) {

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteCylinderType();
            }
        });
    }

    private void onDeleteCylinderType() {

        startTransaction(successMsg,progressMsg,failureMsg,1);

    }

    private String getTypeId(){

        Bundle args=getArguments();
        if(args==null)
            return "Default";

        return args.getString(KEY_TYPE_ID,"Default");

    }


    //region Transaction Overloads


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
        return new DeleteCylinderTypeTransaction(getTypeId());
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task.isSuccessful()){
            Msg.show(getContext(),successMsg);
            getDialog().dismiss();
        }else
            Msg.show(getContext(),failureMsg);
    }
}
