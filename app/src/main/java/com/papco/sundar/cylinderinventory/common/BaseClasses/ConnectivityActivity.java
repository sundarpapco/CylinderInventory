package com.papco.sundar.cylinderinventory.common.BaseClasses;

import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.logic.ConnectionMonitor;

public class ConnectivityActivity extends AppCompatActivity implements ConnectionMonitor.InternetConnectivityListener {

    private ConnectionMonitor connectionMonitor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectionMonitor=new ConnectionMonitor(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(connectionMonitor,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionMonitor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionMonitor.clearCallBack();
    }

    @Override
    public void onInternetConnected() {

        ActionBar actionBar=getSupportActionBar();
        if(actionBar==null)
            return;

        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));

    }

    @Override
    public void onInternetDisconnected() {

        ActionBar actionBar=getSupportActionBar();
        if(actionBar==null)
            return;

        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.ConnectionFailRed)));

    }
}
