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
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

public class MarkDamageTransaction extends BaseTransaction {

    private int cylinderNumber;

    public MarkDamageTransaction(int cylinderNumber) {
        this.cylinderNumber = cylinderNumber;
    }


    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference cylinderRef = db.collection(DbPaths.COLLECTION_CYLINDERS).document(Integer.toString(cylinderNumber));
        DocumentReference globalDamageCounterRef = db.document(DbPaths.COUNT_CYLINDERS_DAMAGED);
        DocumentReference globalCounterToReduce;
        DocumentReference typeDamageCounterRef;
        DocumentReference typeCounterToReduceRef;

        Cylinder cylinder;
        Aggregation warehouseAgg;
        Aggregation damageAgg;
        Aggregation typeCounterToReduce;
        Aggregation typeDamagedCounter;


        //read the cylinder document first;
        DocumentSnapshot cylinderDoc = transaction.get(cylinderRef);

        //if the cylinder document was not found, throw exception
        if (!cylinderDoc.exists())
            throw new FirebaseFirestoreException("Cylinder not found", FirebaseFirestoreException.Code.CANCELLED);
        else
            cylinder = cylinderDoc.toObject(Cylinder.class);

        if (cylinder.getLocationId() != Destination.TYPE_WAREHOUSE)
            throw new FirebaseFirestoreException("Cylinder not in warehouse. Try after getting it to warehouse", FirebaseFirestoreException.Code.CANCELLED);


        typeDamageCounterRef = db.document(DbPaths.getAggregationForType(cylinder.getCylinderTypeName(), DbPaths.AggregationType.DAMAGED));
        if (cylinder.isEmpty()) {
            globalCounterToReduce = db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
            typeCounterToReduceRef = db.document(DbPaths.getAggregationForType(cylinder.getCylinderTypeName(), DbPaths.AggregationType.EMPTY));
        } else {
            globalCounterToReduce = db.document(DbPaths.COUNT_CYLINDERS_FULL);
            typeCounterToReduceRef = db.document(DbPaths.getAggregationForType(cylinder.getCylinderTypeName(), DbPaths.AggregationType.FULL));
        }

        DocumentSnapshot aggDoc = transaction.get(globalCounterToReduce);
        DocumentSnapshot damageCounterDoc = transaction.get(globalDamageCounterRef);
        DocumentSnapshot typeDamageCounterDocument = transaction.get(typeDamageCounterRef);
        DocumentSnapshot typeCounterToReduceDocument = transaction.get(typeCounterToReduceRef);

        if (!aggDoc.exists()) {
            warehouseAgg = new Aggregation();
            warehouseAgg.setType(Aggregation.TYPE_CYLINDERS);
            warehouseAgg.setCount(0);
        } else {
            warehouseAgg = aggDoc.toObject(Aggregation.class);
            warehouseAgg.setCount(warehouseAgg.getCount() - 1);
        }

        if (!damageCounterDoc.exists()) {
            damageAgg = new Aggregation();
            damageAgg.setType(Aggregation.TYPE_CYLINDERS);
            damageAgg.setCount(1);
        } else {
            damageAgg = damageCounterDoc.toObject(Aggregation.class);
            damageAgg.setCount(damageAgg.getCount() + 1);
        }

        if (typeCounterToReduceDocument.exists()) {
            typeCounterToReduce = typeCounterToReduceDocument.toObject(Aggregation.class);
            typeCounterToReduce.setCount(typeCounterToReduce.getCount() - 1);
        } else {
            typeCounterToReduce = new Aggregation();
            typeCounterToReduce.setCount(0);
            typeCounterToReduce.setType(Aggregation.TYPE_CYLINDERS);
        }

        if (typeDamageCounterDocument.exists()) {
            typeDamagedCounter = typeDamageCounterDocument.toObject(Aggregation.class);
            typeDamagedCounter.setCount(typeDamagedCounter.getCount() + 1);
        } else {
            typeDamagedCounter = new Aggregation();
            typeDamagedCounter.setCount(1);
            typeDamagedCounter.setType(Aggregation.TYPE_CYLINDERS);
        }

        cylinder.setDamaged(true);
        cylinder.setEmpty(true);
        cylinder.setDamageCount(cylinder.getDamageCount() + 1);

        transaction.set(cylinderRef, cylinder);
        transaction.set(globalCounterToReduce, warehouseAgg);
        transaction.set(globalDamageCounterRef, damageAgg);
        transaction.set(typeDamageCounterRef,typeDamagedCounter);
        transaction.set(typeCounterToReduceRef,typeCounterToReduce);

        return null;
    }
}
