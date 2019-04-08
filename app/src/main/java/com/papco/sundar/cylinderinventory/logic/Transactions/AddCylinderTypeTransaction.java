package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;
import com.papco.sundar.cylinderinventory.data.CylinderType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddCylinderTypeTransaction extends BaseTransaction {

    CylinderType cylinderType;

    public AddCylinderTypeTransaction(@NonNull CylinderType cylinderType){

        this.cylinderType=cylinderType;
        cylinderType.setEditable(true);

    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();


        //region ------------ Checking the cylinder type document itself
            // if this cylinder type document already exists, then this is the duplication name
            // throw exception since cylinder type name has to be unique and its the document id

        DocumentReference CylinderTypeReference=db.collection(DbPaths.COLLECTION_CYLINDER_TYPES)
                .document(cylinderType.getName());
        DocumentSnapshot cylinderTypeDocument=transaction.get(CylinderTypeReference);

        if(cylinderTypeDocument.exists()){
            throw new FirebaseFirestoreException("This cylinder type already exists. Try with different name",
                    FirebaseFirestoreException.Code.CANCELLED);
        }

        Aggregation initialAggregation=new Aggregation();
        initialAggregation.setCount(0);
        initialAggregation.setType(Aggregation.TYPE_CYLINDER_TYPES);

        //endregion ------------ types document complete


        //region ---------- initializing the sub collection aggregation for the given type

        DocumentReference fullCylindersRef=
                db.document(DbPaths.getAggregationForType(cylinderType.getName(), DbPaths.AggregationType.FULL));
        DocumentReference emptyCylindersRef=
                db.document(DbPaths.getAggregationForType(cylinderType.getName(), DbPaths.AggregationType.EMPTY));
        DocumentReference clientCylindersRef=
                db.document(DbPaths.getAggregationForType(cylinderType.getName(), DbPaths.AggregationType.CLIENT));
        DocumentReference damagedCylindersRef=
                db.document(DbPaths.getAggregationForType(cylinderType.getName(), DbPaths.AggregationType.DAMAGED));
        DocumentReference refillCylindersRef=
                db.document(DbPaths.getAggregationForType(cylinderType.getName(), DbPaths.AggregationType.REFILL_STATIONS));
        DocumentReference repairCylindersRef=
                db.document(DbPaths.getAggregationForType(cylinderType.getName(), DbPaths.AggregationType.REPAIR_STATIONS));
        DocumentReference approvedCylindersRef=
                db.document(DbPaths.getAggregationForType(cylinderType.getName(), DbPaths.AggregationType.APPROVED));

        //----------- endregion sub collection aggregation complete

        //finally write all values
        transaction.set(CylinderTypeReference,cylinderType);
        transaction.set(fullCylindersRef,initialAggregation);
        transaction.set(emptyCylindersRef,initialAggregation);
        transaction.set(clientCylindersRef,initialAggregation);
        transaction.set(damagedCylindersRef,initialAggregation);
        transaction.set(refillCylindersRef,initialAggregation);
        transaction.set(repairCylindersRef,initialAggregation);
        transaction.set(approvedCylindersRef,initialAggregation);

        return null;
    }



}
