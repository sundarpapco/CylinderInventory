package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.app.Application;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


public class MainActivityVM extends AndroidViewModel {

    private MutableLiveData<Batch> searchedBatch;
    private FeedDataSource feedDataSource;
    private Batch batchToDelete;

    public MainActivityVM(@NonNull Application application) {
        super(application);
        searchedBatch=new MutableLiveData<>();

    }

    FeedDataSource getFeedDataSource() {

        if(feedDataSource==null)
            feedDataSource=new FeedDataSource(getApplication());

        return feedDataSource;

    }

    Batch getBatchToDelete() {
        return batchToDelete;
    }

    void setBatchToDelete(Batch batchToDelete) {
        this.batchToDelete = batchToDelete;
    }

    MutableLiveData<Batch> getSearchedBatch() {
        return searchedBatch;
    }

    void searchBatch(String batchNumber){

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        db.collection(DbPaths.COLLECTION_BATCHES).document(batchNumber)
                .get()
                .addOnCompleteListener(task -> {

                    if(!task.isSuccessful()){
                        searchedBatch.setValue(null);
                    }else{
                        DocumentSnapshot snapshot=task.getResult();
                        Batch batch;
                        if(snapshot!=null && snapshot.exists())
                            batch=snapshot.toObject(Batch.class);
                        else
                            batch=null;

                        searchedBatch.setValue(batch);
                    }

                });
    }


    @Override
    protected void onCleared() {
        super.onCleared();
       if(feedDataSource!=null)
           feedDataSource.onDestroy();
       feedDataSource=null;
    }
}
