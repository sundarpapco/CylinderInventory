package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionDialogFragment;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;
import com.papco.sundar.cylinderinventory.logic.Transactions.DeleteAllotmentTransaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DeleteAllotmentFragment extends TransactionDialogFragment {

    private static final String KEY_ALLOTMENT_ID="allotment_id";

    public static Bundle getStartingArgs(int allotmentId){

        Bundle args=new Bundle();
        args.putInt(KEY_ALLOTMENT_ID,allotmentId);
        return args;

    }

    private Button btnDelete;
    private ProgressBar progressBar;
    private TextView msgView;
    private final String successMsg="Allotment deleted successfully";
    private final String progressMsg="Deleting allotment";
    private final String failureMsg="Delete allotment failed. Please check internet connection";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.alert_fragment, null);
        linkViews(view);
        initViews();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();

    }

    private void linkViews(View view) {

        btnDelete=view.findViewById(R.id.alert_fragment_btnPositive);
        progressBar=view.findViewById(R.id.alert_fragment_progressBar);
        msgView=view.findViewById(R.id.alert_fragment_message);
    }

    private void initViews() {

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TransactionRunnerService.isRunning())
                    return;

                onDeleteAllotment();
            }
        });

        msgView.setText("Sure want to delete this allotment?");
    }

    private void onDeleteAllotment(){

        startTransaction(successMsg,progressMsg,failureMsg,1);

    }

    private int getAllotmentId(){

        Bundle args=getArguments();
        if(args==null)
            return -1;

        return args.getInt(KEY_ALLOTMENT_ID,-1);
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
        return new DeleteAllotmentTransaction(getAllotmentId());
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task.isSuccessful()){
            Msg.show(getActivity(),successMsg);
            getDialog().dismiss();
        }else{
            if(task.getException()!=null){
                Msg.show(getActivity(),task.getException().getMessage());
            }else{
                Msg.show(getActivity(),failureMsg);
            }
        }
    }
}
