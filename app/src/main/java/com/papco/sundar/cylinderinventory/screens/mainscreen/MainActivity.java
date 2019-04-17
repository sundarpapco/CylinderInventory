package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.screens.batchDetail.BatchDetailActivity;
import com.papco.sundar.cylinderinventory.screens.cylinders.CylindersActivity;
import com.papco.sundar.cylinderinventory.screens.cylinders.cylinderTypes.CylinderTypeActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.clients.ClientsActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.refills.RefillsActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.repairs.RepairStationsActivity;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.EcrActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.FciActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.RciActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.refill.SelectRefillStationActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.repair.SelectRepairStationActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends ConnectivityActivity implements BatchFeedAdapter.FeedAdapterCallBack, DatePickerDialog.OnDateSetListener {

    public static final String NOTIFICATION_CHANNEL_ID = "transactionChannelID";
    public static final String NOTIFICATION_CHANNEL_NAME = "Cylinder Inventory";
    public static final String NOTIFICATION_CHANNEL_DESC = "Notifications about transactions";

    public static final int FILTER_NONE = 0;
    public static final int FILTER_INVOICE = 1;
    public static final int FILTER_ECR = 2;
    public static final int FILTER_REFILL = 3;
    public static final int FILTER_FCI = 4;
    public static final int FILTER_REPAIR = 5;
    public static final int FILTER_RCI = 6;
    private final String KEY_SELECTED_FILTER = "selected_filter";
    private final String KEY_TIMELINE_FILTER = "timeline_filter";

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private BatchFeedAdapter adapter;
    private ProgressBar progressBar;
    private ConstraintLayout timelineFilterView;
    private TextView timeline_filter_info;
    private AppCompatImageView timelineFilterClose;
    private int typeFilter = 0;
    private long timelineFilter = -1;

    private MainActivityVM viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(MainActivityVM.class);
        linkViews();
        restoreFilters(savedInstanceState);
        initViews();
        setupToolBar();
        setupDrawer();
        createNotificationChannel();
        FirebaseApp.initializeApp(getApplicationContext());

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_SELECTED_FILTER, typeFilter);
        outState.putLong(KEY_TIMELINE_FILTER, timelineFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.mnu_main_screen, menu);
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
                drawerLayout.openDrawer(GravityCompat.START);
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

            case R.id.mnu_main_screen_search_batch_number:
                showSearchByBatchNumberFragment();
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
        adapter = new BatchFeedAdapter(getApplicationContext(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        BatchFeedScrollListener scrollListener = new BatchFeedScrollListener(layoutManager);
        adapter.setDataSource(viewModel.getFeedDataSource());
        adapter.setScrollListener(scrollListener);

        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setAdapter(adapter);
        adapter.setFilters(typeFilter,timelineFilter);

        if (timelineFilter == -1)
            hideTimelineFilterView();
        else
            showTimelineFilterView();


        timelineFilterClose.setOnClickListener(v -> clearTimelineFilter());
    }

    private void restoreFilters(Bundle savedInstanceState){

        if (savedInstanceState == null) {
            typeFilter = FILTER_NONE;
            timelineFilter = -1;
        } else {
            typeFilter = savedInstanceState.getInt(KEY_SELECTED_FILTER);
            timelineFilter = savedInstanceState.getLong(KEY_TIMELINE_FILTER);
        }

    }


    private void setTimeLineFilter(long chosenTime){

        timelineFilter=chosenTime;
        showTimelineFilterView();
        adapter.setFilters(typeFilter,timelineFilter);
    }

    private void clearTimelineFilter() {

        timelineFilter = -1;
        hideTimelineFilterView();
        adapter.setFilters(typeFilter,-1);
    }

    private void hideTimelineFilterView() {
        timelineFilterView.setVisibility(View.GONE);
    }

    private void showTimelineFilterView() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        timelineFilterView.setVisibility(View.VISIBLE);
        timeline_filter_info.setText("Results starting from " + dateFormat.format(timelineFilter));

    }

    private void showDatePickerDialog(){

        DatePickerDialogFragment datePicker=new DatePickerDialogFragment();
        datePicker.show(getSupportFragmentManager(),"datePicker");

    }

    private void setTypeFilter(int typeFilter){

        if (typeFilter == this.typeFilter)
            return;
        else
            this.typeFilter=typeFilter;

        setTitle();
        adapter.setFilters(typeFilter,timelineFilter);
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
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setTitle(getTitleForTypeFilter(typeFilter));

    }

    private void setupDrawer() {

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {

            boolean result = false;

            switch (menuItem.getItemId()) {
                case R.id.mnu_drawer_cylinders:
                    showActivity(CylindersActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_cylinder_types:
                    showActivity(CylinderTypeActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_clients:
                    showActivity(ClientsActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_refilling_stations:
                    showActivity(RefillsActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_repair_stations:
                    showActivity(RepairStationsActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_fci:
                    showActivity(FciActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_ecr:
                    showActivity(EcrActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_repin:
                    showActivity(RciActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_refill:
                    showActivity(SelectRefillStationActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_repout:
                    showActivity(SelectRepairStationActivity.class);
                    result = true;
                    break;

                case R.id.mnu_drawer_allotment:
                    showActivity(AllotmentActivity.class);
                    result = true;
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return result;
        });

    }

    private void showActivity(Class<?> className) {

        Intent intent = new Intent(this, className);
        startActivity(intent);

    }

    private void createNotificationChannel() {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setTitle(){

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getTitleForTypeFilter(typeFilter));

    }

    private String getTitleForTypeFilter(int typeFilter){

        switch (typeFilter) {

            case FILTER_NONE:
                return  "Live Summary";

            case FILTER_INVOICE:
                return "Invoice summary";

            case FILTER_ECR:
                return  "Ecr summary";

            case FILTER_REFILL:
                return  "Refill summary";

            case FILTER_FCI:
                return  "Fci summary";

            case FILTER_REPAIR:
                return  "Repair summary";

            case FILTER_RCI:
                return  "Rci summary";
        }

        return "Live Feed";

    }

    private void showSearchByBatchNumberFragment() {

        BatchNumberInputFragment batchNumberInputFragment = new BatchNumberInputFragment();
        batchNumberInputFragment.setArguments(
                BatchNumberInputFragment.getArguments("Batch number", "", "SEARCH"));
        batchNumberInputFragment.show(getSupportFragmentManager(), "batchNumberSearchFragment");

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

        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);

        setTimeLineFilter(calendar.getTimeInMillis());

    }
}
