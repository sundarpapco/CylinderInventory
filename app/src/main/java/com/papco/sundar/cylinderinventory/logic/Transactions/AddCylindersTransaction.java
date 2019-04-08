package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths.AggregationType;
import com.papco.sundar.cylinderinventory.data.Aggregation;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.CylinderType;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddCylindersTransaction extends BaseTransaction {

    private int numberOfCylinders;
    private String remarks;
    private FirebaseFirestore db;
    private String supplier;
    private String cylinderType;

    public AddCylindersTransaction(int numberOfCylinders,String supplier,String remarks,String cylinderType){

        db=FirebaseFirestore.getInstance();
        this.numberOfCylinders=numberOfCylinders;
        this.remarks=remarks;
        this.supplier=supplier;
        this.cylinderType=cylinderType;
    }


    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        int previousLastCylinderNumber;
        long purchaseTime= Calendar.getInstance().getTimeInMillis();

        DocumentReference cylinderTypeRef=db.collection(DbPaths.COLLECTION_CYLINDER_TYPES)
                .document(cylinderType);
        DocumentReference typeAggregationFullRef=db.document(DbPaths.getAggregationForType(cylinderType, AggregationType.FULL));
        DocumentReference totalCountRef=db.document(DbPaths.COUNT_CYLINDERS_TOTAL);
        DocumentReference filledCountRef=db.document(DbPaths.COUNT_CYLINDERS_FULL);

        DocumentSnapshot totalCountDocument=transaction.get(totalCountRef);
        DocumentSnapshot filledCountDocument=transaction.get(filledCountRef);
        DocumentSnapshot cylinderTypeDocument=transaction.get(cylinderTypeRef);
        DocumentSnapshot typeAggregationFullDocument=transaction.get(typeAggregationFullRef);

        Aggregation totalCylinderCount;
        Aggregation totalFilledCylinderCount;
        CylinderType cylinderType;
        Aggregation typeAggregationFull;

        // ---------- Checking and initializing the global total cylinder count

        if(totalCountDocument.exists()) {
            totalCylinderCount=totalCountDocument.toObject(Aggregation.class);
            previousLastCylinderNumber=totalCylinderCount.getCount();
            totalCylinderCount.setCount(totalCylinderCount.getCount()+numberOfCylinders);
        }else{
            previousLastCylinderNumber=0;
            totalCylinderCount=new Aggregation();
            totalCylinderCount.setCount(numberOfCylinders);
            totalCylinderCount.setType(Aggregation.TYPE_CYLINDERS);
        }

        // ---------- Checking and initializing the global full cylinder count

        if(filledCountDocument.exists()){
            totalFilledCylinderCount=filledCountDocument.toObject(Aggregation.class);
            totalFilledCylinderCount.setCount(totalFilledCylinderCount.getCount()+numberOfCylinders);
        }else{
            totalFilledCylinderCount=new Aggregation();
            totalFilledCylinderCount.setCount(numberOfCylinders);
        }

        // ---------- Checking whether the cylinderTypeDocument is still valid

        if(cylinderTypeDocument.exists()){
            cylinderType=cylinderTypeDocument.toObject(CylinderType.class);
            cylinderType.setNoOfCylinders(cylinderType.getNoOfCylinders()+numberOfCylinders);
            cylinderType.setEditable(false);
        }else{
            throw new FirebaseFirestoreException("Cylinder type not found! Cannot add cylinders",FirebaseFirestoreException.Code.CANCELLED);
        }

        // ---------- Checking and initializing the sub collection full cylinder aggregation of cylinder type

        if(typeAggregationFullDocument.exists()){
            typeAggregationFull=typeAggregationFullDocument.toObject(Aggregation.class);
            typeAggregationFull.setCount(typeAggregationFull.getCount()+numberOfCylinders);
        }else{
            typeAggregationFull=new Aggregation();
            typeAggregationFull.setCount(numberOfCylinders);
            typeAggregationFull.setType(Aggregation.TYPE_CYLINDERS);
        }

        //------------ initializing and writing cylinders document

        Cylinder cylinder=new Cylinder();
        cylinder.setPurchaseDate(purchaseTime);
        cylinder.setSupplier(supplier);
        cylinder.setLastTransaction(purchaseTime);
        cylinder.setCylinderTypeName(cylinderType.getName());
        cylinder.setRemarks(remarks);

        for(int i=1;i<=numberOfCylinders;++i){

            cylinder.setCylinderNo(previousLastCylinderNumber+i);
            DocumentReference newCylRef=db.collection(DbPaths.COLLECTION_CYLINDERS).document(cylinder.getStringId());
            transaction.set(newCylRef,cylinder);

        }

        // ------------- updating the global cylinder aggregation counts
        transaction.set(totalCountRef,totalCylinderCount);
        transaction.set(filledCountRef,totalFilledCylinderCount);

        // ------------- updating the cylinderTypeDocument and its aggregation full cylinder
        transaction.set(cylinderTypeRef,cylinderType);
        transaction.set(typeAggregationFullRef,typeAggregationFull);

        return null;
    }
}
