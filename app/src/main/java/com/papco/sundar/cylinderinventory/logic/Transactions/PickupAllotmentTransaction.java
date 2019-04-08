package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PickupAllotmentTransaction extends BaseTransaction {

    private int allotmentId;
    private Allotment allotment;

    public PickupAllotmentTransaction(int allotmentId){
        this.allotmentId=allotmentId;
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        checkPrefetch();

        initializeCylinders();

        // ********** Check allotment document for existence and correct state
        DocumentReference allotmentRef=getDb().collection(DbPaths.COLLECTION_ALLOTMENT).document(Integer.toString(allotmentId));
        DocumentSnapshot allotmentDoc=transaction.get(allotmentRef);

        if(!allotmentDoc.exists())
            throw new FirebaseFirestoreException("Invalid allotment",FirebaseFirestoreException.Code.CANCELLED);
        else
            allotment=allotmentDoc.toObject(Allotment.class);

        if(allotment.getState()!=Allotment.STATE_PICKED_UP)
            throw new FirebaseFirestoreException("Invalid allotment state. Maybe already picked up",
                    FirebaseFirestoreException.Code.CANCELLED);


        // ************* Check all allotted cylinders to be as per requirement
        checkCylinderForMatching(allotment.getRequirement());


        // ************ write and prepare the allotment for ready to invoice
        allotment.setState(Allotment.STATE_READY_FOR_INVOICE);
        transaction.set(allotmentRef,allotment);
        return null;
    }

    private void checkCylinderForMatching(HashMap<String,Integer> map)
            throws FirebaseFirestoreException{

        List<DocumentSnapshot> cylinderDocuments= getPrefetchDocuments();

        Set<String> keys=map.keySet();

        if(getMasterList().size()!=keys.size())
            throw new FirebaseFirestoreException("Number of Cylinder types not matching with requirement",
                    FirebaseFirestoreException.Code.CANCELLED);

        int count;
        String msg;
        String key;
        for(List<Cylinder> monoList:getMasterList()){

            key=monoList.get(0).getCylinderTypeName();

            if(!map.containsKey(key)){
                msg="Cylinder type "+ key +" not required. But allotted";
                throw new FirebaseFirestoreException(msg,FirebaseFirestoreException.Code.CANCELLED);
            }

            count=map.get(key);
            if(monoList.size()!=count){
                msg="Number of "+monoList.get(0).getCylinderTypeName()+" cylinders not matching with requirement";
                throw new FirebaseFirestoreException(msg,FirebaseFirestoreException.Code.CANCELLED);
            }

        }

        //set the cylindersType and cylinders to the allotment
        allotment.setCylindersAndTypes(getMasterList());

    }

    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {

        //Cylinder should not be empty or damaged and also the cylinder should be in warehouse only

        String cylinderNo="Cylinder number "+cylinder.getStringId()+" ";

        if(cylinder.getLocationId()!= Destination.TYPE_WAREHOUSE)
            throw new FirebaseFirestoreException(cylinderNo+"is not in warehouse",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(cylinder.isDamaged())
            throw new FirebaseFirestoreException(cylinderNo+"is damaged",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(cylinder.isEmpty())
            throw new FirebaseFirestoreException(cylinderNo+"is empty",
                    FirebaseFirestoreException.Code.CANCELLED);


    }
}
