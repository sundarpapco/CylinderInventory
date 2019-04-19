package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.os.Bundle;

import com.google.android.gms.tasks.Task;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.DeleteConfirmationDialog;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;
import com.papco.sundar.cylinderinventory.logic.Transactions.DeleteOperationTransaction;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

public class DeleteBatchDialog extends DeleteConfirmationDialog {

    private MainActivityVM viewModel;
    private Batch batchToDelete;
    private String successMsg = "Transaction deleted successfully";
    private String progressMsg = "Deleting transaction";
    private String failureMsg = "Error deleting transaction";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(MainActivityVM.class);
        batchToDelete = viewModel.getBatchToDelete();
    }

    @Override
    protected String getMessage() {
        return "Are you sure want to delete this transaction?";
    }

    @Override
    protected void onButtonClicked() {
        if (batchToDelete == null)
            return;

        if (TransactionRunnerService.isRunning())
            return;

        startTransaction(successMsg, progressMsg, failureMsg, 1);

    }


    @Override
    public void showTransactionProgressBar() {
        showProgressBar();
    }

    @Override
    public void hideTransactionProgressBar() {
        hideProgressBar();
    }

    @Override
    public List<Integer> getPrefetchList() {
        return batchToDelete.getCylinders();
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        return new DeleteOperationTransaction(batchToDelete);
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if (task.isSuccessful()) {
            if (getActivity() != null)
                Msg.show(getActivity(), successMsg);

            if (getDialog() != null)
                getDialog().dismiss();
        } else
            Msg.show(getActivity(), task.getException().getMessage());

    }
}
