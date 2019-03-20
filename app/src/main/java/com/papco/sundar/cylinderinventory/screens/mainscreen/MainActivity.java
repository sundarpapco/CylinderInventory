package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.logic.LoadMoreListener;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.screens.batchDetail.BatchDetailActivity;
import com.papco.sundar.cylinderinventory.screens.cylinders.CylindersActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.clients.ClientsActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.refills.RefillsActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.repairs.RepairStationsActivity;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.EcrActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.FciActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.RciActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.refill.SelectRefillStationActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.repair.SelectRepairStationActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends ConnectivityActivity implements RecyclerListener<Batch> {

    public static final String NOTIFICATION_CHANNEL_ID = "transactionChannelID";
    public static final String NOTIFICATION_CHANNEL_NAME = "Cylinder Inventory";
    public static final String NOTIFICATION_CHANNEL_DESC = "Notifications about transactions";

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private BatchFeedAdapter adapter;
    private BatchFeedScrollListener scrollListener;
    private ProgressBar progressBar;

    private MainActivityVM viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel= ViewModelProviders.of(this).get(MainActivityVM.class);
        linkViews();
        initViews();
        setupToolBar();
        setupDrawer();
        createNotificationChannel();
        FirebaseApp.initializeApp(getApplicationContext());

        initViewModel();
        showProgressBar();
        viewModel.loadFirstPage();

    }

    private void initViewModel() {

        viewModel.getFirstPage().observe(this, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(QuerySnapshot querySnapshot) {
                adapter.setInitialData(querySnapshot);
                hideProgressBar();
            }
        });

        viewModel.getLoadedPage().observe(this, new Observer<List<DocumentSnapshot>>() {
            @Override
            public void onChanged(List<DocumentSnapshot> documentSnapshots) {
                adapter.addData(documentSnapshots);
                scrollListener.loadCompleted();
                if(documentSnapshots.size()<BatchFeedScrollListener.PAGE_SIZE)
                    scrollListener.setAllLoadingCOmplete();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //loadLiveFeed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void linkViews(){

        recyclerView=findViewById(R.id.main_feed_recycler);
        progressBar=findViewById(R.id.main_progress_bar);
    }

    private void initViews(){

        SpacingDecoration decoration=new SpacingDecoration(this,SpacingDecoration.VERTICAL,18,12,24);
        adapter=new BatchFeedAdapter(getApplicationContext(),this);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        scrollListener=new BatchFeedScrollListener(layoutManager, new LoadMoreListener() {
            @Override
            public void loadMoreData() {
                viewModel.loadNextPage(adapter.getLastLoadedDocument());
            }
        });

        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setAdapter(adapter);
    }

    private void showProgressBar(){

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

    }

    private void hideProgressBar(){

        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

    }

    private void setupToolBar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

    }

    private void setupDrawer() {

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                boolean result = false;

                switch (menuItem.getItemId()) {
                    case R.id.mnu_drawer_cylinders:
                        showActivity(CylindersActivity.class);
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
            }
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


    @Override
    public void onRecyclerItemClicked(Batch item, int position) {

        BatchDetailActivity.start(this,item);

    }

    @Override
    public void onRecyclerItemLongClicked(Batch item, int position, View view) {

    }
}
