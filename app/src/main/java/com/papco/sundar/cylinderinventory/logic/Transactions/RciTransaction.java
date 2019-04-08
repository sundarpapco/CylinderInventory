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

public class RciTransaction extends BaseTransaction {


    public RciTransaction() {
        setBatchType(Batch.TYPE_RCI);
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {


        checkPrefetch();

        //Step 1:Destination
        initializeDestination(transaction,getMasterList().get(0).get(0).getLocationId());
        if(getDestination().getDestType()!=Destination.TYPE_REPAIR_STATION)
            throw new FirebaseFirestoreException("Error: Cylinders not from Repair station found",
                    FirebaseFirestoreException.Code.CANCELLED);

        //Step 2:Cylinders
        initializeCylinders();

        //Step 3:Aggregations
        initializeAggregations(transaction);

        //Step 4:Counter
        initializeCounter(transaction,DbPaths.COUNT_BATCHES_RCI);


        Batch rci=new Batch();
        rci.setId(getCounter().getNextNumber());
        rci.setTimestamp(getTimestamp());
        rci.setNoOfCylinders(getPrefetchDocuments().size());
        rci.setDestinationId(getDestination().getId());
        rci.setDestinationName(getDestination().getName());
        rci.setType(Batch.TYPE_RCI);
        rci.setCylindersAndTypes(getMasterList());
        DocumentReference rciRef=getDb().collection(DbPaths.COLLECTION_BATCHES).document(rci.getBatchNumber());

        //region writing all values ------------------------------------

        writeDestination(transaction);
        writeCylinders(transaction);
        writeAggregations(transaction);
        writeCounter(transaction);
        transaction.set(rciRef,rci);

        //endregion writing all values -------------------------------------

        return null;
    }

    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {

        if(cylinder.getLocationId()!=getDestination().getId())
            throw new FirebaseFirestoreException("Cylinders from multiple locations found. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        cylinder.setLocationId(Destination.TYPE_WAREHOUSE);
        cylinder.setLocationName("WAREHOUSE");
        cylinder.setLastTransaction(getTimestamp());
        cylinder.setEmpty(true);
        cylinder.setDamaged(false);

    }
}
