package com.papco.sundar.cylinderinventory.screens.operations.allotment.pickup;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.logic.Transactions.AllocateAllotmentTransaction;
import com.papco.sundar.cylinderinventory.logic.Transactions.PickupAllotmentTransaction;
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

    private FillCylindersVM viewModel;

    private String successMsg="Allotment successful";
    private String progressMsg="Alloting cylinders";
    private String failureMsg="Failed to allot cylinders. Some other person may have allotted in parallel. Also check internet connection";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolBarSubTitle();
        viewModel= ViewModelProviders.of(this).get(FillCylindersVM.class);
        viewModel.loadAllotment(getAllotment().getId());
        viewModel.getCurrentAllotment().observe(this, allotment -> {
            if(allotment==null){
                showWarningAndClose();
            }
        });
        if(savedInstanceState!=null)
            setData(viewModel.getCylindersBackup());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.mnu_pickup,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.mnu_pickup_info){

            PickupRequirementFragment pickupInfoFragment=new PickupRequirementFragment();
            pickupInfoFragment.show(getSupportFragmentManager(),"infoFragment");
            return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        viewModel.setCylindersBackup(getCylinders());
    }

    private void showWarningAndClose() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("This Allotment has already been completed or deleted by someone else");
        builder.setPositiveButton("OK", (dialog, which) -> finish());
        builder.setOnCancelListener(dialog -> finish());
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

        viewModel.setAreWeApproving(true);
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

        return new PickupAllotmentTransaction(getAllotment().getId());

    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task==null) { //prefetch failed
            Msg.show(this, "Error connecting to server. Please connect to internet");
            viewModel.setAreWeApproving(false);
            return;
        }

        if (task.isSuccessful()) {
            Msg.show(this, successMsg);
            finish();
        } else {
            viewModel.setAreWeApproving(false);
            FirebaseFirestoreException exception=(FirebaseFirestoreException)task.getException();
            if(exception.getCode()==FirebaseFirestoreException.Code.CANCELLED){
                Msg.show(this,exception.getMessage());
            }else{
                Msg.show(this,failureMsg);
            }
        }
    }

    //endregion ---------------------------------------------------------------------------
}
