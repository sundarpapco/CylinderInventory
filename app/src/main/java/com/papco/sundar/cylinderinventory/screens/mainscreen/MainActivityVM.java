package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.app.Application;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


public class MainActivityVM extends AndroidViewModel {

    private MutableLiveData<QuerySnapshot> firstPage;
    private MutableLiveData<List<DocumentSnapshot>> loadedPage;
    private MutableLiveData<Batch> searchedBatch;
    private List<DocumentSnapshot> feedBackup;
    private ListenerRegistration firstPageListener;
    private FirebaseFirestore db;
    private Query query;

    public MainActivityVM(@NonNull Application application) {
        super(application);

        firstPage = new MutableLiveData<>();
        loadedPage = new MutableLiveData<>();
        searchedBatch=new MutableLiveData<>();
        feedBackup=null;
        db = FirebaseFirestore.getInstance();

    }

    public List<DocumentSnapshot> getFeedBackup() {

        return feedBackup;
    }

    public void setFeedBackup(List<DocumentSnapshot> feedBackup) {
        this.feedBackup = feedBackup;
    }

    public void loadFirstPage(int typeFilter,long timeFilter) {

        if (firstPageListener != null)
            firstPageListener.remove();

        query = constructQuery(typeFilter,timeFilter);

        firstPageListener = query.addSnapshotListener((querySnapshot, e) -> {

            if (e != null) {
                Msg.show(getApplication(), "Error fetching documents");
                Log.d("SUNDAR", e.getMessage());
                return;
            }

            firstPage.setValue(querySnapshot);
        });
    }

    public void loadNextPage(DocumentSnapshot lastSnapshot) {

        if (lastSnapshot == null)
            return;

        query.startAfter(lastSnapshot)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Msg.show(getApplication(), "Couldn't refresh feed");
                        return;
                    }

                    loadedPage.setValue(task.getResult().getDocuments());

                });

    }

    public MutableLiveData<QuerySnapshot> getFirstPage() {
        return firstPage;
    }

    public MutableLiveData<List<DocumentSnapshot>> getLoadedPage() {
        return loadedPage;
    }

    public MutableLiveData<Batch> getSearchedBatch() {
        return searchedBatch;
    }

    public void searchBatch(String batchNumber){

        db.collection(DbPaths.COLLECTION_BATCHES).document(batchNumber)
                .get()
                .addOnCompleteListener(task -> {

                    if(!task.isSuccessful()){
                        searchedBatch.setValue(null);
                    }else{
                        DocumentSnapshot snapshot=task.getResult();
                        Batch batch;
                        if(snapshot.exists())
                            batch=snapshot.toObject(Batch.class);
                        else
                            batch=null;

                        searchedBatch.setValue(batch);
                    }

                });
    }

    private Query constructQuery(int typeFilter,long timeFilter) {

        query = db.collection(DbPaths.COLLECTION_BATCHES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(BatchFeedScrollListener.PAGE_SIZE);

        if(timeFilter!=-1)
            query=query.whereLessThanOrEqualTo("timestamp",timeFilter);

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

    @Override
    protected void onCleared() {
        super.onCleared();
        if(firstPageListener!=null)
            firstPageListener.remove();
    }
}
