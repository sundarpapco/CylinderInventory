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

public class InvoiceTransaction extends BaseTransaction {

    private int clientId;
    private int allotmentId;

    public InvoiceTransaction(int clientId, int allotmentId) {
        this.clientId = clientId;
        this.allotmentId =allotmentId;
        setBatchType(Batch.TYPE_INVOICE);
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        checkPrefetch();

        //Step 1: destination
        initializeDestination(transaction,clientId);

        //Step 2:Cylinders
        initializeCylinders();

        //Step 3:Aggregations
        initializeAggregations(transaction);

        //Step 4:Counter
        initializeCounter(transaction,DbPaths.COUNT_BATCHES_INVOICE);


        Batch invoice=new Batch();
        invoice.setId(getCounter().getNextNumber());
        invoice.setTimestamp(getTimestamp());
        invoice.setNoOfCylinders(getPrefetchDocuments().size());
        invoice.setDestinationId(getDestination().getId());
        invoice.setDestinationName(getDestination().getName());
        invoice.setType(Batch.TYPE_INVOICE);
        invoice.setCylindersAndTypes(getMasterList());
        DocumentReference invoiceRef=getDb().collection(DbPaths.COLLECTION_BATCHES).document(invoice.getBatchNumber());

        //region writing all values ------------------------------------

        writeCylinders(transaction);
        writeDestination(transaction);
        writeAggregations(transaction);
        writeCounter(transaction);
        transaction.set(invoiceRef,invoice);
        deleteAllotment(transaction);

        return null;
    }

    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {

        String cylinderDetail="Cylinder number "+cylinder.getStringId()+" ";

        if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE)
            throw new FirebaseFirestoreException(cylinderDetail+" not in warehouse. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(cylinder.isEmpty())
            throw new FirebaseFirestoreException(cylinderDetail+"is empty. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(!cylinder.isAlloted())
            throw new FirebaseFirestoreException(cylinderDetail+"is not allotted to this client. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);

        cylinder.takeSnapShot();
        cylinder.setAlloted(false);
        cylinder.setLocationId(getDestination().getId());
        cylinder.setLocationName(getDestination().getName());
        cylinder.setLastTransaction(getTimestamp());
    }


    private void deleteAllotment(Transaction transaction){

        DocumentReference allotmentRef=getDb().collection(DbPaths.COLLECTION_ALLOTMENT).document(Integer.toString(allotmentId));
        transaction.delete(allotmentRef);

    }

}
