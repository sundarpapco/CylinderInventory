package com.papco.sundar.cylinderinventory.logic.Transactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;
import com.papco.sundar.cylinderinventory.data.Cylinder;

import java.util.Date;

public class AddCylindersTransaction extends BaseTransaction {

    private int numberOfCylinders;
    private DocumentReference totalCountRef;
    private DocumentReference filledCountRef;
    private String remarks;
    private FirebaseFirestore db;
    private String supplier;

    public AddCylindersTransaction(int numberOfCylinders,String supplier,String remarks){

        db=FirebaseFirestore.getInstance();
        this.numberOfCylinders=numberOfCylinders;
        totalCountRef=db.document(DbPaths.COUNT_CYLINDERS_TOTAL);
        filledCountRef=db.document(DbPaths.COUNT_CYLINDERS_FULL);
        this.remarks=remarks;
        this.supplier=supplier;
    }


    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        int lastNumber;
        int filledCount;

        DocumentSnapshot totalCountDocument=transaction.get(totalCountRef);
        DocumentSnapshot filledCountDocument=transaction.get(filledCountRef);

        lastNumber=totalCountDocument.getLong("count").intValue();
        filledCount=filledCountDocument.getLong("count").intValue();

        Cylinder cylinder=new Cylinder();
        cylinder.setPurchaseDate(new Date().getTime());
        cylinder.setSupplier(supplier);

        for(int i=1;i<=numberOfCylinders;++i){

            cylinder.setCylinderNo(lastNumber+i);
            cylinder.setLastTransaction(cylinder.getPurchaseDate());
            cylinder.setRemarks(remarks);


            DocumentReference newCylRef=db.collection("cylinders").document(cylinder.getStringId());
            transaction.set(newCylRef,cylinder);

        }

        Aggregation newValue=new Aggregation();
        newValue.setType(Aggregation.TYPE_CYLINDERS);
        newValue.setCount(lastNumber+numberOfCylinders);

        //updating the total cylinder counter
        transaction.set(totalCountRef,newValue);

        //updating the filled cylinders count
        newValue.setCount(filledCount+numberOfCylinders);
        transaction.set(filledCountRef,newValue);

        return null;
    }
}
