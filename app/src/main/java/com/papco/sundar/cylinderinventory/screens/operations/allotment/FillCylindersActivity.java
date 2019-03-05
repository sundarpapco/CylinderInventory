package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.logic.Transactions.AllocateAllotmentTransaction;
import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;

import java.util.List;

public class FillCylindersActivity extends OperationOutwardActivity {

    public static void start(Context context,@NonNull Allotment allotment){

        Bundle args=new Bundle();
        args.putInt("allotmentId",allotment.getId());
        args.putInt("destinationId",allotment.getClientId());
        args.putString("destinationName",allotment.getClientName());
        args.putInt("allotmentCylinderCount",allotment.getNumberOfCylinders());

        Intent intent=new Intent(context,FillCylindersActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    private ListenerRegistration listenerRegistration;
    private String successMsg="Allotment successful";
    private String progressMsg="Alloting cylinders";
    private String failureMsg="Failed to allot cylinders. Some other person may have allotted in parallel. Also check internet connection";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolBarSubTitle();
    }

    @Override
    public void onStart() {
        super.onStart();
        startMonitoringTheAllotment();
    }

    private void startMonitoringTheAllotment() {

        if(listenerRegistration!=null)
            listenerRegistration.remove();

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        listenerRegistration=db.collection(DbPaths.COLLECTION_ALLOTMENT).document(getAllotment().getStringId())
                .addSnapshotListener(this,new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null){
                            Msg.show(FillCylindersActivity.this,"Error connecting to server. Check internet connection");
                            return;
                        }

                        if(!documentSnapshot.exists()){
                            showAllotmentAlreadyBilledMessage();
                            return;
                        }

                        if(documentSnapshot.getLong("state").intValue()==Allotment.STATE_ALLOTTED){
                            markAllotmentAsPickedUp(documentSnapshot);
                            return;
                        }

                        if(documentSnapshot.getLong("state").intValue()==Allotment.STATE_READY_FOR_INVOICE){
                            showAllotmentAlreadyAllotedMessage();
                        }

                    }
                });

    }

    private void markAllotmentAsPickedUp(DocumentSnapshot documentSnapshot) {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        Allotment allotment=documentSnapshot.toObject(Allotment.class);
        allotment.setState(Allotment.STATE_PICKED_UP);

        db.collection(DbPaths.COLLECTION_ALLOTMENT).document(getAllotment().getStringId())
                .set(allotment).addOnFailureListener(this,new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Msg.show(FillCylindersActivity.this,"Error connecting to server. Please check internet connection");
            }
        });

    }

    private void showAllotmentAlreadyAllotedMessage() {

        showAllotmentAlreadyBilledMessage();

    }

    private void showAllotmentAlreadyBilledMessage() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("This Allotment has already been completed by someone else");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();

    }

    private void setupToolBarSubTitle() {

        ActionBar actionBar=getSupportActionBar();
        actionBar.setSubtitle(getAllottedNumberOfCylinders()+" Cylinders");
    }

    @Override
    protected String getActivityTitle() {
        return "Add cylinders for allotment";
    }

    @Override
    public boolean canAddMoreCylinderNumbers() {

        return getNumberOfCylindersAdded()< getAllottedNumberOfCylinders();
    }

    @Override
    protected void onSaveOperation() {

        if(!isCorrectNumberOfCylindersAdded()){
            Msg.show(this,"You have to allocate exactly " +
                    Integer.toString(getAllottedNumberOfCylinders())+" cylinders");
            return;
        }

        startTransaction(successMsg,progressMsg,failureMsg,1);
        //BatchReader reader=new BatchReader(getCylinders(),null);
        //reader.fetchDocuments();
    }

    protected int getAllottedNumberOfCylinders(){

        Bundle args=getIntent().getExtras();

        if(args==null)
            return 0;
        else
            return args.getInt("allotmentCylinderCount",0);

    }

    protected Allotment getAllotment(){

        Bundle args=getIntent().getExtras();

        if(args==null)
            return null;

        Allotment allotment=new Allotment();

        allotment.setId(args.getInt("allotmentId",-1));
        allotment.setClientId(args.getInt("destinationId",-1));
        allotment.setClientName(args.getString("destinationName",""));
        allotment.setNumberOfCylinders(args.getInt("allotmentCylinderCount",0));
        allotment.setCylinders(null);

        return allotment;
    }

    private boolean isCorrectNumberOfCylindersAdded(){

        return getNumberOfCylindersAdded()==getAllottedNumberOfCylinders();
    }


    //region Transaction overrides --------------------------------------------------------

    @Override
    public List<Integer> getPrefetchList() {
        return getCylinders();
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {

        if(listenerRegistration!=null)
            listenerRegistration.remove();

        return new AllocateAllotmentTransaction(getAllotment().getId());

    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task==null) { //prefetch failed
            Msg.show(this, "Error connecting to server. Please connect to internet");
            return;
        }

        if (task.isSuccessful()) {
            Msg.show(this, successMsg);
            finish();
        } else {
            FirebaseFirestoreException exception=(FirebaseFirestoreException)task.getException();
            if(exception.getCode()==FirebaseFirestoreException.Code.CANCELLED){
                Msg.show(this,exception.getMessage());
            }else{
                Msg.show(this,failureMsg);
            }
            startMonitoringTheAllotment();
        }
    }

    //endregion ---------------------------------------------------------------------------
}
