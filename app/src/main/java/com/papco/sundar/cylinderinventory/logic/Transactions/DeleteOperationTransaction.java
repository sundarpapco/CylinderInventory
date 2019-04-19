package com.papco.sundar.cylinderinventory.logic.Transactions;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.Cylinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DeleteOperationTransaction extends BaseTransaction {

    private final String TAG="SUNDAR";
    private Batch batchToDelete;

    public DeleteOperationTransaction(Batch batchToDelete){
        this.batchToDelete=batchToDelete;
        setBatchType(batchToDelete.getType());
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        checkPrefetch();

        //prerequistic is, the batch should be present and if so, we should lock it
        DocumentReference batchReference=getDb().collection(DbPaths.COLLECTION_BATCHES).document(batchToDelete.getBatchNumber());
        DocumentSnapshot batchDocument=transaction.get(batchReference);
        if(!batchDocument.exists())
            throw new FirebaseFirestoreException("This batch already deleted",
                    FirebaseFirestoreException.Code.CANCELLED);

        //Step 1: Destination
        initializeReverseDestination(transaction,batchToDelete.getDestinationId());

        //Step 2: Cylinders
        initializeCylinders();

        //Step 3: Aggregations
        initializeReverseAggregations(transaction);


        // ************* Write all values
        writeDestination(transaction);
        writeCylinders(transaction);
        writeAggregations(transaction);
        transaction.delete(batchReference);

        return null;
    }

    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {

        if(cylinder.getLastTransaction()!=batchToDelete.getTimestamp())
            throw new FirebaseFirestoreException("Cylinder "+cylinder.getStringId()+". This transaction cannot be deleted",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(!cylinder.hasValidSnapshot())
            throw new FirebaseFirestoreException("Cylinder "+cylinder.getStringId()+". This transaction cannot be deleted",
                    FirebaseFirestoreException.Code.CANCELLED);

        cylinder.restoreSnapshot();

    }
}
