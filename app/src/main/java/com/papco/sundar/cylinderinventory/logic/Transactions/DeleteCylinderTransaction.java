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
import com.papco.sundar.cylinderinventory.data.CylinderType;
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
        DocumentReference graveYardCounterRef=db.document(DbPaths.COUNT_CYLINDERS_GRAVEYARD);
        DocumentReference graveYardEntryRef=db.collection(DbPaths.COLLECTION_GRAVEYARD).document(Integer.toString(cylinderNumber));
        DocumentReference cylinderTypeRef;
        DocumentReference aggRef;
        DocumentReference typeCounterToReduceRef;

        Cylinder cylinder;
        CylinderType cylinderType;
        Aggregation respectiveAgg;
        Aggregation graveYardAgg;
        Aggregation typeCounterToReduce;


        //------------- read and validate the cylinder document

        //read the cylinder document first;
        DocumentSnapshot cylinderDoc=transaction.get(cylinderRef);

        //if the cylinder document was not found, throw exception
        if(!cylinderDoc.exists())
            throw new FirebaseFirestoreException("Cylinder not found",FirebaseFirestoreException.Code.CANCELLED);
        else
            cylinder=cylinderDoc.toObject(Cylinder.class);

        if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE)
            throw new FirebaseFirestoreException("Cylinder not in warehouse. Try after getting it to warehouse",FirebaseFirestoreException.Code.CANCELLED);

        if(cylinder.isDamaged()) {
            aggRef = db.document(DbPaths.COUNT_CYLINDERS_DAMAGED);
            typeCounterToReduceRef=db.document(DbPaths.getAggregationForType(cylinder.getCylinderTypeName(), DbPaths.AggregationType.DAMAGED));
        }else {
            if (cylinder.isEmpty()) {
                aggRef = db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
                typeCounterToReduceRef = db.document(DbPaths.getAggregationForType(cylinder.getCylinderTypeName(), DbPaths.AggregationType.EMPTY));
            } else {
                aggRef = db.document(DbPaths.COUNT_CYLINDERS_FULL);
                typeCounterToReduceRef=db.document(DbPaths.getAggregationForType(cylinder.getCylinderTypeName(), DbPaths.AggregationType.FULL));
            }
        }


        // ------------------- read and validate the cylinder type document

        cylinderTypeRef=db.collection(DbPaths.COLLECTION_CYLINDER_TYPES).document(cylinder.getCylinderTypeName());
        DocumentSnapshot cylinderTypeDocument=transaction.get(cylinderTypeRef);

        if(cylinderTypeDocument.exists()){
            cylinderType=cylinderTypeDocument.toObject(CylinderType.class);
            cylinderType.setNoOfCylinders(cylinderType.getNoOfCylinders()-1);
        }else
            throw new FirebaseFirestoreException("Invalid cylinder type",FirebaseFirestoreException.Code.CANCELLED);


        // ------------------ read the aggregations to modify

        DocumentSnapshot respectiveAggDoc=transaction.get(aggRef);
        DocumentSnapshot graveYardCounterDoc=transaction.get(graveYardCounterRef);
        DocumentSnapshot typeCounterToReduceDocument=transaction.get(typeCounterToReduceRef);

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

        if(typeCounterToReduceDocument.exists()){
            typeCounterToReduce=typeCounterToReduceDocument.toObject(Aggregation.class);
            typeCounterToReduce.setCount(typeCounterToReduce.getCount()-1);
        }else{
            typeCounterToReduce=new Aggregation();
            typeCounterToReduce.setCount(0);
            typeCounterToReduce.setType(Aggregation.TYPE_CYLINDERS);
        }

        // ----------------------- write all the values

        cylinder.setLocationId(Destination.TYPE_GRAVEYARD);
        cylinder.setLocationName("GRAVEYARD");
        cylinder.setLastTransaction(new Date().getTime());

        transaction.delete(cylinderRef);
        transaction.set(graveYardEntryRef,cylinder);
        transaction.set(graveYardCounterRef,graveYardAgg);
        transaction.set(aggRef,respectiveAgg);
        transaction.set(typeCounterToReduceRef,typeCounterToReduce);
        transaction.set(cylinderTypeRef,cylinderType);

        return null;
    }
}
