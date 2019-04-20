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

public class RefillTransaction extends BaseTransaction {

    private int clientId;

    public RefillTransaction(int clientId) {
        this.clientId = clientId;
        setBatchType(Batch.TYPE_REFILL);
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {


        checkPrefetch();

        // Step 1: Destination
        initializeDestination(transaction,clientId);


        // Step 2: Cylinders
        initializeCylinders();

        // Step 3: Aggregation
        initializeAggregations(transaction);

        // Step 4: Counter
        initializeCounter(transaction,DbPaths.COUNT_BATCHES_REFILL);

        // Step 5: Batch
        Batch refill=new Batch();
        refill.setId(getCounter().getNextNumber());
        refill.setTimestamp(getTimestamp());
        refill.setNoOfCylinders(getPrefetchDocuments().size());
        refill.setDestinationId(getDestination().getId());
        refill.setDestinationName(getDestination().getName());
        refill.setType(Batch.TYPE_REFILL);
        refill.setCylindersAndTypes(getMasterList());
        DocumentReference refillRef=getDb().collection(DbPaths.COLLECTION_BATCHES).document(refill.getBatchNumber());


        //region writing all values ------------------------------------

        writeCylinders(transaction);
        writeDestination(transaction);
        writeAggregations(transaction);
        writeCounter(transaction);
        transaction.set(refillRef,refill);

        //endregion writing all values -------------------------------------

        return null;
    }


    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {

        String cylinderDetail="Cylinder number "+cylinder.getStringId()+" ";

        if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE)
            throw new FirebaseFirestoreException(cylinderDetail+"not in warehouse. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(!cylinder.isEmpty())
            throw new FirebaseFirestoreException(cylinderDetail+"is not empty. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(cylinder.isDamaged())
            throw new FirebaseFirestoreException(cylinderDetail+"is damaged. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        if (cylinder.isAlloted())
            throw new FirebaseFirestoreException(
                    cylinderDetail+" has been allotted to a client. Please delete the allotment first",
                    FirebaseFirestoreException.Code.CANCELLED);

        cylinder.takeSnapShot();
        cylinder.setLocationId(getDestination().getId());
        cylinder.setLocationName(getDestination().getName());
        cylinder.setLastTransaction(getTimestamp());

    }
}
