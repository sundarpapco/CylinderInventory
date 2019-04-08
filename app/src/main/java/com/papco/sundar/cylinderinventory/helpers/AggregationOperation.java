package com.papco.sundar.cylinderinventory.helpers;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.CylinderType;

public class AggregationOperation {


    private String cylinderType;
    private int cylinderCount;
    private FirebaseFirestore db;

    private DocumentReference sourceReference;
    private DocumentReference destinationReference;
    private DocumentReference approvalReference;

    private Aggregation source;
    private Aggregation destination;
    private Aggregation approval;

    public AggregationOperation(String cylinderType, int batchType,int cylinderCount) {

        db = FirebaseFirestore.getInstance();
        this.cylinderType = cylinderType;
        this.cylinderCount=cylinderCount;

        switch (batchType) {

            case Batch.TYPE_INVOICE:
                initializeForInvoice();
                break;

            case Batch.TYPE_ECR:
                initializeForEcr();
                break;

            case Batch.TYPE_REFILL:
                initializeForRefill();
                break;

            case Batch.TYPE_FCI:
                initializeForFci();
                break;

            case Batch.TYPE_REPAIR:
                initializeForRepair();
                break;

            case Batch.TYPE_RCI:
                initializeForRci();
                break;

        }

    }

    public void initializeWith(Transaction transaction) throws FirebaseFirestoreException {


        DocumentSnapshot sourceDocument=transaction.get(sourceReference);

        if(sourceDocument.exists()){
            source=sourceDocument.toObject(Aggregation.class);
            source.setCount(source.getCount()-cylinderCount);
            if(source.getCount()<0)
                source.setCount(0);
        }else{
            source=new Aggregation();
            source.setCount(0);
        }

        DocumentSnapshot destinationDocument=transaction.get(destinationReference);

        if(destinationDocument.exists()){
            destination=destinationDocument.toObject(Aggregation.class);
            destination.setCount(destination.getCount()+cylinderCount);

        }else{
            destination=new Aggregation();
            destination.setCount(cylinderCount);
        }

        if(approvalReference==null)
            return;

        DocumentSnapshot approvalDocument=transaction.get(approvalReference);
        if(approvalDocument.exists()){
            approval=approvalDocument.toObject(Aggregation.class);
            approval.setCount(approval.getCount()-cylinderCount);
            if(approval.getCount()<0)
                approval.setCount(0);
        }else{
            approval=new Aggregation();
            approval.setCount(0);
        }

    }

    public void writeWith(Transaction transaction) throws FirebaseFirestoreException{

        transaction.set(sourceReference,source);
        transaction.set(destinationReference,destination);

        if(approvalReference!=null)
            transaction.set(approvalReference,approval);

    }


    //************** initializing methods ********************************

    private void initializeForInvoice() {

        if (cylinderType.equals(CylinderType.TYPE_GLOBAL)) {

            sourceReference = db.document(DbPaths.COUNT_CYLINDERS_FULL);
            destinationReference = db.document(DbPaths.COUNT_CYLINDERS_CLIENT);
            approvalReference = null;

        } else {

            sourceReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.FULL));
            destinationReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.CLIENT));
            approvalReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.APPROVED));

        }

    }

    private void initializeForEcr() {

        if (cylinderType.equals(CylinderType.TYPE_GLOBAL)) {

            sourceReference = db.document(DbPaths.COUNT_CYLINDERS_CLIENT);
            destinationReference = db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
            approvalReference = null;

        } else {

            sourceReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.CLIENT));
            destinationReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.EMPTY));
            approvalReference = null;

        }

    }

    private void initializeForRefill() {

        if (cylinderType.equals(CylinderType.TYPE_GLOBAL)) {

            sourceReference = db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
            destinationReference = db.document(DbPaths.COUNT_CYLINDERS_REFILL_STATION);
            approvalReference = null;

        } else {

            sourceReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.EMPTY));
            destinationReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.REFILL_STATIONS));
            approvalReference = null;

        }

    }

    private void initializeForFci() {

        if (cylinderType.equals(CylinderType.TYPE_GLOBAL)) {

            sourceReference = db.document(DbPaths.COUNT_CYLINDERS_REFILL_STATION);
            destinationReference = db.document(DbPaths.COUNT_CYLINDERS_FULL);
            approvalReference = null;

        } else {

            sourceReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.REFILL_STATIONS));
            destinationReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.FULL));
            approvalReference = null;

        }

    }

    private void initializeForRepair() {

        if (cylinderType.equals(CylinderType.TYPE_GLOBAL)) {

            sourceReference = db.document(DbPaths.COUNT_CYLINDERS_DAMAGED);
            destinationReference = db.document(DbPaths.COUNT_CYLINDERS_REPAIR_STATION);
            approvalReference = null;

        } else {

            sourceReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.DAMAGED));
            destinationReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.REPAIR_STATIONS));
            approvalReference = null;

        }

    }

    private void initializeForRci() {

        if (cylinderType.equals(CylinderType.TYPE_GLOBAL)) {

            sourceReference = db.document(DbPaths.COUNT_CYLINDERS_REPAIR_STATION);
            destinationReference = db.document(DbPaths.COUNT_CYLINDERS_EMPTY);
            approvalReference = null;

        } else {

            sourceReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.REPAIR_STATIONS));
            destinationReference = db.document(DbPaths.getAggregationForType(cylinderType, DbPaths.AggregationType.EMPTY));
            approvalReference = null;

        }

    }

}
