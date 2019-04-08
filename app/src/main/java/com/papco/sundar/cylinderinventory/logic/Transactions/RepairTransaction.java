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

public class RepairTransaction extends BaseTransaction {

    private int clientId;

    public RepairTransaction(int clientId) {
        this.clientId = clientId;
        setBatchType(Batch.TYPE_REPAIR);
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {


        checkPrefetch();

        //Step 1:Destination
        initializeDestination(transaction,clientId);

        //Step 2:Cylinders
        initializeCylinders();

        //Step 3: Aggregations
        initializeAggregations(transaction);

        //Step 4:Counter
        initializeCounter(transaction,DbPaths.COUNT_BATCHES_REPAIR);


        Batch repair=new Batch();
        repair.setId(getCounter().getNextNumber());
        repair.setTimestamp(getTimestamp());
        repair.setNoOfCylinders(getPrefetchDocuments().size());
        repair.setDestinationId(getDestination().getId());
        repair.setDestinationName(getDestination().getName());
        repair.setType(Batch.TYPE_REPAIR);
        repair.setCylindersAndTypes(getMasterList());
        DocumentReference repairRef=getDb().collection(DbPaths.COLLECTION_BATCHES).document(repair.getBatchNumber());

        //region writing all values ------------------------------------

        writeDestination(transaction);
        writeCylinders(transaction);
        writeAggregations(transaction);
        writeCounter(transaction);
        transaction.set(repairRef,repair);

        //endregion writing all values -------------------------------------

        return null;
    }

    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {

        if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE)
            throw new FirebaseFirestoreException("Some cylinders not in warehouse. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(!cylinder.isDamaged())
            throw new FirebaseFirestoreException("Non damaged cylinders found. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        cylinder.setLocationId(getDestination().getId());
        cylinder.setLocationName(getDestination().getName());
        cylinder.setLastTransaction(getTimestamp());
        cylinder.setEmpty(true);

    }
}
