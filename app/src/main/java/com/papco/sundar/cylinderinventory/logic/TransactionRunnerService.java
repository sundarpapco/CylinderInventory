package com.papco.sundar.cylinderinventory.logic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.screens.mainscreen.MainActivity;

import java.lang.ref.WeakReference;

public class TransactionRunnerService extends Service {

    public static Intent getStartingIntent(Context context,
                                           String successMessage,
                                           String progressMessage,
                                           String failureMessage,
                                           int requestCode){

        Bundle bundle=new Bundle();
        bundle.putString(KEY_SUCCESS_MESSAGE,successMessage);
        bundle.putString(KEY_FAILURE_MESSAGE,failureMessage);
        bundle.putString(KEY_PROGRESS_MESSAGE,progressMessage);
        bundle.putInt(KEY_REQUEST_CODE,requestCode);
        Intent intent=new Intent(context,TransactionRunnerService.class);
        intent.setAction(ACTION_START_SERVICE);
        intent.putExtras(bundle);
        return intent;

    }

    public static Intent getStoppingIntent(Context context){

        Intent intent=new Intent(context,TransactionRunnerService.class);
        intent.setAction(ACTION_STOP_SERVICE);
        return intent;
    }

    private static final String ACTION_START_SERVICE = "com.papco.sundar.cylinderinventory.starttransactionservice";
    private static final String ACTION_STOP_SERVICE = "com.papco.sundar.cylinderinventory.stoptransactionservice";
    private static final String KEY_SUCCESS_MESSAGE="success_message";
    private static final String KEY_FAILURE_MESSAGE="failure_message";
    private static final String KEY_PROGRESS_MESSAGE="progress_message";
    private static final String KEY_REQUEST_CODE="request_code";

    //-------------------------- *** ----------------------------------


    private static boolean isRunning = false;
    private final int NOTIFICATION_ID = 2;

    private NotificationCompat.Builder builder;
    private Transaction.Function<Void> transaction;
    private TransactionListener callback = null;
    private String successMessage;
    private String failureMessage;
    private String progressMessage;
    private TransactionBinder binder;
    private int requestCode;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_START_SERVICE)) {

            if (isRunning()) {
                Msg.show(this, "Some operation already running. Try again later");
                return START_STICKY;
            } else {
                initializeService(intent);
            }
        }

        if(intent.getAction().equals(ACTION_STOP_SERVICE)){
            stopServiceSuccess();
            return START_STICKY;
        }

        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null)
            binder = new TransactionBinder();

        return binder;
    }

    private void initializeService(Intent intent) {

        isRunning = true;

        successMessage=intent.getExtras().getString(KEY_SUCCESS_MESSAGE,"Transaction success");
        failureMessage=intent.getExtras().getString(KEY_FAILURE_MESSAGE,"Transacion failed. Check intener conection");
        progressMessage=intent.getExtras().getString(KEY_PROGRESS_MESSAGE,"Running Transaction");
        requestCode=intent.getExtras().getInt(KEY_REQUEST_CODE,-1);
        showNotification(); //This will make the service foreground
    }

    private void showNotification() {

        builder = new NotificationCompat.Builder(this, MainActivity.NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle(progressMessage);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        startForeground(NOTIFICATION_ID, builder.build());

    }

    public void startTransaction(Transaction.Function<Void> transaction) {

        if (this.transaction != null)
            return;
        else
            this.transaction=transaction;

        Log.d("SUNDAR", "starting transaction work ");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.runTransaction(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    if (callback!=null)
                        callback.onTransactionComplete(task,requestCode);
                    else
                        Msg.show(TransactionRunnerService.this,successMessage);

                    stopServiceSuccess();

                } else {

                    if (callback!=null) {
                        callback.onTransactionComplete(task,requestCode);
                        stopServiceSuccess();
                    } else {
                        Msg.show(TransactionRunnerService.this, failureMessage);
                        stopServiceFailure();
                    }
                }
            }
        });
    }

    public void setCallback(TransactionListener callback){
        this.callback=callback;
    }

    public void clearCallback(){
        callback=null;
    }

    private void stopServiceFailure() {

        isRunning=false;

        transaction=null;
        callback=null;
        builder.setContentTitle("Transaction failed");
        builder.setContentText(failureMessage);
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
        stopForeground(false);
        stopSelf();

    }

    private void stopServiceSuccess() {

        isRunning=false;

        transaction=null;
        callback=null;
        stopForeground(true);
        stopSelf();

    }

    public static boolean isRunning() {
        return isRunning;
    }

    public int getRequestCode(){
        return requestCode;
    }

    //---------------- Nested declarations below

    public interface TransactionListener {

        void onTransactionComplete(Task<Void> task,int requestCode);
    }

    public class TransactionBinder extends Binder {

        public TransactionRunnerService getService() {

            return TransactionRunnerService.this;
        }
    }
}
