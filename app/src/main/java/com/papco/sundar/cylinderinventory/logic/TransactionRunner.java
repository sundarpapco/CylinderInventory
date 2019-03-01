package com.papco.sundar.cylinderinventory.logic;

public interface TransactionRunner extends TransactionRunnerService.TransactionListener {

    void startService(String successMsg,String failureMsg,int requestCode);
    void onServiceBinded(TransactionRunnerService service);
    void clearCallbacks();
}
