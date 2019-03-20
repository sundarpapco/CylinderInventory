package com.papco.sundar.cylinderinventory.common.BaseClasses;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;

import java.util.List;

public class TransactionActivity extends ConnectivityActivity implements TransactionRunnable{

    private boolean hasPendingWork=false;
    private TransactionRunnerService transactionService;
    private TransactionServiceConnection connection;


    @Override
    public void onStop() {
        super.onStop();

        unBindFromService();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(hasPendingWork && TransactionRunnerService.isRunning()){
            bindToService();
        }else{
            hasPendingWork=false;
            hideTransactionProgressBar();
        }
    }

    @Override
    public void showTransactionProgressBar() {

    }

    @Override
    public void hideTransactionProgressBar() {

    }

    @Override
    public final void startTransaction(String successMsg, String progressMsg, String failureMsg, int requestCode) {

        if(TransactionRunnerService.isRunning()){
            Msg.show(this,"Already a transaction is running. Please try a bit later");
            return;
        }

        Intent startIntent = TransactionRunnerService.getStartingIntent(
                this, successMsg,progressMsg, failureMsg,requestCode);

        this.startService(startIntent);

        bindToService();

    }

    @Override
    public final void stopTransaction() {
        Intent intent=TransactionRunnerService.getStoppingIntent(this);
        unBindFromService();
        startService(intent);
    }

    @Override
    public final void onServiceBinded(TransactionRunnerService service) {

        service.setCallback(this);
        if(hasPendingWork)
            return;

        BaseTransaction transaction=getTransactionToRun(service.getRequestCode());

        if(transaction==null){
            stopTransaction();
            return;
        }

        if(getPrefetchList()!=null)
            service.setPrefetchList(getPrefetchList());

        hasPendingWork=true;
        showTransactionProgressBar();
        service.startTransaction(transaction);

    }

    @Override
    public final void bindToService() {

        Intent bindIntent = new Intent(this, TransactionRunnerService.class);

        if(connection==null)
            connection = new TransactionServiceConnection();

        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public final void unBindFromService() {

        if(transactionService!=null)
            transactionService.clearCallback();

        if(connection!=null)
            unbindService(connection);

        transactionService=null;
        connection=null;

    }

    @Override
    public List<Integer> getPrefetchList() {
        return null;
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        return null;
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {

        hasPendingWork=false;
        unBindFromService();
        hideTransactionProgressBar();

    }


    class TransactionServiceConnection implements ServiceConnection {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            transactionService = ((TransactionRunnerService.TransactionBinder) iBinder).getService();
            onServiceBinded(transactionService);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            transactionService = null;
            connection=null;

        }
    }


}
