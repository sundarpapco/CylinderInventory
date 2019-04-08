package com.papco.sundar.cylinderinventory.screens.operations.allotment.approve;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ExitConfirmationDialog;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionActivity;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;
import com.papco.sundar.cylinderinventory.logic.Transactions.ApproveAllotmentTransaction;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentListItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ApproveAllotmentActivity extends TransactionActivity implements ApproveAllotmentAdapter.RecyclerListener {

    private static final String KEY_ALLOTMENT_ID="allotment_id";
    private static final String KEY_CLIENT_NAME="client_name";
    private static final String KEY_REQ_CYLINDER_COUNT="required_cyl_count";

    public static void start(@NonNull Context context, @NonNull Allotment allotment){

        Bundle arg=new Bundle();
        arg.putInt(KEY_ALLOTMENT_ID,allotment.getId());
        arg.putString(KEY_CLIENT_NAME,allotment.getClientName());
        arg.putInt(KEY_REQ_CYLINDER_COUNT,allotment.getNumberOfCylinders());

        Intent intent=new Intent(context,ApproveAllotmentActivity.class);
        intent.putExtras(arg);
        context.startActivity(intent);

    }


    private TextView clientName,totalApprovedCylinders;
    private RecyclerView recyclerView;
    private ProgressBar transactionProgressBar,recyclerProgressBar;
    private ApproveAllotmentAdapter adapter;
    private ApproveAllotmentVM viewModel;

    private final int REQ_IGNORE=1;
    private final int REQ_APPROVE_LATER=2;

    private final String successMsg="Allotment approved successfully";
    private final String progressMsg="Approving allotment";
    private final String failureMsg="Failed to approve allotment. Check internet connection";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.approve_allotment);
        linkViews();
        initViews();
        setupToolBar();
        viewModel= ViewModelProviders.of(this).get(ApproveAllotmentVM.class);
        viewModel.loadAllotment(getAllotmentId());
        viewModel.getCurrentAllotment().observe(this, new Observer<Allotment>() {
            @Override
            public void onChanged(Allotment allotment) {


                if(allotment==null){
                    //someone has changed the document since we have loaded it
                    //show message and close this activity
                    hideRecyclerProgressBar();
                    showAlertAndClose();
                    return;
                }

                if(savedInstanceState==null)
                    adapter.setData(allotment.getRequirement());
                else
                    adapter.setData(viewModel.getApprovalListBackup());

                hideRecyclerProgressBar();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.mnu_done,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        if(item.getItemId()==R.id.mnu_done) {

            if(TransactionRunnerService.isRunning())
                return false;

            showApprovalConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        viewModel.setApprovalListBackup(adapter.getData());
    }

    @Override
    public void onBackPressed() {

        if(adapter.getApprovedCylinderCount()!=getTotalRequiredCylinders()) {
            showExitConfirmationDialog();
            return;
        }

        super.onBackPressed();
    }

    private void linkViews() {

        clientName=findViewById(R.id.approve_allot_client_name);
        totalApprovedCylinders=findViewById(R.id.approve_allot_total_cylinder_count);
        recyclerView=findViewById(R.id.approve_allot_recycler);
        transactionProgressBar =findViewById(R.id.approve_allot_progress_bar);
        recyclerProgressBar=findViewById(R.id.approve_allot_recycler_progressBar);
    }

    private void initViews() {

        clientName.setText(getClientName());
        //updateTotalCylInfo(getTotalRequiredCylinders(),getTotalRequiredCylinders());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SpacingDecoration decoration=new SpacingDecoration(this,SpacingDecoration.VERTICAL,0,0,26);
        adapter=new ApproveAllotmentAdapter(this);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(adapter);
        showRecyclerProgressBar();
    }

    private void setupToolBar() {

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        actionBar.setTitle("Approve allotment");
    }

    @Override
    public void onRecyclerItemClicked(AllotmentListItem item, int position) {

        NumberPickerDialogFragment pickerFragment=new NumberPickerDialogFragment();
        pickerFragment.setArguments(
                NumberPickerDialogFragment.getStartingArgs(item.getApprovedQuantity(),item.getRequiredQuantity(),position));
        pickerFragment.show(getSupportFragmentManager(),"pickerFragment");

    }

    @Override
    public void onRecyclerItemLongClicked(AllotmentListItem item, int position, View view) {

    }

    @Override
    public void onApprovedCylinderCountChanged(int approvedCylinderCount) {

        String requiredCount=Integer.toString(getTotalRequiredCylinders());
        String approvedCount=Integer.toString(approvedCylinderCount);
        totalApprovedCylinders.setText(approvedCount+" of "+requiredCount+" approved");

    }

    public void updateData(int position, int approvalQuantity){
        adapter.updateData(position,approvalQuantity);
    }

    private void showApprovalConfirmation(){

        if(adapter.getApprovedCylinderCount()==0){
            Msg.show(this,"Approve at least one cylinder");
            return;
        }


        if(getTotalRequiredCylinders()!=adapter.getApprovedCylinderCount()){

            UnapprovedConfirmationDialog confirmationDialog=new UnapprovedConfirmationDialog();
            confirmationDialog.show(getSupportFragmentManager(),"confirmationDialog");
        }else{

            ApprovalConfirmationDialog confirmationDialog=new ApprovalConfirmationDialog();
            confirmationDialog.show(getSupportFragmentManager(),"confirmationDialog");
        }

    }

    public void onApproveAllotment(boolean ignoreUnapproved){

        viewModel.setAreWeApproving(true);
        if(ignoreUnapproved)
            startTransaction(successMsg,progressMsg,failureMsg,REQ_IGNORE);
        else
            startTransaction(successMsg,progressMsg,failureMsg,REQ_APPROVE_LATER);

    }

    private int getAllotmentId(){

        Bundle args=getIntent().getExtras();
        if(args==null)
            return -1;

        return args.getInt(KEY_ALLOTMENT_ID,-1);

    }

    private int getTotalRequiredCylinders(){

        Bundle args=getIntent().getExtras();
        if(args==null)
            return -1;

        return args.getInt(KEY_REQ_CYLINDER_COUNT,-1);

    }

    private String getClientName(){

        Bundle args=getIntent().getExtras();
        if(args==null)
            return "Unknown";

        return args.getString(KEY_CLIENT_NAME,"Unknown");

    }

    private void showRecyclerProgressBar(){
        recyclerProgressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideRecyclerProgressBar(){
        recyclerProgressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showAlertAndClose(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Someone else has approved or deleted this document and needs to close now");
        builder.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setTitle("Alert");
        AlertDialog dialog=builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();

    }

    private void showExitConfirmationDialog(){

        ExitConfirmationDialog exitConfirmationDialog=new ExitConfirmationDialog();
        exitConfirmationDialog.getArguments("Abort","Are you sure want to abort this approval?","ABORT");
        exitConfirmationDialog.show(getSupportFragmentManager(),"exitDialog");
    }

    private Allotment getClone(Allotment allotment){

        Allotment clone=new Allotment();
        clone.setId(allotment.getId());
        clone.setClientId(allotment.getClientId());
        clone.setClientName(allotment.getClientName());
        clone.setNumberOfCylinders(allotment.getNumberOfCylinders());
        clone.setState(allotment.getState());
        clone.setTimeStamp(allotment.getTimeStamp());
        clone.setRequirement(allotment.getRequirement());
        clone.setCylinders(allotment.getCylinders());

        return clone;

    }

    // ********* Transaction Overloads


    @Override
    public void showTransactionProgressBar() {
        transactionProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTransactionProgressBar() {
        transactionProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {

        Allotment laterAllotment;
        Allotment approvalAllotment=getClone(viewModel.getCurrentAllotment().getValue());
        approvalAllotment.setNumberOfCylinders(adapter.getApprovedCylinderCount());
        approvalAllotment.setState(Allotment.STATE_APPROVED);
        approvalAllotment.setRequirement(adapter.getApprovedHashMap());

        if(requestCode==REQ_IGNORE){
            laterAllotment=null;
        }else{
            laterAllotment=getClone(approvalAllotment);
            laterAllotment.setNumberOfCylinders(getTotalRequiredCylinders()-adapter.getApprovedCylinderCount());
            laterAllotment.setState(Allotment.STATE_ALLOTTED);
            laterAllotment.setRequirement(adapter.getUnApprovedHashMap());
        }

        return new ApproveAllotmentTransaction(approvalAllotment,laterAllotment);
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task.isSuccessful()){
            Msg.show(this,successMsg);
            finish();
            return;
        }else{
            if(task.getException()!=null)
                Msg.show(this,task.getException().getMessage());
            else
                Msg.show(this,failureMsg);
        }
        viewModel.setAreWeApproving(false);
    }
}
