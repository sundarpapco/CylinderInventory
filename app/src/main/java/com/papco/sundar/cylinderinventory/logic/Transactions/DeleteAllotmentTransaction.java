package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.helpers.ApprovalOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DeleteAllotmentTransaction extends BaseTransaction {

    private int allotmentId;
    private Allotment allotment;
    private List<ApprovalOperation> approvals;

    public DeleteAllotmentTransaction(int allotmentId) {
        this.allotmentId = allotmentId;
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference allotmentRef=db.collection(DbPaths.COLLECTION_ALLOTMENT).document(Integer.toString(allotmentId));
        DocumentSnapshot allotmentDocument;

        //checking the allotment
        allotmentDocument=transaction.get(allotmentRef);
        if(allotmentDocument.exists()){
            allotment=allotmentDocument.toObject(Allotment.class);
        }else
            throw new FirebaseFirestoreException("Allotment not found or already deleted",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(allotment.getState()==Allotment.STATE_READY_FOR_INVOICE) {
            checkPrefetch();
            initializeCylinders();
        }


        if(allotment.getState()!=Allotment.STATE_ALLOTTED){

            approvals=initializeApproval(transaction);
            for(ApprovalOperation approval:approvals)
                approval.deleteApprovalWith(transaction);
        }

        if(allotment.getState()==Allotment.STATE_READY_FOR_INVOICE)
            writeCylinders(transaction);


        transaction.delete(allotmentRef);

        return null;
    }

    private List<ApprovalOperation> initializeApproval(Transaction transaction) throws FirebaseFirestoreException{

        HashMap<String,Integer> map=allotment.getRequirement();
        List<ApprovalOperation> approvals=new ArrayList<>();
        for(String key:map.keySet()){
            ApprovalOperation approval=new ApprovalOperation(key,map.get(key));
            approval.initializeWith(transaction);
            approvals.add(approval);
        }

        return approvals;

    }

    @Override
    protected void onCylinderValidation(Cylinder cylinder) throws FirebaseFirestoreException {
        cylinder.setAlloted(false);
    }
}
