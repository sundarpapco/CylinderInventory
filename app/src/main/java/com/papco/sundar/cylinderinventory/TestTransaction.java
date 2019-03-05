package com.papco.sundar.cylinderinventory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;

public class TestTransaction implements Transaction.Function<Void> {

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        DocumentReference ref=db.document(DbPaths.COUNT_CYLINDERS_TOTAL);
        ref.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

            }
        });

        exceptionMethod();


        return null;
    }


    private void exceptionMethod() throws FirebaseFirestoreException{

        int i=5;
        i=i+6;

        throw new FirebaseFirestoreException("Sundar throwed exception",FirebaseFirestoreException.Code.CANCELLED);

    }
}
