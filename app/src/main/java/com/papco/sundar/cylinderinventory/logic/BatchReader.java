package com.papco.sundar.cylinderinventory.logic;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BatchReader {

    private List<Integer> documentIds;
    private BatchReaderListener callback;
    private int counter = 0;
    private List<DocumentSnapshot> resultList;
    private FirebaseFirestore db;
    private boolean prefetchFailed = false;

    public BatchReader(List<Integer> documentIds, BatchReaderListener callback) {
        this.documentIds = documentIds;
        this.callback = callback;
        db = FirebaseFirestore.getInstance();
        resultList = new ArrayList<>(documentIds.size() + 1);
    }

    public void fetchDocuments() {

        for (int i = 0; i < documentIds.size(); ++i) {

            DocumentReference reference = db.collection(DbPaths.COLLECTION_CYLINDERS).document(Integer.toString(documentIds.get(i)));
            reference.get(Source.SERVER).addOnCompleteListener(new CylinderListener());

        }

    }

    public void onReadComplete() {

        if (callback == null)
            return;

        if (prefetchFailed)
            callback.onBatchReadComplete(null);
        else
            callback.onBatchReadComplete(resultList);
    }


    class CylinderListener implements OnCompleteListener<DocumentSnapshot> {


        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful())
                resultList.add(task.getResult());
            else
                prefetchFailed = true;

            counter++;
            if (counter >= documentIds.size())
                onReadComplete();
        }
    }


    interface BatchReaderListener {

        void onBatchReadComplete(List<DocumentSnapshot> documentSnapshots);

    }
}
