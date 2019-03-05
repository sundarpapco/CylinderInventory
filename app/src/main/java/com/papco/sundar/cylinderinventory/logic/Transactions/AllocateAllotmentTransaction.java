package com.papco.sundar.cylinderinventory.logic.Transactions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AllocateAllotmentTransaction extends BaseTransaction {

    private int allocationId;
    private List<DocumentSnapshot> cylinders;

    public AllocateAllotmentTransaction(int allocationId) {
        this.allocationId = allocationId;
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference allotmentRef=db.collection(DbPaths.COLLECTION_ALLOTMENT).document(Integer.toString(allocationId));
        Allotment allotment;

        cylinders=getPrefetchedDocuments();
        if(cylinders==null)
            throw new FirebaseFirestoreException("No prefetch found",
                    FirebaseFirestoreException.Code.CANCELLED);

        //Checking if the allotment is valid
        DocumentSnapshot allotmentDoc=transaction.get(allotmentRef);
        if(!allotmentDoc.exists())
            throw new FirebaseFirestoreException("This allotment was already closed",FirebaseFirestoreException.Code.CANCELLED);
        else
            allotment=allotmentDoc.toObject(Allotment.class);


        if(allotment.getState()== Allotment.STATE_READY_FOR_INVOICE)
            throw new FirebaseFirestoreException("This allotment was already filled and now ready to be invoiced",FirebaseFirestoreException.Code.CANCELLED);


        //Checking all the cylinders for validity
        List<Integer> cylinderIds=new ArrayList<>();
        Cylinder cylinder;

        for(DocumentSnapshot snapshot:cylinders){


            if(!snapshot.exists()){

                String msg="Some cylinders not found. It may be deleted or not created";
                throw new FirebaseFirestoreException(msg,FirebaseFirestoreException.Code.CANCELLED);

            }
            cylinder=snapshot.toObject(Cylinder.class);


            if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE) {

                String msg="Cylinder number "+cylinder.getStringId()
                        +" is not in warehouse. Please check";
                throw new FirebaseFirestoreException(msg,FirebaseFirestoreException.Code.CANCELLED);
            }

            if(cylinder.isDamaged()) {

                String msg="Cylinder number "+cylinder.getStringId()
                        +" is damaged. Please check";
                throw new FirebaseFirestoreException(msg,FirebaseFirestoreException.Code.CANCELLED);
            }

            if(cylinder.isEmpty()) {

                String msg="Cylinder number "+cylinder.getStringId()
                        +" is empty. Please check";
                throw new FirebaseFirestoreException(msg,FirebaseFirestoreException.Code.CANCELLED);
            }

            cylinderIds.add(cylinder.getCylinderNo());

        }

        allotment.setState(Allotment.STATE_READY_FOR_INVOICE);
        allotment.setCylinders(cylinderIds);

        transaction.set(allotmentRef,allotment);

        return null;
    }

}
