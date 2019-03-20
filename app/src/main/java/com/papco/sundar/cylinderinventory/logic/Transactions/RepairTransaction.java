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

public class RepairTransaction extends BaseTransaction {

    private List<Cylinder> cylinders;
    private int clientId;

    public RepairTransaction(int clientId) {
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
            throw new FirebaseFirestoreException("Invalid repair station. Please check",
                    FirebaseFirestoreException.Code.CANCELLED);
        else {
            client = clientSnapshot.toObject(Destination.class);
            client.setCylinderCount(client.getCylinderCount()+prefetchCylinders.size());
            client.setEditable(false);
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

            if(!cylinder.isDamaged())
                throw new FirebaseFirestoreException("Non damaged cylinders found. Please check",
                        FirebaseFirestoreException.Code.CANCELLED);

            cylinder.setLocationId(client.getId());
            cylinder.setLocationName(client.getName());
            cylinder.setLastTransaction(timestamp);
            cylinder.setEmpty(true);
            cylinders.add(cylinder);
            cylinderNumbers.add(cylinder.getCylinderNo());
        }

        //endregion checking all prefetch cylinders --------------------------------------------

        //region getting, checking and initializing all the batch documents needed ------------------------------

        DocumentReference counterBatchRef=db.document(DbPaths.COUNT_BATCHES_REPAIR);
        DocumentReference counterDamagedCylinderRef=db.document(DbPaths.COUNT_CYLINDERS_DAMAGED);
        DocumentReference counterRepairStationCylinderRef=db.document(DbPaths.COUNT_CYLINDERS_REPAIR_STATION);
        DocumentSnapshot counterBatchSnapshot;
        DocumentSnapshot counterDamagedCylinderSnapshot;
        DocumentSnapshot counterRepairStationCylinderSnapshot;
        Aggregation batchAgg;
        Aggregation damagedCylinderAgg;
        Aggregation repairStationCylinderAgg;

        counterBatchSnapshot=transaction.get(counterBatchRef);
        counterDamagedCylinderSnapshot=transaction.get(counterDamagedCylinderRef);
        counterRepairStationCylinderSnapshot=transaction.get(counterRepairStationCylinderRef);

        if(counterBatchSnapshot.exists()) {
            batchAgg = counterBatchSnapshot.toObject(Aggregation.class);
            batchAgg.setCount(batchAgg.getCount()+1);
        }else{
            batchAgg=new Aggregation();
            batchAgg.setType(Aggregation.TYPE_BATCH);
            batchAgg.setCount(1);
        }

        if(counterDamagedCylinderSnapshot.exists()) {
            damagedCylinderAgg = counterDamagedCylinderSnapshot.toObject(Aggregation.class);
            damagedCylinderAgg.setCount(damagedCylinderAgg.getCount()-prefetchCylinders.size());
        }else{
            damagedCylinderAgg=new Aggregation();
            damagedCylinderAgg.setType(Aggregation.TYPE_CYLINDERS);
            damagedCylinderAgg.setCount(0);
        }

        if(counterRepairStationCylinderSnapshot.exists()) {
            repairStationCylinderAgg = counterRepairStationCylinderSnapshot.toObject(Aggregation.class);
            repairStationCylinderAgg.setCount(repairStationCylinderAgg.getCount()+prefetchCylinders.size());
        }else{
            repairStationCylinderAgg=new Aggregation();
            repairStationCylinderAgg.setType(Aggregation.TYPE_CYLINDERS);
            repairStationCylinderAgg.setCount(prefetchCylinders.size());
        }

        //endregion region getting, checking and initializing all the batch documents needed ------------------------------

        Batch repair=new Batch();
        repair.setId(batchAgg.getCount());
        repair.setTimestamp(timestamp);
        repair.setNoOfCylinders(cylinderNumbers.size());
        repair.setDestinationId(client.getId());
        repair.setDestinationName(client.getName());
        repair.setType(Batch.TYPE_REPAIR);
        repair.setCylinders(cylinderNumbers);
        DocumentReference repairRef=db.collection(DbPaths.COLLECTION_BATCHES).document(repair.getBatchNumber());

        //region writing all values ------------------------------------

        DocumentReference cylRef;
        for(Cylinder cyl:cylinders){
            cylRef=db.collection(DbPaths.COLLECTION_CYLINDERS).document(cyl.getStringId());
            transaction.set(cylRef,cyl);
        }

        transaction.set(clientRef,client);
        transaction.set(counterBatchRef,batchAgg);
        transaction.set(counterDamagedCylinderRef,damagedCylinderAgg);
        transaction.set(counterRepairStationCylinderRef,repairStationCylinderAgg);
        transaction.set(repairRef,repair);

        //endregion writing all values -------------------------------------

        return null;
    }


}
