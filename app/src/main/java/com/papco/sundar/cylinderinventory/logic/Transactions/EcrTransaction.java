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

public class EcrTransaction extends BaseTransaction {

    private List<Cylinder> cylinders;
    private int clientId;

    public EcrTransaction() {
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


        //region checking all the prefetch cylinders --------------------------------------


        DocumentSnapshot snapshot;
        Cylinder cylinder;

        for(int i=0;i<prefetchCylinders.size();++i){

            snapshot=prefetchCylinders.get(i);
            if(!snapshot.exists())
                throw new FirebaseFirestoreException("Invalid cylinder number found. Please check",
                        FirebaseFirestoreException.Code.CANCELLED);
            cylinder=snapshot.toObject(Cylinder.class);

            if(i==0)
                clientId=cylinder.getLocationId();
            else{

                if(cylinder.getLocationId()!=clientId)
                    throw new FirebaseFirestoreException("Cylinders from multiple locations found. Please check",
                            FirebaseFirestoreException.Code.CANCELLED);
            }

            cylinder.setLocationId(Destination.TYPE_WAREHOUSE);
            cylinder.setLocationName("WAREHOUSE");
            cylinder.setLastTransaction(timestamp);
            cylinder.setEmpty(true);
            cylinders.add(cylinder);
            cylinderNumbers.add(cylinder.getCylinderNo());
        }

        //endregion checking all prefetch cylinders --------------------------------------------

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
            if(client.getDestType()!=Destination.TYPE_CLIENT)
                throw new FirebaseFirestoreException("Cylinders not with a client. Please check",
                        FirebaseFirestoreException.Code.CANCELLED);

            client.setCylinderCount(client.getCylinderCount()- prefetchCylinders.size());
        }
        //endregion checking client and loading ----------------------------------

        //region getting, checking and initializing all the batch documents needed ------------------------------

        DocumentReference counterBatchRef=db.document(DbPaths.COUNT_BATCHES_ECR);
        DocumentReference counterEmptyCylinderRef=db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
        DocumentReference counterClientsCylinderRef=db.document(DbPaths.COUNT_CYLINDERS_CLIENT);
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
            emptyCylinderAgg.setCount(emptyCylinderAgg.getCount()+ prefetchCylinders.size());
        }else{
            emptyCylinderAgg=new Aggregation();
            emptyCylinderAgg.setType(Aggregation.TYPE_CYLINDERS);
            emptyCylinderAgg.setCount(prefetchCylinders.size());
        }

        if(counterClientsCylinderSnapshot.exists()) {
            clientCylinderAgg = counterClientsCylinderSnapshot.toObject(Aggregation.class);
            clientCylinderAgg.setCount(clientCylinderAgg.getCount() - prefetchCylinders.size());
        }else{
            clientCylinderAgg=new Aggregation();
            clientCylinderAgg.setType(Aggregation.TYPE_CYLINDERS);
            clientCylinderAgg.setCount(0);
        }

        //endregion region getting, checking and initializing all the batch documents needed ------------------------------

        Batch ecr=new Batch();
        ecr.setId(batchAgg.getCount());
        ecr.setTimestamp(timestamp);
        ecr.setNoOfCylinders(cylinderNumbers.size());
        ecr.setDestinationId(client.getId());
        ecr.setDestinationName(client.getName());
        ecr.setType(Batch.TYPE_ECR);
        ecr.setCylinders(cylinderNumbers);
        DocumentReference ecrRef=db.collection(DbPaths.COLLECTION_BATCHES).document(ecr.getBatchNumber());

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
        transaction.set(ecrRef,ecr);

        //endregion writing all values -------------------------------------

        return null;
    }
}
