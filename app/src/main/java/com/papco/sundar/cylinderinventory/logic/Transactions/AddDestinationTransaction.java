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
import com.papco.sundar.cylinderinventory.data.Destination;

public class AddDestinationTransaction extends BaseTransaction {

    private Destination destination;
    private int lastId;
    private FirebaseFirestore db;

    public AddDestinationTransaction(@NonNull Destination destination){
        db=FirebaseFirestore.getInstance();
        this.destination=destination;
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        DocumentReference counterRef=db.document(DbPaths.COUNT_DESTINATION);
        DocumentSnapshot counterDoc=transaction.get(counterRef);
        if(counterDoc.exists()){
            lastId=counterDoc.getLong("count").intValue();
        }else{
            // we are starting from number 3, since warehouse and graveyard are assumed to be
            // id 1 and 2
            lastId=2;
        }

        destination.setId(lastId+1);
        DocumentReference newDestinationRef=db.collection(DbPaths.COLLECTION_DESTINATIONS).document(destination.getStringId());
        transaction.set(newDestinationRef,destination);

        Aggregation agg=new Aggregation();
        agg.setType(Aggregation.TYPE_DESTINATION);
        agg.setCount(lastId+1);
        transaction.set(counterRef,agg);

        return null;
    }


}
