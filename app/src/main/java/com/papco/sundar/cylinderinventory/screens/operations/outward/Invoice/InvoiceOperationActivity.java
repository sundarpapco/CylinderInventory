package com.papco.sundar.cylinderinventory.screens.operations.outward.Invoice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.papco.sundar.cylinderinventory.logic.Transactions.InvoiceTransaction;
import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;

import java.util.Arrays;
import java.util.List;

public class InvoiceOperationActivity extends OperationOutwardActivity {

    public static void start(Context context, @NonNull Allotment allotment){

        Bundle args=new Bundle();
        args.putInt("allotmentId",allotment.getId());
        args.putInt("destinationId",allotment.getClientId());
        args.putString("destinationName",allotment.getClientName());
        args.putInt("allotmentCylinderCount",allotment.getNumberOfCylinders());

        Intent intent=new Intent(context, InvoiceOperationActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    private ListenerRegistration listenerRegistration;
    private String successMsg="Invoice added successfully";
    private String progressMsg="Preparing Invoice";
    private String failureMsg="Failed to prepare Invoice. Please check internet connection";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadAllotment();
    }

    private void loadAllotment() {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        if(listenerRegistration!=null)
            listenerRegistration.remove();


        listenerRegistration=db.collection(DbPaths.COLLECTION_ALLOTMENT).document(getAllotment().getStringId())
                .addSnapshotListener(this,new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e!=null) {
                            Msg.show(InvoiceOperationActivity.this, "Error communicating wih server. Check internet connection");
                            return;
                        }

                        if(!documentSnapshot.exists()){
                            showAllotmentDeletedWarning();
                            return;
                        }else {
                            Allotment allotment=documentSnapshot.toObject(Allotment.class);
                            loadAllotmentData(allotment);
                        }
                    }
                });
    }

    private void loadAllotmentData(Allotment allotment){
        setData(allotment.getCylinders());
    }

    private void showAllotmentDeletedWarning(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("This invoice is closed by some other person or some administrator deleted it");
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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

    @Override
    public void onRecyclerItemClicked(Integer item, int position) {

    }

    @Override
    protected String getActivityTitle() {
        return "Invoice";
    }

    @Override
    protected void onSaveOperation() {
        startTransaction(successMsg,progressMsg,failureMsg,1);
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
        allotment.setState(Allotment.STATE_READY_FOR_INVOICE);
        //simulating a dummy list instead of reading from parcel.
        //becasue we will be getting the cylinder list from internet and not through intent
        allotment.setCylinders(Arrays.asList(23,45,78,98,34));

        return allotment;
    }


    @Override
    public List<Integer> getPrefetchList() {
        return getCylinders();
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        if(listenerRegistration!=null)
            listenerRegistration.remove();
        return new InvoiceTransaction(getDestinationId(),getAllotment().getId());
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
            loadAllotment();
        }
    }
}
