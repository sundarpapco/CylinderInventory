package com.papco.sundar.cylinderinventory.logic.Transactions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

public class MarkDamageTransaction implements Transaction.Function<Void> {

    private int cylinderNumber;

    public MarkDamageTransaction(int cylinderNumber) {
        this.cylinderNumber = cylinderNumber;
    }


    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        DocumentReference cylinderRef=db.collection(DbPaths.COLLECTION_CYLINDERS).document(Integer.toString(cylinderNumber));
        DocumentReference damageCounterRef=db.document(DbPaths.COUNT_CYLINDERS_DAMAGED);
        DocumentReference aggRef;

        Cylinder cylinder;
        Aggregation warehouseAgg;
        Aggregation damageAgg;


        //read the cylinder document first;
        DocumentSnapshot cylinderDoc=transaction.get(cylinderRef);

        //if the cylinder document was not found, throw exception
        if(!cylinderDoc.exists())
            throw new FirebaseFirestoreException("Cylinder document not found",FirebaseFirestoreException.Code.CANCELLED);
        else
            cylinder=cylinderDoc.toObject(Cylinder.class);

        if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE)
            throw new FirebaseFirestoreException("Cylinder not in warehouse. Try after getting it to warehouse",FirebaseFirestoreException.Code.CANCELLED);

        if(cylinder.isEmpty())
            aggRef=db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
        else
            aggRef=db.document(DbPaths.COUNT_CYLINDERS_FULL);


        DocumentSnapshot aggDoc=transaction.get(aggRef);
        DocumentSnapshot damageCounterDoc=transaction.get(damageCounterRef);

        if(!aggDoc.exists()) {
            warehouseAgg = new Aggregation();
            warehouseAgg.setType(Aggregation.TYPE_CYLINDERS);
            warehouseAgg.setCount(0);
        }else {
            warehouseAgg = aggDoc.toObject(Aggregation.class);
            warehouseAgg.setCount(warehouseAgg.getCount()-1);
        }

        if(!damageCounterDoc.exists()){
            damageAgg=new Aggregation();
            damageAgg.setType(Aggregation.TYPE_CYLINDERS);
            damageAgg.setCount(1);
        }else{
            damageAgg=damageCounterDoc.toObject(Aggregation.class);
            damageAgg.setCount(damageAgg.getCount()+1);
        }
        
        cylinder.setDamaged(true);
        cylinder.setEmpty(true);
        cylinder.setDamageCount(cylinder.getDamageCount()+1);

        transaction.set(cylinderRef,cylinder);
        transaction.set(aggRef,warehouseAgg);
        transaction.set(damageCounterRef,damageAgg);

        return null;
    }
}
