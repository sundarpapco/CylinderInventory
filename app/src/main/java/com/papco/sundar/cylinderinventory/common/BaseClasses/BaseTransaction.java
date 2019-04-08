package com.papco.sundar.cylinderinventory.common.BaseClasses;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.CylinderType;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.helpers.AggregationOperation;
import com.papco.sundar.cylinderinventory.helpers.CounterAggregation;
import com.papco.sundar.cylinderinventory.helpers.CylinderUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class BaseTransaction implements Transaction.Function<Void> {

    private FirebaseFirestore db;
    private List<DocumentSnapshot> prefetchedDocuments = null;
    private long timestamp;
    private List<List<Cylinder>> masterList;
    private Destination destination;
    private List<AggregationOperation> aggregationOperations;
    private AggregationOperation globalAggregation;
    private CounterAggregation counterAggregation;
    private int batchType;

    public BaseTransaction() {
        db = FirebaseFirestore.getInstance();
        timestamp = Calendar.getInstance().getTimeInMillis();
        masterList = null;
        destination = null;
        aggregationOperations = null;
        globalAggregation = null;
        counterAggregation = null;
        batchType = -1;
    }

    // Setters *********************

    protected void setBatchType(int batchType) {
        this.batchType = batchType;
    }

    public void setPrefetchDocuments(List<DocumentSnapshot> documents) {
        this.prefetchedDocuments = documents;
    }


    // getters *******************************
    protected List<DocumentSnapshot> getPrefetchDocuments() {
        return prefetchedDocuments;
    }

    protected FirebaseFirestore getDb() {
        return db;
    }

    protected long getTimestamp() {
        return timestamp;
    }



    protected void checkPrefetch() throws FirebaseFirestoreException {
        if (getPrefetchDocuments() == null)
            throw new FirebaseFirestoreException("No prefetch found", FirebaseFirestoreException.Code.CANCELLED);

        masterList=CylinderUtils.getMasterList(getPrefetchDocuments());
    }

    // Step 1: Destination methods ******************************************

    protected void initializeDestination(Transaction transaction, int destId)
            throws FirebaseFirestoreException {

        DocumentReference destRef = db.collection(DbPaths.COLLECTION_DESTINATIONS).document(Integer.toString(destId));
        DocumentSnapshot destDocument = transaction.get(destRef);

        if (!destDocument.exists())
            throw new FirebaseFirestoreException("Invalid destination", FirebaseFirestoreException.Code.CANCELLED);

        destination = destDocument.toObject(Destination.class);

        if(destination==null)
            throw new FirebaseFirestoreException("Invalid destination", FirebaseFirestoreException.Code.CANCELLED);

        if(batchType== Batch.TYPE_INVOICE || batchType==Batch.TYPE_REFILL|| batchType==Batch.TYPE_REPAIR)
            destination.setCylinderCount(destination.getCylinderCount()+ getPrefetchDocuments().size());
        else
            destination.setCylinderCount(destination.getCylinderCount()- getPrefetchDocuments().size());

        destination.setEditable(false);
    }

    protected void writeDestination(Transaction transaction)
            throws FirebaseFirestoreException {

        if (destination == null)
            throw new FirebaseFirestoreException("Cannot write destination before initializing", FirebaseFirestoreException.Code.CANCELLED);

        DocumentReference destRef = db.collection(DbPaths.COLLECTION_DESTINATIONS).document(destination.getStringId());
        transaction.set(destRef, destination);
    }

    public Destination getDestination() {
        return destination;
    }


    // Step 2: Cylinder methods *************************************************

    protected void initializeCylinders()
            throws FirebaseFirestoreException {

        if (masterList == null || masterList.size() == 0)
            throw new FirebaseFirestoreException("Invalid cylinder list",
                    FirebaseFirestoreException.Code.CANCELLED);

        for (List<Cylinder> monoList : masterList) {
            for (Cylinder cylinder : monoList) {
                onCylinderValidation(cylinder);
            }
        }
    }

    protected List<List<Cylinder>> getMasterList(){
        return masterList;
    }

    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException{


    }

    protected void writeCylinders(Transaction transaction)
            throws FirebaseFirestoreException {

        if (masterList == null || masterList.size() == 0)
            throw new FirebaseFirestoreException("Invalid cylinder list",
                    FirebaseFirestoreException.Code.CANCELLED);


        DocumentReference cylinderRef;
        for (List<Cylinder> monoList : masterList) {
            for (Cylinder cylinder : monoList) {
                cylinderRef = db.collection(DbPaths.COLLECTION_CYLINDERS).document(cylinder.getStringId());
                transaction.set(cylinderRef, cylinder);
            }
        }
    }


    // Step 3: Aggregations *********************************************************

    protected void initializeAggregations(Transaction transaction)
            throws FirebaseFirestoreException {

        if (masterList == null)
            throw new FirebaseFirestoreException("Cannot write cylinders without initializing",
                    FirebaseFirestoreException.Code.CANCELLED);

        aggregationOperations = new ArrayList<>();
        for (List<Cylinder> monoList : masterList) {
            aggregationOperations.add(new AggregationOperation(monoList.get(0).getCylinderTypeName(), batchType, monoList.size()));
        }
        globalAggregation = new AggregationOperation(CylinderType.TYPE_GLOBAL, batchType, getPrefetchDocuments().size());


        globalAggregation.initializeWith(transaction);
        for (AggregationOperation operation : aggregationOperations)
            operation.initializeWith(transaction);
    }

    protected void writeAggregations(Transaction transaction)
            throws FirebaseFirestoreException {

        if (aggregationOperations == null)
            throw new FirebaseFirestoreException("Cannot write aggregations without initializing",
                    FirebaseFirestoreException.Code.CANCELLED);

        for (AggregationOperation operation : aggregationOperations) {
            operation.writeWith(transaction);
        }
        globalAggregation.writeWith(transaction);
    }

    //Step 4:Counter *********************************************************************

    protected void initializeCounter(Transaction transaction, String aggPath)
            throws FirebaseFirestoreException {

        counterAggregation = new CounterAggregation(aggPath);
        counterAggregation.initializeWith(transaction);
    }

    protected CounterAggregation getCounter() {
        return counterAggregation;
    }

    protected void writeCounter(Transaction transaction)
            throws FirebaseFirestoreException {

        if(counterAggregation==null)
            throw new FirebaseFirestoreException("Cannot write counter without initializing",
                    FirebaseFirestoreException.Code.CANCELLED);

        counterAggregation.writeWith(transaction);
    }



}
