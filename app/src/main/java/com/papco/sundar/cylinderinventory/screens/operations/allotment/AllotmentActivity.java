package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.approve.ApproveAllotmentActivity;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.pickup.FillCylindersActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.Invoice.InvoiceActivity;

import java.util.List;

public class AllotmentActivity extends ConnectivityActivity implements RecyclerListener<Allotment> {


    private RecyclerView recyclerView;
    private AllotmentAdapter adapter;
    private ProgressBar progressBar;
    private AllotmentActivityVM viewModel;

    private final String successMsg="Allotment deleted successfully";
    private final String progressMsg="Deleting allotment";
    private final String failureMsg="Allotment deletion failed. Try again";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allotment);
        linkViews();
        initViews();
        setupToolBar();
        viewModel= ViewModelProviders.of(this).get(AllotmentActivityVM.class);
        viewModel.getAllotmentList().observe(this, new Observer<List<Allotment>>() {
            @Override
            public void onChanged(List<Allotment> allotments) {
                hideListProgressBar();
                adapter.setData(allotments);
            }
        });
    }

    private void linkViews() {

        recyclerView=findViewById(R.id.allotment_recycler);
        progressBar=findViewById(R.id.allotment_progress_bar);
    }

    private void initViews() {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new AllotmentAdapter(this,this);
        recyclerView.addItemDecoration(new SpacingDecoration(this,SpacingDecoration.VERTICAL,0,16,26));
        recyclerView.setAdapter(adapter);
        showListProgressBar();

        FloatingActionButton fab=findViewById(R.id.allotment_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewAllotment();
            }
        });


    }

    private void setupToolBar() {

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Allotments");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return false;

    }

    @Override
    public void onRecyclerItemClicked(Allotment item, int position) {


        if(item.getState()==Allotment.STATE_ALLOTTED){

            ApproveAllotmentActivity.start(this,item);

        }

        if(item.getState()==Allotment.STATE_APPROVED || item.getState()==Allotment.STATE_PICKED_UP){

            FillCylindersActivity.start(this,item);

        }

        if(item.getState()==Allotment.STATE_READY_FOR_INVOICE){

            InvoiceActivity.start(this,item);

        }

    }

    @Override
    public void onRecyclerItemLongClicked(final Allotment item, int position, View view) {

        if(TransactionRunnerService.isRunning())
            return;

        PopupMenu popupMenu=new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.mnu_delete,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {

            if(menuItem.getItemId()==R.id.mnu_delete){

                deleteAllotment(item);
                return true;
            }

            return false;
        });
        popupMenu.show();

    }

    private void showListProgressBar(){
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideListProgressBar(){
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    protected void onCreateNewAllotment(){

        Intent intent=new Intent(this,SelectClientActivity.class);
        startActivity(intent);

    }

    // *************** Operations

    private void deleteAllotment(Allotment allotment){

        viewModel.setAllotmentToDelete(allotment);
        DeleteAllotmentFragment deleteFragment=new DeleteAllotmentFragment();
        deleteFragment.show(getSupportFragmentManager(),"deleteFragment");

    }
}
