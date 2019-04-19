package com.papco.sundar.cylinderinventory.screens.cylinders.history;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.screens.mainscreen.MainActivity;

import java.util.List;

import androidx.annotation.NonNull;

public class CylHistoryDataSource {

    private Query query;
    private Context context;
    private List<DocumentSnapshot> loadedDataCache;
    private List<DocumentSnapshot> configChangeBackup;
    private CylHistoryDataSource.Callback callback;

    CylHistoryDataSource(@NonNull Context context) {

        this.context = context;
    }

    public void setCallback(CylHistoryDataSource.Callback callback) {
        this.callback = callback;
    }

    void setConfigChangeBackup(List<DocumentSnapshot> configChangeBackup) {
        this.configChangeBackup = configChangeBackup;
    }

    private Query constructQuery(int typeFilter, long timeFilter, int cylinderNumber) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        query = db.collection(DbPaths.COLLECTION_BATCHES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereArrayContains("cylinders", cylinderNumber)
                .limit(25);

        if (timeFilter != -1)
            query = query.whereLessThanOrEqualTo("timestamp", timeFilter);

        switch (typeFilter) {

            case MainActivity.FILTER_INVOICE:
                query = query.whereEqualTo("type", Batch.TYPE_INVOICE);
                return query;

            case MainActivity.FILTER_ECR:
                query = query.whereEqualTo("type", Batch.TYPE_ECR);
                return query;

            case MainActivity.FILTER_REFILL:
                query = query.whereEqualTo("type", Batch.TYPE_REFILL);
                return query;

            case MainActivity.FILTER_FCI:
                query = query.whereEqualTo("type", Batch.TYPE_FCI);
                return query;

            case MainActivity.FILTER_REPAIR:
                query = query.whereEqualTo("type", Batch.TYPE_REPAIR);
                return query;

            case MainActivity.FILTER_RCI:
                query = query.whereEqualTo("type", Batch.TYPE_RCI);
                return query;
        }

        return query;
    }

    void loadInitialData(int typeFilter, long timeFilter, int cylinderNumber) {


        // if the observer is starting to load after the config change
        if (configChangeBackup != null && callback != null) {

            callback.onLoadComplete(configChangeBackup);
            configChangeBackup = null;
            if (loadedDataCache != null) {
                callback.onLoadComplete(loadedDataCache);
                loadedDataCache = null;
            }
            return;
        }

        query = constructQuery(typeFilter, timeFilter, cylinderNumber);

        query.get().addOnCompleteListener(task -> {

            if(!task.isSuccessful()){
                Msg.show(context,"Error fetching data");
                Log.d("SUNDAR", task.getException().getMessage());
                return;
            }

            if (callback != null)
                callback.onLoadComplete(task.getResult().getDocuments());
            else
                loadedDataCache = task.getResult().getDocuments();


        });

    }

    void loadMoreData(DocumentSnapshot lastDocument){

        if(lastDocument==null)
            return;

        query.startAfter(lastDocument).get().addOnCompleteListener(task -> {

            if(!task.isSuccessful()){
                Msg.show(context,"Error fetching data");
                return;
            }

            if(callback!=null)
                callback.onLoadComplete(task.getResult().getDocuments());
            else
                loadedDataCache=task.getResult().getDocuments();

        });

    }


    void clearCallback() {

        this.callback = null;
    }


    public interface Callback {

        void onLoadComplete(List<DocumentSnapshot> initialData);

    }
}
