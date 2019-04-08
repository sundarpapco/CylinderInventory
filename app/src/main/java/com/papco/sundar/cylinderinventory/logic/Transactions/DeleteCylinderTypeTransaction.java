package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DeleteCylinderTypeTransaction extends BaseTransaction {

    private String typeName;

    public DeleteCylinderTypeTransaction(String typeName){

        this.typeName=typeName;
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference typeRef=db.collection(DbPaths.COLLECTION_CYLINDER_TYPES)
                .document(typeName);
        DocumentSnapshot snapshot=transaction.get(typeRef);

        DocumentReference fullCylinderRef=db.document(DbPaths.getAggregationForType(typeName, DbPaths.AggregationType.FULL));
        DocumentReference emptyCylinderRef=db.document(DbPaths.getAggregationForType(typeName, DbPaths.AggregationType.EMPTY));
        DocumentReference clientsCylinderRef=db.document(DbPaths.getAggregationForType(typeName, DbPaths.AggregationType.CLIENT));
        DocumentReference damagedCylinderRef=db.document(DbPaths.getAggregationForType(typeName, DbPaths.AggregationType.DAMAGED));
        DocumentReference refillCylinderRef=db.document(DbPaths.getAggregationForType(typeName, DbPaths.AggregationType.REFILL_STATIONS));
        DocumentReference repairCylinderRef=db.document(DbPaths.getAggregationForType(typeName, DbPaths.AggregationType.REPAIR_STATIONS));

        transaction.delete(fullCylinderRef);
        transaction.delete(emptyCylinderRef);
        transaction.delete(clientsCylinderRef);
        transaction.delete(damagedCylinderRef);
        transaction.delete(refillCylinderRef);
        transaction.delete(repairCylinderRef);
        transaction.delete(typeRef);

        return null;
    }
}
