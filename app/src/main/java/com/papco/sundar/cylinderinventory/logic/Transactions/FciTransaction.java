package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FciTransaction extends BaseTransaction {

    public FciTransaction() {
        setBatchType(Batch.TYPE_FCI);
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        checkPrefetch();

        //Step 1:Destination
        initializeDestination(transaction,getMasterList().get(0).get(0).getLocationId());
        if(getDestination().getDestType()!=Destination.TYPE_REFILL_STATION)
            throw new FirebaseFirestoreException("Error: All cylinders not with refill station",
                    FirebaseFirestoreException.Code.CANCELLED);

        //Step 2:Cylinders
        initializeCylinders();

        //Step 3:Aggregations
        initializeAggregations(transaction);

        //Step 4:Counter
        initializeCounter(transaction,DbPaths.COUNT_BATCHES_FCI);

        //Step 5:Batch
        Batch fci=new Batch();
        fci.setId(getCounter().getNextNumber());
        fci.setTimestamp(getTimestamp());
        fci.setNoOfCylinders(getPrefetchDocuments().size());
        fci.setDestinationId(getDestination().getId());
        fci.setDestinationName(getDestination().getName());
        fci.setType(Batch.TYPE_FCI);
        fci.setCylindersAndTypes(getMasterList());
        DocumentReference fciRef=getDb().collection(DbPaths.COLLECTION_BATCHES).document(fci.getBatchNumber());

        //region writing all values ------------------------------------

        writeDestination(transaction);
        writeCylinders(transaction);
        writeAggregations(transaction);
        writeCounter(transaction);
        transaction.set(fciRef,fci);

        //endregion writing all values -------------------------------------

        return null;
    }

    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {

        if(cylinder.getLocationId()!=getDestination().getId())
            throw new FirebaseFirestoreException("Cylinders from multiple locations found. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        cylinder.setRefillCount(cylinder.getRefillCount()+1);
        cylinder.setLocationId(Destination.TYPE_WAREHOUSE);
        cylinder.setLocationName("WAREHOUSE");
        cylinder.setLastTransaction(getTimestamp());
        cylinder.setEmpty(false);
    }
}
