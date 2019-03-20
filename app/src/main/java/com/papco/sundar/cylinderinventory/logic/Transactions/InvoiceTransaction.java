package com.papco.sundar.cylinderinventory.logic.Transactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InvoiceTransaction extends BaseTransaction {

    private List<Cylinder> cylinders;
    private int clientId;
    private int allottmentId;

    public InvoiceTransaction(int clientId, int allottmentId) {
        this.clientId = clientId;
        cylinders=new ArrayList<>();
        this.allottmentId=allottmentId;
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        long timestamp= Calendar.getInstance().getTimeInMillis();

        List<Integer> cylinderNumbers=new ArrayList<>(); // needed to set in batch document
        List<DocumentSnapshot> prefetchCylinders =getPrefetchedDocuments();
        if(prefetchCylinders==null)
            throw new FirebaseFirestoreException("No prefetch found",FirebaseFirestoreException.Code.CANCELLED);

        //region checking client and loading if valid ------------------------------

        DocumentReference clientRef=db.collection(DbPaths.COLLECTION_DESTINATIONS).document(Integer.toString(clientId));
        DocumentSnapshot clientSnapshot;
        Destination client;

        //get the client document first since we need to fill client info into cylinders
        clientSnapshot=transaction.get(clientRef);
        if(!clientSnapshot.exists())
            throw new FirebaseFirestoreException("Invalid client. Please check",
                FirebaseFirestoreException.Code.CANCELLED);
        else {
            client = clientSnapshot.toObject(Destination.class);
            client.setCylinderCount(client.getCylinderCount()+prefetchCylinders.size());
            client.setEditable(false);
        }
        //endregion checking client and loading ----------------------------------

        //region checking all the prefetch cylinders --------------------------------------


        DocumentSnapshot snapshot;
        Cylinder cylinder;

        for(int i=0;i<prefetchCylinders.size();++i){

            snapshot=prefetchCylinders.get(i);
            if(!snapshot.exists())
                throw new FirebaseFirestoreException("Invalid cylinder number found. Please check",
                        FirebaseFirestoreException.Code.CANCELLED);
            cylinder=snapshot.toObject(Cylinder.class);

            if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE)
                throw new FirebaseFirestoreException("Some cylinders not in warehouse. Please check",
                        FirebaseFirestoreException.Code.CANCELLED);

            if(cylinder.isEmpty())
                throw new FirebaseFirestoreException("Empty cylinders found. Please check",
                        FirebaseFirestoreException.Code.CANCELLED);


            cylinder.setLocationId(client.getId());
            cylinder.setLocationName(client.getName());
            cylinder.setLastTransaction(timestamp);
            cylinders.add(cylinder);
            cylinderNumbers.add(cylinder.getCylinderNo());
        }

        //endregion checking all prefetch cylinders --------------------------------------------

        //region getting, checking and initializing all the batch documents needed ------------------------------

        DocumentReference counterBatchRef=db.document(DbPaths.COUNT_BATCHES_INVOICE);
        DocumentReference counterFullCylinderRef=db.document(DbPaths.COUNT_CYLINDERS_FULL);
        DocumentReference counterClientsCylinderRef=db.document(DbPaths.COUNT_CYLINDERS_CLIENT);
        DocumentSnapshot counterBatchSnapshot;
        DocumentSnapshot counterFullCylinderSnapshot;
        DocumentSnapshot counterClientsCylinderSnapshot;
        Aggregation batchAgg;
        Aggregation fullCylinderAgg;
        Aggregation clientCylinderAgg;

        counterBatchSnapshot=transaction.get(counterBatchRef);
        counterFullCylinderSnapshot=transaction.get(counterFullCylinderRef);
        counterClientsCylinderSnapshot=transaction.get(counterClientsCylinderRef);

        if(counterBatchSnapshot.exists()) {
            batchAgg = counterBatchSnapshot.toObject(Aggregation.class);
            batchAgg.setCount(batchAgg.getCount()+1);
        }else{
            batchAgg=new Aggregation();
            batchAgg.setType(Aggregation.TYPE_BATCH);
            batchAgg.setCount(1);
        }

        if(counterFullCylinderSnapshot.exists()) {
            fullCylinderAgg = counterFullCylinderSnapshot.toObject(Aggregation.class);
            fullCylinderAgg.setCount(fullCylinderAgg.getCount()-prefetchCylinders.size());
        }else{
            fullCylinderAgg=new Aggregation();
            fullCylinderAgg.setType(Aggregation.TYPE_CYLINDERS);
            fullCylinderAgg.setCount(0);
        }

        if(counterClientsCylinderSnapshot.exists()) {
            clientCylinderAgg = counterClientsCylinderSnapshot.toObject(Aggregation.class);
            clientCylinderAgg.setCount(clientCylinderAgg.getCount()+prefetchCylinders.size());
        }else{
            clientCylinderAgg=new Aggregation();
            clientCylinderAgg.setType(Aggregation.TYPE_CYLINDERS);
            clientCylinderAgg.setCount(prefetchCylinders.size());
        }

        //endregion region getting, checking and initializing all the batch documents needed ------------------------------

        Batch invoice=new Batch();
        invoice.setId(batchAgg.getCount());
        invoice.setTimestamp(timestamp);
        invoice.setNoOfCylinders(cylinderNumbers.size());
        invoice.setDestinationId(client.getId());
        invoice.setDestinationName(client.getName());
        invoice.setType(Batch.TYPE_INVOICE);
        invoice.setCylinders(cylinderNumbers);
        DocumentReference invoiceRef=db.collection(DbPaths.COLLECTION_BATCHES).document(invoice.getBatchNumber());

        //region writing all values ------------------------------------

        DocumentReference cylRef;
        for(Cylinder cyl:cylinders){
            cylRef=db.collection(DbPaths.COLLECTION_CYLINDERS).document(cyl.getStringId());
            transaction.set(cylRef,cyl);
        }

        DocumentReference allotmentRef=db.collection(DbPaths.COLLECTION_ALLOTMENT).document(Integer.toString(allottmentId));

        transaction.set(clientRef,client);
        transaction.set(counterBatchRef,batchAgg);
        transaction.set(counterFullCylinderRef,fullCylinderAgg);
        transaction.set(counterClientsCylinderRef,clientCylinderAgg);
        transaction.set(invoiceRef,invoice);
        transaction.delete(allotmentRef);


        //endregion writing all values -------------------------------------

        return null;
    }


}
