package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.historyList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class DestinationHistoryData {

    public static final int PAGE_SIZE = 25;
    public static final int PREFETCH_DISTANCE = 5;
    private MutableLiveData<List<DocumentSnapshot>> batches;
    private int destinationId;
    private boolean isThereMoreToLoad = true;
    private boolean isLoading = false;
    private List<DocumentSnapshot> loadedBackup;

    public DestinationHistoryData(int destinationId) {
        this.destinationId = destinationId;
        batches=new MutableLiveData<>();
    }

    public int getDestinationId() {
        return destinationId;
    }

    public MutableLiveData<List<DocumentSnapshot>> getBatches() {
        return batches;
    }

    public void loadMoreData(DocumentSnapshot lastSnapshot) {

        if (!isThereMoreToLoad)
            return;

        if (isLoading)
            return;
        else
            isLoading = true;

        if(lastSnapshot==null && loadedBackup!=null){
            isLoading=false;
            batches.setValue(loadedBackup);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection(DbPaths.COLLECTION_BATCHES)
                .whereEqualTo("destinationId", destinationId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastSnapshot != null)
            query = query.startAfter(lastSnapshot);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                isLoading = false;
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() < PAGE_SIZE)
                        isThereMoreToLoad = false;
                    if(loadedBackup==null)
                        loadedBackup=new ArrayList<>();

                    loadedBackup.addAll(task.getResult().getDocuments());

                    batches.setValue(task.getResult().getDocuments());
                } else {
                    batches.setValue(null);
                }
            }
        });

    }

}
