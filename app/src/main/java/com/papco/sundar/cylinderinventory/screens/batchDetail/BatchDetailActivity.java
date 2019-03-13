package com.papco.sundar.cylinderinventory.screens.batchDetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Batch;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BatchDetailActivity extends ConnectivityActivity {


    //region static starter ----------------------------------

    private static String KEY_BATCH_ID="batch_id";
    private static String KEY_BATCH_TYPE = "batch_type";
    private static String KEY_TIMESTAMP = "batch_timestamp";
    private static String KEY_DEST_NAME = "dest_name";
    private static String KEY_CYLINDER_COUNT = "cylinder_count";

    public static void start(@NonNull Context context, @NonNull Batch batch) {

        Intent intent = new Intent(context, BatchDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_BATCH_TYPE, batch.getType());
        bundle.putLong(KEY_TIMESTAMP, batch.getTimestamp());
        bundle.putString(KEY_DEST_NAME, batch.getDestinationName());
        bundle.putInt(KEY_CYLINDER_COUNT, batch.getNoOfCylinders());
        bundle.putLong(KEY_BATCH_ID,batch.getId());

        intent.putExtras(bundle);

        context.startActivity(intent);

    }

    //endregion

    private Batch loadedBatch;
    private CylinderDetailsAdapter adapter;
    private BatchDetailVM viewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.batch_details);
        linkViews();
        initViews();
        initToolBar();
        initViewModel();

    }

    private void linkViews() {

        recyclerView=findViewById(R.id.batch_detail_recycler);
        progressBar=findViewById(R.id.batch_detail_progress);

    }

    private void initViews() {

        TextView batchType = findViewById(R.id.batch_detail_batch_type);
        TextView batchNumber = findViewById(R.id.batch_detail_batch_number);
        TextView timestamp = findViewById(R.id.batch_detail_timestamp);
        TextView destinationName = findViewById(R.id.batch_detail_destination_name);
        TextView cylinderCount = findViewById(R.id.batch_detail_no_of_cylinders);

        Batch batch=getBatch();
        if(batch==null)
            return;

        switch (batch.getType()) {

            case Batch.TYPE_ECR:
                batchType.setText("Empty cylinder return");
                batchType.setTextColor(ContextCompat.getColor(this,R.color.ecr_blue));
                break;

            case Batch.TYPE_FCI:
                batchType.setText("Full cylinder Inward");
                batchType.setTextColor(ContextCompat.getColor(this,R.color.fci_green));
                break;

            case Batch.TYPE_INVOICE:
                batchType.setText("Invoice");
                batchType.setTextColor(ContextCompat.getColor(this,R.color.invoice_green));
                break;

            case Batch.TYPE_RCI:
                batchType.setText("Repaired cylinder Inward");
                batchType.setTextColor(ContextCompat.getColor(this,R.color.rci_orange));
                break;

            case Batch.TYPE_REFILL:
                batchType.setText("Sent for refilling");
                batchType.setTextColor(ContextCompat.getColor(this,R.color.ref_pink));
                break;

            case Batch.TYPE_REPAIR:
                batchType.setText("Sent for repair");
                batchType.setTextColor(ContextCompat.getColor(this,R.color.rep_red));
                break;
        }

        batchNumber.setText("Document No: "+batch.getBatchNumber());
        timestamp.setText(batch.getStringTimeStamp());
        destinationName.setText(batch.getDestinationName());
        cylinderCount.setText(Integer.toString(batch.getNoOfCylinders())+" Cylinders");


        adapter=new CylinderDetailsAdapter(this,getBatch().getType());
        RecyclerView recyclerView=findViewById(R.id.batch_detail_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this,6));
        recyclerView.addItemDecoration(new CylinderListSpacingDecoration());
        recyclerView.setAdapter(adapter);
    }

    private void initToolBar(){

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

    }

    private void initViewModel() {

        viewModel= ViewModelProviders.of(this).get(BatchDetailVM.class);
        showProgressBar();
        viewModel.getCylinderNumbers().observe(this, cylinders -> {

            if(cylinders==null){
                Msg.show(this,"Error connecting to server. Check internet connection");
                return;
            }

            adapter.setData(cylinders);
            hideProgressBar();
        });

        viewModel.loadCylinders(getBatch().getBatchNumber());

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home)
            finish();

        return false;

    }

    private Batch getBatch() {

        if (loadedBatch != null)
            return loadedBatch;

        Bundle args = getIntent().getExtras();

        if (getIntent().getExtras() == null)
            return null;

        loadedBatch = new Batch();
        loadedBatch.setType(args.getInt(KEY_BATCH_TYPE, -1));
        loadedBatch.setTimestamp(args.getLong(KEY_TIMESTAMP, 0));
        loadedBatch.setDestinationName(args.getString(KEY_DEST_NAME, "Null"));
        loadedBatch.setNoOfCylinders(args.getInt(KEY_CYLINDER_COUNT, 0));
        loadedBatch.setId(args.getLong(KEY_BATCH_ID,-1));
        loadedBatch.setCylinders(null);

        return loadedBatch;
    }

    private void showProgressBar(){

        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){

        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }


}
