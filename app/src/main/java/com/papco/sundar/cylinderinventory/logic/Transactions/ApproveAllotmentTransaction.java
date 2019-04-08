package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.helpers.ApprovalOperation;
import com.papco.sundar.cylinderinventory.helpers.CounterAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ApproveAllotmentTransaction extends BaseTransaction {

    private Allotment allotment;
    private Allotment laterAllotment;
    private List<ApprovalOperation> approvals;

    public ApproveAllotmentTransaction(Allotment allotment, Allotment laterAllotment) {
        this.allotment = allotment;
        this.laterAllotment=laterAllotment;
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        // ------------------- First prepare the current allotment for approval
        DocumentReference allotmentRef=db.collection(DbPaths.COLLECTION_ALLOTMENT).document(allotment.getStringId());
        DocumentSnapshot allotmentDocument;

        allotmentDocument=transaction.get(allotmentRef);

        if(!allotmentDocument.exists())
            throw new FirebaseFirestoreException("Error! Allotment not found",FirebaseFirestoreException.Code.CANCELLED);
        else{
            int state=allotmentDocument.getLong("state").intValue();
            if(state!=Allotment.STATE_ALLOTTED)
                throw new FirebaseFirestoreException("Allotment already approved",FirebaseFirestoreException.Code.CANCELLED);
        }

        //Allotment state is perfect. Now prepare the approvals for approval
        approvals=initializeApproval(transaction);

        //So, now we can write the approved allotment here. But if we want to create another allotment
        // for later approval prepare it first since we need to get aggregation document

        // *********** Check whether we need to create new allotment
        if(laterAllotment==null) {
            approveAllotment(transaction,allotmentRef);
            return null;
        }

        CounterAggregation allotmentCounter=new CounterAggregation(DbPaths.COUNT_ALLOTMENT);
        allotmentCounter.initializeWith(transaction);

        DocumentReference newAllotmentRef=db.collection(DbPaths.COLLECTION_ALLOTMENT)
                .document(Integer.toString(allotmentCounter.getNextNumber()));

        laterAllotment.setId(allotmentCounter.getNextNumber());

        //write all the values
        approveAllotment(transaction,allotmentRef);
        allotmentCounter.writeWith(transaction);
        transaction.set(newAllotmentRef,laterAllotment);

        return null;
    }


    private List<ApprovalOperation> initializeApproval(Transaction transaction) throws FirebaseFirestoreException{

        HashMap<String,Integer> map=allotment.getRequirement();
        List<ApprovalOperation> approvals=new ArrayList<>();
        for(String key:map.keySet()){
            ApprovalOperation approval=new ApprovalOperation(key,(int)map.get(key));
            approval.initializeWith(transaction);
            approvals.add(approval);
        }

        return approvals;

    }

    private void approveAllotment(Transaction transaction,DocumentReference allotmentRef) throws FirebaseFirestoreException{

        for(ApprovalOperation approval:approvals)
            approval.approveWith(transaction);

        transaction.set(allotmentRef,allotment);

    }
}
