package com.papco.sundar.cylinderinventory.screens.operations.outward.Invoice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionActivity;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.logic.Transactions.InvoiceTransaction;
import com.papco.sundar.cylinderinventory.screens.common.batchDetailView.CylinderTypeDetailedView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

public class InvoiceActivity extends TransactionActivity {

    private static final String KEY_ALLOT_ID = "allotment_id";
    private static final String KEY_CLIENT_NAME = "client_name";
    private static final String KEY_CYLINDER_COUNT = "cylinder_count";

    public static void start(Context context, Allotment allotment) {

        if (context == null || allotment == null)
            return;

        Bundle args = new Bundle();
        args.putInt(KEY_ALLOT_ID, allotment.getId());
        args.putString(KEY_CLIENT_NAME, allotment.getClientName());
        args.putInt(KEY_CYLINDER_COUNT, allotment.getNumberOfCylinders());

        Intent intent = new Intent(context, InvoiceActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);

    }

    private TextView clientName, cylinderCount;
    private LinearLayout container;
    private ProgressBar transactionProgressBar,loadingProgressBar;
    private InvoiceActivityVM viewModel;

    private final String successMsg="Invoice created successfully";
    private final String progressMsg="Preparing Invoice";
    private final String failureMsg="Invoice preparation failed";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoice_operation);
        linkViews();
        initViews();
        setupToolBar();
        initViewModel(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.mnu_done,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if(item.getItemId()==R.id.mnu_done){
            onPrepareInvoice();
            return true;
        }

        return false;
    }

    private void linkViews() {
        clientName = findViewById(R.id.invoice_operation_client_name);
        cylinderCount = findViewById(R.id.invoice_operation_cylinder_count);
        transactionProgressBar = findViewById(R.id.invoice_operation_progress_bar);
        loadingProgressBar=findViewById(R.id.invoice_operation_loading_progress);
        container = findViewById(R.id.invoice_operation_container);
    }

    private void initViews() {

        clientName.setText(getClientName());
        cylinderCount.setText(Integer.toString(getTotalCylinderCount()) + " Cylinders");
        showLoadingProgressBar();

    }

    private void setupToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        actionBar.setTitle("Invoice preview");
    }

    private void initViewModel(Bundle savedInstanceState) {

        viewModel = ViewModelProviders.of(this).get(InvoiceActivityVM.class);
        viewModel.getAllotment().observe(this, allotment -> {

            if(allotment==null){
                showAlertAndClose();
                return;
            }

            loadViewFromAllotment(allotment);
            hideLoadingProgressBar();
        });
        if(savedInstanceState==null)
            viewModel.loadAllotment(getAllottmentId());

    }

    private void loadViewFromAllotment(Allotment allotment) {

        List<List<Cylinder>> masterList;
        masterList = allotment.getTypedMasterList();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, 13, getResources().getDisplayMetrics());
        params.bottomMargin = (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        CylinderTypeDetailedView detailedView = null;

        for (List<Cylinder> monoList : masterList) {

            try {
                detailedView = new CylinderTypeDetailedView(this, monoList, Batch.TYPE_INVOICE);
            } catch (Exception e) {
                Msg.show(this, e.getMessage());
                finish();
            }

            container.addView(detailedView.getView(), params);

        }

    }

    private void showAlertAndClose(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Someone else has closed or deleted this allotment");
        builder.setPositiveButton("CLOSE", (dialog, which) -> finish());
        builder.setTitle("Alert");
        AlertDialog dialog=builder.create();
        dialog.setOnCancelListener(dialog1 -> finish());
        dialog.show();

    }

    private int getAllottmentId() {

        Bundle args = getIntent().getExtras();
        if (args == null)
            return -1;

        return args.getInt(KEY_ALLOT_ID, -1);

    }

    private int getTotalCylinderCount() {

        Bundle args = getIntent().getExtras();
        if (args == null)
            return -1;

        return args.getInt(KEY_CYLINDER_COUNT, -1);

    }

    private String getClientName() {

        Bundle args = getIntent().getExtras();
        if (args == null)
            return "Unknown";

        return args.getString(KEY_CLIENT_NAME, "Unknown");

    }

    private void showLoadingProgressBar(){
        container.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingProgressBar(){
        container.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
    }

    private void onPrepareInvoice(){

        viewModel.setAreWeInvoicing(true);
        startTransaction(successMsg,progressMsg,failureMsg,1);
    }

    // ************ Transaction overloads


    @Override
    public void showTransactionProgressBar() {
        transactionProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTransactionProgressBar() {
        transactionProgressBar.setVisibility(View.GONE);
    }

    @Override
    public List<Integer> getPrefetchList() {
        return viewModel.getAllotmentObject().getCylinders();
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        return new InvoiceTransaction(viewModel.getAllotmentObject().getClientId(),getAllottmentId());
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task.isSuccessful()){
            Msg.show(this,successMsg);
            finish();
        }else{
            viewModel.setAreWeInvoicing(false);

            if(task.getException()!=null){
                FirebaseFirestoreException exception=(FirebaseFirestoreException)task.getException();
                if(exception.getCode()==FirebaseFirestoreException.Code.CANCELLED)
                    Msg.show(this,task.getException().getMessage());
                else
                    Msg.show(this,failureMsg);
            }else{
                Msg.show(this,failureMsg);
            }
        }
    }
}
