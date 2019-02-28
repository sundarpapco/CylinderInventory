package com.papco.sundar.cylinderinventory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;

public class TestTransaction implements Transaction.Function<Void> {

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        DocumentReference reference=db.document(DbPaths.COUNT_CYLINDERS_TOTAL);

        Aggregation agg=new Aggregation();
        agg.setType(Aggregation.TYPE_CYLINDERS);
        agg.setCount(23);
        transaction.set(reference,agg);

        //throw new FirebaseFirestoreException("Invalid inputs",FirebaseFirestoreException.Code.ABORTED);

        return null;
    }
}
