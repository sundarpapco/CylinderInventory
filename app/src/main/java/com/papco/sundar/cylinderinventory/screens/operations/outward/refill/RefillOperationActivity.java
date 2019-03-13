package com.papco.sundar.cylinderinventory.screens.operations.outward.refill;

import androidx.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.Transactions.RefillTransaction;
import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.repair.RepairOperationActivity;

import java.util.ArrayList;
import java.util.List;

public class RefillOperationActivity extends OperationOutwardActivity {

    private String successMsg="Cylinders sent for refilling successfully";
    private String progressMsg="Sending cylinders for refilling";
    private String failureMsg="Error sending cylinders for refilling. check internet connection";
    private boolean autoLoading=false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_auto_load,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.mnu_item_auto_load){
            autoLoadCylinders();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(autoLoading)
            autoLoadCylinders();
        else
            hideTransactionProgressBar();
    }

    @Override
    protected String getActivityTitle() {
        return "Send for refilling";
    }

    @Override
    protected String getDestinationHint() {
        return "Refill station";
    }

    @Override
    protected void onSaveOperation() {

        if(getCylinders().size()==0){
            Msg.show(this,"Select at least one cylinder!");
            return;
        }

        startTransaction(successMsg,progressMsg,failureMsg,1);

    }

    private void autoLoadCylinders() {

        showTransactionProgressBar();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        autoLoading=true;
        db.collection(DbPaths.COLLECTION_CYLINDERS)
                .whereEqualTo("empty", true)
                .get()
                .addOnCompleteListener(this,new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        hideTransactionProgressBar();
                        autoLoading=false;
                        if (!task.isSuccessful()) {
                            Msg.show(RefillOperationActivity.this, "Error connecting to server. Please check");
                            return;
                        }else {

                            List<Integer> cylinderNos = new ArrayList<>();
                            for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                                if(snapshot.getLong("locationId").intValue()== Destination.TYPE_WAREHOUSE)
                                    if(!snapshot.getBoolean("damaged"))
                                    cylinderNos.add(snapshot.getLong("cylinderNo").intValue());
                            }
                            setData(cylinderNos);
                        }
                    }
                });

    }

    //region Transaction Overloads -----------------------------------------------------------------

    @Override
    public List<Integer> getPrefetchList() {
        return getCylinders();
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        return new RefillTransaction(getDestinationId());
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

    //endregion Transaction Overloads --------------------------------------------------------------
}
