package com.papco.sundar.cylinderinventory.screens.operations.inward;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.logic.Transactions.EcrTransaction;
import com.papco.sundar.cylinderinventory.screens.operations.common.OperationActivity;

import java.util.List;

public class EcrActivity extends OperationActivity {

    private String successMsg="Empty cylinders returned successfully";
    private String progressMsg="Getting back empty cylinders";
    private String failureMsg="Error getting empty cylinders. check internet connection";

    @Override
    protected String getActivityTitle() {
        return "Empty cylinder receipt";
    }

    @Override
    protected void onSaveOperation() {
        if(getCylinders().size()==0){
            Msg.show(this,"Select at least one cylinder");
            return;
        }

        startTransaction(successMsg,progressMsg,failureMsg,1);

    }

    //region Transaction overloads -------------------------------------------------------


    @Override
    public List<Integer> getPrefetchList() {
        return getCylinders();
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        return new EcrTransaction();
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

    //endregion Transaction overloads
}
