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
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.Destination;

import java.util.Calendar;

public class CreateAllotmentTransaction extends BaseTransaction {

    private int noOfCylinders;
    private Destination destination;

    public CreateAllotmentTransaction(int noOfCylinders, Destination destination) {
        this.noOfCylinders = noOfCylinders;
        this.destination = destination;
    }


    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        int lastAllotmentId;
        Aggregation allotmentAgg;
        Allotment allotment;

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference counterRef=db.document(DbPaths.COUNT_ALLOTMENT);

        DocumentSnapshot counterDoc=transaction.get(counterRef);

        if(!counterDoc.exists()){
            allotmentAgg=new Aggregation();
            allotmentAgg.setType(Aggregation.TYPE_ALLOTMENT);
            allotmentAgg.setCount(1);
        }else{
            allotmentAgg=counterDoc.toObject(Aggregation.class);
            allotmentAgg.setCount(allotmentAgg.getCount()+1);
        }

        allotment=new Allotment();
        allotment.setId(allotmentAgg.getCount());
        allotment.setState(Allotment.STATE_ALLOTTED);
        allotment.setNumberOfCylinders(noOfCylinders);
        allotment.setClientName(destination.getName());
        allotment.setClientId(destination.getId());
        allotment.setTimeStamp(Calendar.getInstance().getTimeInMillis());

        DocumentReference newAllotmentRef=db.collection(DbPaths.COLLECTION_ALLOTMENT)
                .document(allotment.getStringId());

        transaction.set(newAllotmentRef,allotment);
        transaction.set(counterRef,allotmentAgg);

        return null;
    }
}
