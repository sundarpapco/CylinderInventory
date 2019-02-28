package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.TestTransaction;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.screens.destinations.clients.ClientsActivity;
import com.papco.sundar.cylinderinventory.screens.cylinders.CylindersActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.refills.RefillsActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.repairs.RepairStationsActivity;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.EcrActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.FsiActivity;
import com.papco.sundar.cylinderinventory.screens.operations.inward.RepInActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.refill.SelectRefillStationActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.repair.SelectRepairStationActivity;

public class MainActivity extends AppCompatActivity {

    public static final String NOTIFICATION_CHANNEL_ID="transactionChannelID";
    public static final String NOTIFICATION_CHANNEL_NAME="Cylinder Inventory";
    public static final String NOTIFICATION_CHANNEL_DESC="Notifications about transactions";

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolBar();
        setupDrawer();
        createNotificationChannel();
        FirebaseApp.initializeApp(getApplicationContext());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupToolBar(){

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

    }

    private void setupDrawer(){

        drawerLayout=findViewById(R.id.drawer_layout);
        NavigationView navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                boolean result=false;

                switch (menuItem.getItemId()){
                    case R.id.mnu_drawer_cylinders:
                        showActivity(CylindersActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_clients:
                        showActivity(ClientsActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_refilling_stations:
                        showActivity(RefillsActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_repair_stations:
                        showActivity(RepairStationsActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_fci:
                        showActivity(FsiActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_ecr:
                        showActivity(EcrActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_repin:
                        showActivity(RepInActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_refill:
                        showActivity(SelectRefillStationActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_repout:
                        showActivity(SelectRepairStationActivity.class);
                        result=true;
                        break;

                    case R.id.mnu_drawer_allotment:
                        showActivity(AllotmentActivity.class);
                        result=true;
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return result;
            }
        });

    }

    private void showActivity(Class<?> className){

        Intent intent=new Intent(this,className);
        startActivity(intent);

    }

    private void createNotificationChannel(){

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


    public void onTest(View view){

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        db.runTransaction(new TestTransaction()).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Msg.show(MainActivity.this,"Success");
                }else{
                    FirebaseFirestoreException exception=(FirebaseFirestoreException)task.getException();

                    if(exception.getCode()==FirebaseFirestoreException.Code.ABORTED)
                        Msg.show(MainActivity.this,"abort code found");
                    else
                        Msg.show(MainActivity.this,task.getException().getMessage());
                }
            }
        });

    }
}
