package com.papco.sundar.cylinderinventory.screens.operations.inward;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.logic.Transactions.RciTransaction;
import com.papco.sundar.cylinderinventory.screens.operations.common.OperationActivity;

import java.util.List;

public class RciActivity extends OperationActivity {

    private String successMsg="Repair cylinders returned successfully";
    private String progressMsg="Getting back repair cylinders";
    private String failureMsg="Error getting repair cylinders. check internet connection";

    @Override
    protected String getActivityTitle() {
        return "Repaired cylinder inward";
    }

    @Override
    protected void onSaveOperation() {

        if(getCylinders().size()==0){
            Msg.show(this,"Add at least one cylinder!");
            return;
        }

        startTransaction(successMsg,progressMsg,failureMsg,1);

    }

    @Override
    public List<Integer> getPrefetchList() {
        return getCylinders();
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        return new RciTransaction();
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if (task == null) { //prefetch failed
            Msg.show(this, "Error connecting to server. Please connect to internet");
            return;
        }

        if (task.isSuccessful()) {
            Msg.show(this, successMsg);
            finish();
        } else {
            FirebaseFirestoreException exception = (FirebaseFirestoreException) task.getException();
            if (exception.getCode() == FirebaseFirestoreException.Code.CANCELLED) {
                Msg.show(this, exception.getMessage());
            } else {
                Msg.show(this, failureMsg);
            }
        }
    }
}
