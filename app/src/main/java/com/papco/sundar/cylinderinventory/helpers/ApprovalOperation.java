package com.papco.sundar.cylinderinventory.helpers;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;


// This class is a helper class that is designed to be used inside Transactions to allow easy management
// of cylinder type approval count for approval or delete approval. You cannot use this class for global
// cylinder aggregation


public class ApprovalOperation {

    private int state;

    private String cylinderType;
    private int cylinderCount;
    private DocumentReference approvedRef;
    private DocumentReference fullCylinderRef;
    private Aggregation approval;
    private Aggregation fullCylinder;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final int STATE_NOT_INITIALIZED = 0;
    private final int STATE_INITIALIZED = 1;
    private final int STATE_APPROVED = 2;
    private final int STATE_DISAPPROVED = 3;

    public ApprovalOperation(String cylinderType, int cylinderCount) {

        state=STATE_NOT_INITIALIZED;

        this.cylinderCount = cylinderCount;
        this.cylinderType = cylinderType;

        approvedRef = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.APPROVED));
        fullCylinderRef = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.FULL));

    }

    public void initializeWith(Transaction transaction) throws FirebaseFirestoreException {

        if(state!=STATE_NOT_INITIALIZED)
            throw new FirebaseFirestoreException("You cannot initialize twice",
                    FirebaseFirestoreException.Code.CANCELLED);

        DocumentSnapshot approvalDoc = transaction.get(approvedRef);

        if (!approvalDoc.exists())
            throw new FirebaseFirestoreException("Cylinder type " + cylinderType + " not found",
                    FirebaseFirestoreException.Code.CANCELLED);

        approval = approvalDoc.toObject(Aggregation.class);


        DocumentSnapshot fullCylinderDoc = transaction.get(fullCylinderRef);

        if (!fullCylinderDoc.exists())
            throw new FirebaseFirestoreException("Cylinder type " + cylinderType + " not found",
                    FirebaseFirestoreException.Code.CANCELLED);

        fullCylinder = fullCylinderDoc.toObject(Aggregation.class);

        state=STATE_INITIALIZED;
    }

    public boolean canBeApproved() {

        if (state!=STATE_INITIALIZED)
            return false;

        return fullCylinder.getCount() - approval.getCount() >= cylinderCount;

    }

    public void approveWith(Transaction transaction) throws FirebaseFirestoreException {

        if (state!=STATE_INITIALIZED)
            throw new FirebaseFirestoreException("Not initialized or already approved or disapproved",
                    FirebaseFirestoreException.Code.CANCELLED);

        if(!canBeApproved())
            throw new FirebaseFirestoreException(getOutOfStockMsg(),
                    FirebaseFirestoreException.Code.CANCELLED);

        approval.setCount(approval.getCount() + cylinderCount);
        transaction.set(fullCylinderRef, fullCylinder);
        transaction.set(approvedRef, approval);

        state=STATE_APPROVED;
    }

    public String getOutOfStockMsg() {

        return "Not enough " + cylinderType + " cylinders in stock for approval";
    }

    public void deleteApprovalWith(Transaction transaction) throws FirebaseFirestoreException {

        if (state!=STATE_INITIALIZED)
            throw new FirebaseFirestoreException("Not initialized or already disApproved or Approved",
                    FirebaseFirestoreException.Code.CANCELLED);

        approval.setCount(approval.getCount() - cylinderCount);
        if (approval.getCount() < 0)
            approval.setCount(0);
        transaction.set(fullCylinderRef, fullCylinder);
        transaction.set(approvedRef, approval);

        state=STATE_DISAPPROVED;

    }

}
