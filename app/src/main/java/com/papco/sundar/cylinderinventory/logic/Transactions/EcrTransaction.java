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

public class EcrTransaction extends BaseTransaction {

    public EcrTransaction(){
        setBatchType(Batch.TYPE_ECR);
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        checkPrefetch();

        // Step 1: destination

        //initializing and checking if the location is a Client
        initializeDestination(transaction, getMasterList().get(0).get(0).getLocationId());
        if (getDestination().getDestType() != Destination.TYPE_CLIENT)
            throw new FirebaseFirestoreException("Error:All cylinders are not with Client",
                    FirebaseFirestoreException.Code.CANCELLED);


        // Step 2: cylinders
        initializeCylinders();


        //Step 3: Aggregations (Global and type)
        initializeAggregations(transaction);

        //Step 4: Counter
        initializeCounter(transaction,DbPaths.COUNT_BATCHES_ECR);

        //Step 5: Batch

        Batch ecr = new Batch();
        ecr.setId(getCounter().getNextNumber());
        ecr.setTimestamp(getTimestamp());
        ecr.setNoOfCylinders(getPrefetchDocuments().size());
        ecr.setDestinationId(getDestination().getId());
        ecr.setDestinationName(getDestination().getName());
        ecr.setType(Batch.TYPE_ECR);
        ecr.setCylindersAndTypes(getMasterList());
        DocumentReference ecrRef = getDb().collection(DbPaths.COLLECTION_BATCHES).document(ecr.getBatchNumber());

        //region writing all values ------------------------------------

        writeCylinders(transaction);
        writeDestination(transaction);
        writeAggregations(transaction);
        writeCounter(transaction);
        transaction.set(ecrRef,ecr);

        //endregion writing all values -------------------------------------

        return null;
    }

    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {

        if (cylinder.getLocationId() != getDestination().getId())
            throw new FirebaseFirestoreException("Error: Cylinders from multiple locations found",
                    FirebaseFirestoreException.Code.CANCELLED);

        cylinder.takeSnapShot();
        cylinder.setLocationId(Destination.TYPE_WAREHOUSE);
        cylinder.setLocationName("WAREHOUSE");
        cylinder.setLastTransaction(getTimestamp());
        cylinder.setEmpty(true);

    }
}
