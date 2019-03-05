package com.papco.sundar.cylinderinventory.logic.Transactions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

public class RefillTransaction extends BaseTransaction {

    private List<Cylinder> cylinders;
    private int clientId;

    public RefillTransaction(int clientId) {
        this.clientId = clientId;
        cylinders=new ArrayList<>();
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
            throw new FirebaseFirestoreException("Invalid refill station. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);
        else {
            client = clientSnapshot.toObject(Destination.class);
            client.setCylinderCount(client.getCylinderCount()+prefetchCylinders.size());
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

            if(!cylinder.isEmpty())
                throw new FirebaseFirestoreException("Non empty cylinders found. Please check",
                        FirebaseFirestoreException.Code.CANCELLED);

            if(cylinder.isDamaged())
                throw new FirebaseFirestoreException("Damaged cylinders found. Please check",
                        FirebaseFirestoreException.Code.CANCELLED);

            cylinder.setLocationId(client.getId());
            cylinder.setLocationName(client.getName());
            cylinder.setLastTransaction(timestamp);
            cylinders.add(cylinder);
            cylinderNumbers.add(cylinder.getCylinderNo());
        }

        //endregion checking all prefetch cylinders --------------------------------------------

        //region getting, checking and initializing all the batch documents needed ------------------------------

        DocumentReference counterBatchRef=db.document(DbPaths.COUNT_BATCHES_REFILL);
        DocumentReference counterEmptyCylinderRef=db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
        DocumentReference counterClientsCylinderRef=db.document(DbPaths.COUNT_CYLINDERS_REFILL_STATION);
        DocumentSnapshot counterBatchSnapshot;
        DocumentSnapshot counterEmptyCylinderSnapshot;
        DocumentSnapshot counterClientsCylinderSnapshot;
        Aggregation batchAgg;
        Aggregation emptyCylinderAgg;
        Aggregation clientCylinderAgg;

        counterBatchSnapshot=transaction.get(counterBatchRef);
        counterEmptyCylinderSnapshot=transaction.get(counterEmptyCylinderRef);
        counterClientsCylinderSnapshot=transaction.get(counterClientsCylinderRef);

        if(counterBatchSnapshot.exists()) {
            batchAgg = counterBatchSnapshot.toObject(Aggregation.class);
            batchAgg.setCount(batchAgg.getCount()+1);
        }else{
            batchAgg=new Aggregation();
            batchAgg.setType(Aggregation.TYPE_BATCH);
            batchAgg.setCount(1);
        }

        if(counterEmptyCylinderSnapshot.exists()) {
            emptyCylinderAgg = counterEmptyCylinderSnapshot.toObject(Aggregation.class);
            emptyCylinderAgg.setCount(emptyCylinderAgg.getCount()-prefetchCylinders.size());
        }else{
            emptyCylinderAgg=new Aggregation();
            emptyCylinderAgg.setType(Aggregation.TYPE_CYLINDERS);
            emptyCylinderAgg.setCount(0);
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

        Batch refill=new Batch();
        refill.setId(batchAgg.getCount());
        refill.setTimestamp(timestamp);
        refill.setNoOfCylinders(cylinderNumbers.size());
        refill.setDestinationId(client.getId());
        refill.setDestinationName(client.getName());
        refill.setType(Batch.TYPE_REFILL);
        refill.setCylinders(cylinderNumbers);
        DocumentReference refillRef=db.collection(DbPaths.COLLECTION_BATCHES).document(refill.getBatchNumber());

        //region writing all values ------------------------------------

        DocumentReference cylRef;
        for(Cylinder cyl:cylinders){
            cylRef=db.collection(DbPaths.COLLECTION_CYLINDERS).document(cyl.getStringId());
            transaction.set(cylRef,cyl);
        }

        transaction.set(clientRef,client);
        transaction.set(counterBatchRef,batchAgg);
        transaction.set(counterEmptyCylinderRef,emptyCylinderAgg);
        transaction.set(counterClientsCylinderRef,clientCylinderAgg);
        transaction.set(refillRef,refill);

        //endregion writing all values -------------------------------------

        return null;
    }
}
