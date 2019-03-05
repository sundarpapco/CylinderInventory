package com.papco.sundar.cylinderinventory.common.BaseClasses;

import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;

import java.util.List;

public interface TransactionRunnable extends TransactionRunnerService.TransactionListener {

    //showing and hiding the progress bar is handled automatically. Just implement these methods
    void showTransactionProgressBar();

    void hideTransactionProgressBar();

    //dont override this function in final class. Call this to start the transaction
    void startTransaction(String successMsg,String progressMsg,String failureMsg,int requestCode);

    //dont override this function in final class.
    void stopTransaction();

    void bindToService();

    void unBindFromService();

    void onServiceBinded(TransactionRunnerService service);

    List<Integer> getPrefetchList();

    BaseTransaction getTransactionToRun(int requestCode);

}
