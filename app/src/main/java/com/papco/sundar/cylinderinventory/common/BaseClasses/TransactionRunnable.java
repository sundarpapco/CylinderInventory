package com.papco.sundar.cylinderinventory.common.BaseClasses;

import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;

public interface TransactionRunnable extends TransactionRunnerService.TransactionListener {

    void showTransactionProgressBar();

    void hideTransactionProgressBar();

    void startTransaction(String successMsg,String progressMsg,String failureMsg,int requestCode);

    void stopTransaction();

    void bindToService();

    void unBindFromService();

    void onServiceBinded(TransactionRunnerService service);

    Transaction.Function getTransactionToRun(int requestCode);



}
