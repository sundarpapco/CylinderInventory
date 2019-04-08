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
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.Destination;

import java.util.Calendar;

public class CreateAllotmentTransaction extends BaseTransaction {

    private Allotment allotment;

    public CreateAllotmentTransaction(@NonNull Allotment newAllotment) {
        allotment=newAllotment;
    }


    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        Aggregation allotmentAgg;
        Destination client;

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference counterRef=db.document(DbPaths.COUNT_ALLOTMENT);
        DocumentReference clientRef=db.collection(DbPaths.COLLECTION_DESTINATIONS).document(Integer.toString(allotment.getClientId()));

        DocumentSnapshot counterDoc=transaction.get(counterRef);
        DocumentSnapshot clientDoc=transaction.get(clientRef);

        //Checking the client. The client may be deleted by some other parallel transaction. So
        // we should check for the existence of the the client and also, we should set the
        //editable flag of the destination to false to disable further editing or deleting

        if(!clientDoc.exists())
            throw new FirebaseFirestoreException("Invalid client!",
                    FirebaseFirestoreException.Code.CANCELLED);
        else{
            client=clientDoc.toObject(Destination.class);
            client.setEditable(false);
        }

        //get the next allocation number or if doesn't exists, create the first number

        if(!counterDoc.exists()){
            allotmentAgg=new Aggregation();
            allotmentAgg.setType(Aggregation.TYPE_ALLOTMENT);
            allotmentAgg.setCount(1);
        }else{
            allotmentAgg=counterDoc.toObject(Aggregation.class);
            allotmentAgg.setCount(allotmentAgg.getCount()+1);
        }


        allotment.setId(allotmentAgg.getCount());
        allotment.setTimeStamp(Calendar.getInstance().getTimeInMillis());

        DocumentReference newAllotmentRef=db.collection(DbPaths.COLLECTION_ALLOTMENT)
                .document(allotment.getStringId());

        transaction.set(clientRef,client);
        transaction.set(newAllotmentRef,allotment);
        transaction.set(counterRef,allotmentAgg);

        return null;
    }
}
