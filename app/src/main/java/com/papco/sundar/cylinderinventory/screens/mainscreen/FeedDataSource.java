package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;

import java.util.List;

import androidx.annotation.NonNull;

public class FeedDataSource {

    private Query query;
    private Context context;
    private List<DocumentSnapshot> initialDataCache;
    private List<DocumentSnapshot> configChangeBackup;
    private ListenerRegistration listenerRegistration;
    private Callback callback;

    FeedDataSource(@NonNull Context context) {

        this.context = context;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    void setConfigChangeBackup(List<DocumentSnapshot> configChangeBackup) {
        this.configChangeBackup = configChangeBackup;
    }



    private Query constructQuery(int typeFilter, long timeFilter) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        query = db.collection(DbPaths.COLLECTION_BATCHES)
                .orderBy("timestamp", Query.Direction.DESCENDING);

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

    void loadInitialData(int typeFilter, long timeFilter) {


        // if the observer is starting to load after the config change
        if (configChangeBackup != null && callback != null) {

            callback.onInitialLoadComplete(configChangeBackup);
            configChangeBackup = null;
            if (initialDataCache != null) {
                callback.onInitialLoadComplete(initialDataCache);
                initialDataCache = null;
            }
            return;
        }


        if (listenerRegistration != null)
            listenerRegistration.remove();

        query = constructQuery(typeFilter, timeFilter);

        listenerRegistration = query.limit(BatchFeedScrollListener.PAGE_SIZE)
                .addSnapshotListener((querySnapshot, e) -> {

                    if (e != null) {
                        if(e.getCode()==FirebaseFirestoreException.Code.CANCELLED)
                            Msg.show(context,e.getMessage());
                        else
                            Msg.show(context,"Couldn't refresh feed");
                        return;
                    }

                    if (callback != null)
                        callback.onInitialLoadComplete(querySnapshot.getDocuments());
                    else
                        initialDataCache = querySnapshot.getDocuments();
                });

    }

    void loadAfter(DocumentSnapshot lastSnapshot) {

        if (lastSnapshot == null)
            return;

        query.limit(BatchFeedScrollListener.PAGE_SIZE)
                .startAfter(lastSnapshot)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Msg.show(context, "Couldn't refresh feed");
                        return;
                    }

                    if (callback != null)
                        callback.onLoadNextComplete(task.getResult().getDocuments());

                });

    }

    void loadBefore(DocumentSnapshot snapshot) {

        if (snapshot == null)
            return;

        query.endBefore(snapshot)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Msg.show(context, "Couldn't refresh feed");
                        return;
                    }

                    if (callback != null)
                        callback.onLoadPreviousComplete(task.getResult().getDocuments());

                });

    }


    void onDestroy() {

        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    void clearCallback() {

        this.callback = null;
    }



    public interface Callback {

        void onInitialLoadComplete(List<DocumentSnapshot> initialData);

        void onLoadNextComplete(List<DocumentSnapshot> nextPageData);

        void onLoadPreviousComplete(List<DocumentSnapshot> previousData);

    }
}
