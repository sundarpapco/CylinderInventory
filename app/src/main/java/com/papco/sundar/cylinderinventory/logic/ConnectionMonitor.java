package com.papco.sundar.cylinderinventory.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.papco.sundar.cylinderinventory.common.Msg;

public class ConnectionMonitor extends BroadcastReceiver {


    private InternetConnectivityListener callback;

    public ConnectionMonitor(InternetConnectivityListener callback) {

        this.callback=callback;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction()!= ConnectivityManager.CONNECTIVITY_ACTION)
            return;

        if(isInternetConnected(context)) {
            if (callback != null)
                callback.onInternetConnected();
            return;
        }
        else
            if(callback!=null)
                callback.onInternetDisconnected();


    }

    public static boolean isInternetConnected(Context context){

        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=cm.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnected();
    }

    public void clearCallBack(){
        callback=null;
    }

    public interface InternetConnectivityListener{

        void onInternetConnected();
        void onInternetDisconnected();
    }


}
