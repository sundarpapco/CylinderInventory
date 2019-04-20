package com.papco.sundar.cylinderinventory.screens.cylinders.history;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.screens.batchDetail.BatchDetailActivity;
import com.papco.sundar.cylinderinventory.screens.mainscreen.BatchFeedScrollListener;
import com.papco.sundar.cylinderinventory.screens.mainscreen.DatePickerDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CylinderHistoryActivity extends ConnectivityActivity
        implements CylHistoryAdapter.Callback, DatePickerDialog.OnDateSetListener {

    public static void start(Context context,int cylinderNumber){

        Bundle args=new Bundle();
        args.putInt("cylinder_number",cylinderNumber);
        Intent intent=new Intent(context,CylinderHistoryActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);

    }


    public static final int FILTER_NONE = 0;
    public static final int FILTER_INVOICE = 1;
    public static final int FILTER_ECR = 2;
    public static final int FILTER_REFILL = 3;
    public static final int FILTER_FCI = 4;
    public static final int FILTER_REPAIR = 5;
    public static final int FILTER_RCI = 6;
    private final String KEY_SELECTED_FILTER = "selected_filter";
    private final String KEY_TIMELINE_FILTER = "timeline_filter";

    private RecyclerView recyclerView;
    private CylHistoryAdapter adapter;
    private ProgressBar progressBar;
    private ConstraintLayout timelineFilterView;
    private TextView timeline_filter_info;
    private AppCompatImageView timelineFilterClose;
    private int typeFilter = 0;
    private long timelineFilter = -1;

    private CylinderHistoryVM viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(CylinderHistoryVM.class);
        linkViews();
        restoreFilters(savedInstanceState);
        initViews();
        setupToolBar();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_SELECTED_FILTER, typeFilter);
        outState.putLong(KEY_TIMELINE_FILTER, timelineFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.mnu_cyl_history, menu);
        menu.getItem(1).getSubMenu().getItem(typeFilter).setChecked(true);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.onConfigChange();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.mnu_feed_filter_all:
                setTypeFilter(FILTER_NONE);
                break;

            case R.id.mnu_feed_filter_invoice:
                setTypeFilter(FILTER_INVOICE);
                break;

            case R.id.mnu_feed_filter_Ecr:
                setTypeFilter(FILTER_ECR);
                break;

            case R.id.mnu_feed_filter_refill:
                setTypeFilter(FILTER_REFILL);
                break;

            case R.id.mnu_feed_filter_fci:
                setTypeFilter(FILTER_FCI);
                break;

            case R.id.mnu_feed_filter_repair:
                setTypeFilter(FILTER_REPAIR);
                break;

            case R.id.mnu_feed_filter_rci:
                setTypeFilter(FILTER_RCI);
                break;

            case R.id.mnu_main_screen_search_by_date:
                showDatePickerDialog();
                break;

        }

        return true;
    }

    private void linkViews() {

        recyclerView = findViewById(R.id.main_feed_recycler);
        progressBar = findViewById(R.id.main_progress_bar);
        timelineFilterView = findViewById(R.id.timeline_filter_view);
        timeline_filter_info = findViewById(R.id.timeline_filter_info);
        timelineFilterClose = findViewById(R.id.timeline_filter_close);
    }

    private void initViews() {

        SpacingDecoration decoration = new SpacingDecoration(this, SpacingDecoration.VERTICAL, 18, 12, 24);
        adapter = new CylHistoryAdapter(getApplicationContext(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        BatchFeedScrollListener scrollListener = new BatchFeedScrollListener(layoutManager);
        adapter.setDataSource(viewModel.getHistoryDataSource());
        adapter.setScrollListener(scrollListener);

        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setAdapter(adapter);
        adapter.setFilters(typeFilter, timelineFilter,getCylinderNumber());

        if (timelineFilter == -1)
            hideTimelineFilterView();
        else
            showTimelineFilterView();


        timelineFilterClose.setOnClickListener(v -> clearTimelineFilter());
    }

    private void restoreFilters(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            typeFilter = FILTER_NONE;
            timelineFilter = -1;
        } else {
            typeFilter = savedInstanceState.getInt(KEY_SELECTED_FILTER);
            timelineFilter = savedInstanceState.getLong(KEY_TIMELINE_FILTER);
        }

    }


    private void setTimeLineFilter(long chosenTime) {

        timelineFilter = chosenTime;
        showTimelineFilterView();
        adapter.setFilters(typeFilter, timelineFilter,getCylinderNumber());
    }

    private void clearTimelineFilter() {

        timelineFilter = -1;
        hideTimelineFilterView();
        adapter.setFilters(typeFilter, -1,getCylinderNumber());
    }

    private void hideTimelineFilterView() {
        timelineFilterView.setVisibility(View.GONE);
    }

    private void showTimelineFilterView() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        timelineFilterView.setVisibility(View.VISIBLE);
        timeline_filter_info.setText("Results starting from " + dateFormat.format(timelineFilter));

    }

    private void showDatePickerDialog() {

        DatePickerDialogFragment datePicker = new DatePickerDialogFragment();
        datePicker.show(getSupportFragmentManager(), "datePicker");

    }

    private void setTypeFilter(int typeFilter) {

        if (typeFilter == this.typeFilter)
            return;
        else
            this.typeFilter = typeFilter;

        setTitle();
        adapter.setFilters(typeFilter, timelineFilter,getCylinderNumber());
        invalidateOptionsMenu();

    }


    private void showProgressBar() {

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

    }

    private void hideProgressBar() {

        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

    }

    private void setupToolBar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        actionBar.setTitle("Cylinder "+Integer.toString(getCylinderNumber())+" history");

    }

    private void setTitle() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getTitleForTypeFilter(typeFilter));

    }

    private String getTitleForTypeFilter(int typeFilter) {

        String result="Cylinder "+Integer.toString(getCylinderNumber())+": ";

        switch (typeFilter) {

            case FILTER_NONE:
                return "Cylinder "+Integer.toString(getCylinderNumber())+" history";

            case FILTER_INVOICE:
                return result+"Invoices";

            case FILTER_ECR:
                return result+"Ecr";

            case FILTER_REFILL:
                return result+"Refills";

            case FILTER_FCI:
                return result+"Fci";

            case FILTER_REPAIR:
                return result+"Repairs";

            case FILTER_RCI:
                return result+"Rci";
        }

        return "Cylinder "+Integer.toString(getCylinderNumber())+" history";

    }


    @Override
    public void onRecyclerItemClicked(Batch item, int position) {

        BatchDetailActivity.start(this, item);

    }

    @Override
    public void onRecyclerItemLongClicked(Batch item, int position, View view) {


    }


    @Override
    public void onStartLoadingData() {
        showProgressBar();
    }

    @Override
    public void onFinishLoadingData() {
        hideProgressBar();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        //setting the calendar to the last millisecond of the selected date so that the query
        //can be constructed from it

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        setTimeLineFilter(calendar.getTimeInMillis());

    }

    private int getCylinderNumber(){

        Bundle args=getIntent().getExtras();
        if(args==null)
            return -1;

        return args.getInt("cylinder_number",-1);
    }

}
