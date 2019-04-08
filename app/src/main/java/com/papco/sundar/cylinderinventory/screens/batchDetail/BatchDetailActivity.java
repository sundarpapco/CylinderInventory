package com.papco.sundar.cylinderinventory.screens.batchDetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.screens.common.batchDetailView.CylinderTypeDetailedView;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class BatchDetailActivity extends ConnectivityActivity {


    //region static starter ----------------------------------

    private static String KEY_BATCH_ID = "batch_id";
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
        bundle.putLong(KEY_BATCH_ID, batch.getId());

        intent.putExtras(bundle);

        context.startActivity(intent);

    }

    //endregion

    private Batch loadedBatch;


    private TextView heading, clientName, cylinderCount, batchNumber, timestamp;
    private LinearLayout container;
    private ProgressBar transactionProgressBar, loadingProgressBar;
    private BatchDetailVM viewModel;
    private ConstraintLayout headerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.batch_details);
        linkViews();
        initViews();
        setupToolBar();
        initViewModel(savedInstanceState);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    private void linkViews() {

        heading = findViewById(R.id.batch_detail_batch_heading);
        clientName = findViewById(R.id.batch_detail_client_name);
        cylinderCount = findViewById(R.id.batch_detail_cylinder_count);
        batchNumber = findViewById(R.id.batch_detail_batch_number);
        timestamp = findViewById(R.id.batch_detail_time_stamp);
        transactionProgressBar = findViewById(R.id.batch_detail_progress_bar);
        loadingProgressBar = findViewById(R.id.batch_detail_loading_progress);
        container = findViewById(R.id.batch_detail_container);
        headerView = findViewById(R.id.batch_detail_header_view);
    }

    private void initViews() {

        clientName.setText(getBatch().getDestinationName());
        cylinderCount.setText(Integer.toString(getBatch().getNoOfCylinders()) + " Cylinders");
        batchNumber.setText(getBatch().getBatchNumber());
        timestamp.setText(getBatch().getStringTimeStamp());
        initalizeHeader();

    }

    private void setupToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        actionBar.setTitle("Transaction detail");
    }

    private void initViewModel(Bundle savedInstanceState) {

        viewModel = ViewModelProviders.of(this).get(BatchDetailVM.class);
        viewModel.getBatch().observe(this, new Observer<Batch>() {
            @Override
            public void onChanged(Batch batch) {

                if (batch == null) {
                    showAlertAndClose();
                    return;
                }

                loadCylindersFromBatch(batch);
                hideLoadingProgressBar();

            }
        });

        showLoadingProgressBar();
        if (savedInstanceState == null) {
            viewModel.loadBatch(getBatch().getBatchNumber());
        }

    }

    private void loadCylindersFromBatch(Batch batch) {

        List<List<Cylinder>> masterList;
        masterList = batch.getTypedMasterList();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, 13, getResources().getDisplayMetrics());
        params.bottomMargin = (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        CylinderTypeDetailedView detailedView = null;

        for (List<Cylinder> monoList : masterList) {

            try {
                detailedView = new CylinderTypeDetailedView(this, monoList, getBatch().getType());
            } catch (Exception e) {
                Msg.show(this, e.getMessage());
                finish();
            }

            container.addView(detailedView.getView(), params);

        }
    }

    private void initalizeHeader() {

        @ColorInt int backgroundColor=0;
        switch (getBatch().getType()) {

            case Batch.TYPE_INVOICE:
                backgroundColor = ContextCompat.getColor(this, R.color.invoice_green);
                heading.setText("Invoice");
                break;

            case Batch.TYPE_ECR:
                backgroundColor = ContextCompat.getColor(this, R.color.ecr_blue);
                heading.setText("Ecr");
                break;

            case Batch.TYPE_REFILL:
                backgroundColor = ContextCompat.getColor(this, R.color.ref_pink);
                heading.setText("Refilling");
                break;

            case Batch.TYPE_FCI:
                backgroundColor = ContextCompat.getColor(this, R.color.fci_green);
                heading.setText("Full cylinder inward");
                break;

            case Batch.TYPE_REPAIR:
                backgroundColor = ContextCompat.getColor(this, R.color.rep_red);
                heading.setText("Repairing");
                break;

            case Batch.TYPE_RCI:
                backgroundColor = ContextCompat.getColor(this, R.color.rci_orange);
                heading.setText("Repaired cylinder inward");
                break;
        }

        headerView.setBackground(new ColorDrawable(backgroundColor));

    }

    private void showLoadingProgressBar() {
        container.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingProgressBar() {
        container.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
    }

    private void showAlertAndClose() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Someone deleted this transaction and needs to close now");
        builder.setPositiveButton("CLOSE", (dialog, which) -> finish());
        builder.setTitle("Alert");
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialog1 -> finish());
        dialog.show();

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
        loadedBatch.setId(args.getLong(KEY_BATCH_ID, -1));
        loadedBatch.setCylinders(null);

        return loadedBatch;
    }


}
