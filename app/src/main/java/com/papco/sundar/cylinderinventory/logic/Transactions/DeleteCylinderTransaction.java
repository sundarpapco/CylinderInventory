package com.papco.sundar.cylinderinventory.logic.Transactions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

import java.util.Date;

public class DeleteCylinderTransaction extends BaseTransaction {

    private int cylinderNumber;

    public DeleteCylinderTransaction(int cylinderNumber) {
        this.cylinderNumber = cylinderNumber;
    }


    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        DocumentReference cylinderRef=db.collection(DbPaths.COLLECTION_CYLINDERS).document(Integer.toString(cylinderNumber));
        DocumentReference graveYardRef=db.document(DbPaths.COUNT_CYLINDERS_GRAVEYARD);
        DocumentReference graveYardEntryRef=db.collection(DbPaths.COLLECTION_GRAVEYARD).document(Integer.toString(cylinderNumber));
        DocumentReference aggRef;

        Cylinder cylinder;
        Aggregation respectiveAgg;
        Aggregation graveYardAgg;


        //read the cylinder document first;
        DocumentSnapshot cylinderDoc=transaction.get(cylinderRef);

        //if the cylinder document was not found, throw exception
        if(!cylinderDoc.exists())
            throw new FirebaseFirestoreException("Cylinder document not found",FirebaseFirestoreException.Code.CANCELLED);
        else
            cylinder=cylinderDoc.toObject(Cylinder.class);

        if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE)
            throw new FirebaseFirestoreException("Cylinder not in warehouse. Try after getting it to warehouse",FirebaseFirestoreException.Code.CANCELLED);

        if(cylinder.isDamaged()) {
            aggRef = db.document(DbPaths.COUNT_CYLINDERS_DAMAGED);
        }else {
            if (cylinder.isEmpty())
                aggRef = db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
            else
                aggRef=db.document(DbPaths.COUNT_CYLINDERS_FULL);
        }


        DocumentSnapshot respectiveAggDoc=transaction.get(aggRef);
        DocumentSnapshot graveYardCounterDoc=transaction.get(graveYardRef);

        if(!respectiveAggDoc.exists()) {
            respectiveAgg = new Aggregation();
            respectiveAgg.setType(Aggregation.TYPE_CYLINDERS);
            respectiveAgg.setCount(0);
        }else {
            respectiveAgg = respectiveAggDoc.toObject(Aggregation.class);
            respectiveAgg.setCount(respectiveAgg.getCount()-1);
        }

        if(!graveYardCounterDoc.exists()){
            graveYardAgg=new Aggregation();
            graveYardAgg.setType(Aggregation.TYPE_CYLINDERS);
            graveYardAgg.setCount(1);
        }else{
            graveYardAgg=graveYardCounterDoc.toObject(Aggregation.class);
            graveYardAgg.setCount(graveYardAgg.getCount()+1);
        }

        cylinder.setLocationId(Destination.TYPE_GRAVEYARD);
        cylinder.setLocationName("GRAVEYARD");
        cylinder.setLastTransaction(new Date().getTime());

        transaction.delete(cylinderRef);
        transaction.set(graveYardEntryRef,cylinder);
        transaction.set(graveYardRef,graveYardAgg);
        transaction.set(aggRef,respectiveAgg);

        return null;
    }
}
