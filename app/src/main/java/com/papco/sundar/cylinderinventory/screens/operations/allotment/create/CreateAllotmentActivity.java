package com.papco.sundar.cylinderinventory.screens.operations.allotment.create;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ExitConfirmationDialog;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionActivity;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.logic.Transactions.CreateAllotmentTransaction;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentListItem;

public class CreateAllotmentActivity extends TransactionActivity implements RecyclerListener<AllotmentListItem> {

    public static void start(Context context, @NonNull Destination destination){

        Bundle bundle=new Bundle();
        bundle.putInt("destinationId",destination.getId());
        bundle.putString("destinationName",destination.getName());

        Intent intent=new Intent(context, CreateAllotmentActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);

    }

    private TextView clientName,totalCylinderCount;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private CreateAllotmentVM viewModel;
    private AllotmentItemAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_allotment);
        viewModel= ViewModelProviders.of(this).get(CreateAllotmentVM.class);
        linkViews();
        initViews();
        setupToolBar();
        if(savedInstanceState!=null){
            adapter.setData(viewModel.getAllotmentListBackup());
            updateTotalCylinderCount();
        }

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

        if(item.getItemId()==R.id.mnu_done)
            onCreateAllotment();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        viewModel.setAllotmentListBackup(adapter.getData());
    }

    @Override
    public void onBackPressed() {

        if(adapter.getData().size()==0)
            super.onBackPressed();
        else
            showExitConfirmation();
    }

    @Override
    public void onRecyclerItemClicked(AllotmentListItem item, int position) {
        CreateAllotmentItemFragment addFragment=new CreateAllotmentItemFragment();
        addFragment.setArguments(CreateAllotmentItemFragment.getArguments(position,item));
        addFragment.show(getSupportFragmentManager(),"editFragment");
    }

    @Override
    public void onRecyclerItemLongClicked(AllotmentListItem item, int position, View view) {

        PopupMenu popupMenu=new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.mnu_delete,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId()==R.id.mnu_delete){
                    showDeleteConfirmationDialog(position);
                }

                return false;
            }
        });
        popupMenu.show();

    }


    private void linkViews() {

        clientName=findViewById(R.id.create_allotment_client_name);
        totalCylinderCount=findViewById(R.id.create_allotment_total_cylinder_count);
        recyclerView=findViewById(R.id.create_allotment_recycler);
        fab=findViewById(R.id.create_allotment_fab);
        progressBar=findViewById(R.id.create_allotment_progress_bar);

    }

    private void initViews() {

        clientName.setText(getDestinationName());
        totalCylinderCount.setText("0 Cylinders");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateAllotmentItem();
            }
        });

        adapter=new AllotmentItemAdapter(this);
        SpacingDecoration spacingDecor=new SpacingDecoration(this,SpacingDecoration.VERTICAL,0,0,26);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(spacingDecor);
        recyclerView.setAdapter(adapter);
    }

    private void setupToolBar() {

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }



    public boolean isDuplicate(int position, String cylinderTypeName){
        return adapter.isDuplicate(position,cylinderTypeName);
    }

    public void addData(AllotmentListItem item){
        adapter.addData(item);
        updateTotalCylinderCount();
    }

    public void updateData(int position,AllotmentListItem item){
        adapter.updateData(position,item);
        updateTotalCylinderCount();
    }

    private void showDeleteConfirmationDialog(int deletePosition) {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Delete this item?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                adapter.deleteData(deletePosition);
                updateTotalCylinderCount();
            }
        });
        builder.show();
    }

    protected void onCreateAllotment(){

        if(adapter.getData().size()==0){
            Msg.show(this,"Please add at least one requirement");
            return;
        }

        String successMsg="Allotment created successfully";
        String progressMsg="Creating allotment";
        String failureMsg="Failed to create allotment. Check internet connection";

        startTransaction(successMsg,progressMsg,failureMsg,1);

    }

    private void onCreateAllotmentItem(){

        CreateAllotmentItemFragment addFragment=new CreateAllotmentItemFragment();
        addFragment.setArguments(CreateAllotmentItemFragment.getArguments(-1,null));
        addFragment.show(getSupportFragmentManager(),"createFragment");

    }

    private void updateTotalCylinderCount(){

        int totalCount=adapter.getTotalCylinderCount();
        totalCylinderCount.setText(Integer.toString(totalCount)+" Cylinders");

    }

    private void showExitConfirmation(){

        ExitConfirmationDialog exitConfirmationDialog=new ExitConfirmationDialog();
        exitConfirmationDialog.show(getSupportFragmentManager(),"exitDialog");
    }



    protected int getDestinationId(){

        if(getIntent().getExtras()==null)
            return -1;

        return getIntent().getExtras().getInt("destinationId",-1);
    }

    protected String getDestinationName(){

        if(getIntent().getExtras()==null)
            return "";

        return getIntent().getExtras().getString("destinationName","");
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
    public BaseTransaction getTransactionToRun(int requestCode) {

        Allotment allotment=new Allotment();
        allotment.setState(Allotment.STATE_ALLOTTED);
        allotment.setCylinders(null);
        allotment.setClientId(getDestinationId());
        allotment.setClientName(getDestinationName());
        allotment.setNumberOfCylinders(adapter.getTotalCylinderCount());
        allotment.setRequirement(adapter.getRequirementHashMap());

        return new CreateAllotmentTransaction(allotment);

    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task.isSuccessful()){
            Msg.show(this,"Allotment created successfully");
            finish();
        }else{

            Exception e=task.getException();
            if(e!=null)
                Msg.show(this,e.getMessage());
            else
                Msg.show(this,"Error creating allotment. Check internet connection");
        }
    }
}
