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
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;
import com.papco.sundar.cylinderinventory.logic.Transactions.DeleteAllotmentTransaction;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

public class DeleteAllotmentFragment extends TransactionDialogFragment {

    private Button btnDelete;
    private ProgressBar progressBar;
    private TextView msgView;
    private final String successMsg="Allotment deleted successfully";
    private final String progressMsg="Deleting allotment";
    private final String failureMsg="Delete allotment failed. Please check internet connection";
    private AllotmentActivityVM viewModel;
    private Allotment allotmentToDelete;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel= ViewModelProviders.of(getActivity()).get(AllotmentActivityVM.class);
        allotmentToDelete=viewModel.getAllotmentToDelete();

    }

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

    @Override
    public void showTransactionProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTransactionProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public List<Integer> getPrefetchList() {
        return allotmentToDelete.getCylinders();
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        return new DeleteAllotmentTransaction(allotmentToDelete.getId());
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
