package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.app.Application;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class AllotmentActivityVM extends AndroidViewModel {


    private MutableLiveData<List<Allotment>> allotmentList;
    private ListenerRegistration listenerRegistration;

    public AllotmentActivityVM(@NonNull Application application) {
        super(application);
        allotmentList = new MutableLiveData<>();
        loadAllotments();
    }

    public MutableLiveData<List<Allotment>> getAllotmentList() {
        return allotmentList;
    }

    private void loadAllotments() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (listenerRegistration != null)
            listenerRegistration.remove();

        listenerRegistration = db.collection(DbPaths.COLLECTION_ALLOTMENT)
                .addSnapshotListener((querySnapshot, e) -> {

                    if (e != null) {
                        Msg.show(getApplication(), "Error connecting to server. Please check internet connection");
                        return;
                    }

                    List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                    List<Allotment> allotments = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                        allotments.add(documentSnapshot.toObject(Allotment.class));
                    }

                    allotmentList.setValue(allotments);
                });

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(listenerRegistration!=null)
            listenerRegistration.remove();
    }
}
