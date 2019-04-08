package com.papco.sundar.cylinderinventory.screens.batchDetail;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.Batch;

import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class BatchDetailVM extends AndroidViewModel {

    private MutableLiveData<Batch> batch;
    private ListenerRegistration listenerRegistration;

    public BatchDetailVM(@NonNull Application application) {
        super(application);
        batch = new MutableLiveData<>();
    }

    public MutableLiveData<Batch> getBatch() {

        return batch;
    }

    public void loadBatch(String batchNumber) {

        if (listenerRegistration != null)
            listenerRegistration.remove();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(DbPaths.COLLECTION_BATCHES).document(batchNumber)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Msg.show(getApplication(), "Error connecting to server. Please check internet connection");
                            return;
                        }

                        if (!documentSnapshot.exists()) {
                            batch.setValue(null);
                            return;
                        }

                        Batch loadedBatch = documentSnapshot.toObject(Batch.class);
                        batch.setValue(loadedBatch);
                    }

                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(listenerRegistration!=null)
            listenerRegistration.remove();
    }
}
