package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.CylinderType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EditCylinderTypeTransaction extends BaseTransaction {

    private CylinderType cylinderType;

    public EditCylinderTypeTransaction(@NonNull CylinderType cylinderType){

        this.cylinderType=cylinderType;
    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference typeRef=db.collection(DbPaths.COLLECTION_CYLINDER_TYPES)
                .document(cylinderType.getName());
        transaction.set(typeRef,cylinderType);

        return null;
    }
}
