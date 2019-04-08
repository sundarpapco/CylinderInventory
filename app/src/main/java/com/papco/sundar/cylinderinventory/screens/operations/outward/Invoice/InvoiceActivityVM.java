package com.papco.sundar.cylinderinventory.screens.operations.outward.Invoice;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class InvoiceActivityVM extends AndroidViewModel {

    private MutableLiveData<Allotment> allottment;
    private ListenerRegistration listenerRegistration;
    private boolean areWeInvoicing=false;

    public InvoiceActivityVM(@NonNull Application application) {
        super(application);
        allottment = new MutableLiveData<>();
    }

    public MutableLiveData<Allotment> getAllotment() {
        return allottment;
    }

    public void setAreWeInvoicing(boolean areWeInvoicing) {
        this.areWeInvoicing = areWeInvoicing;
    }

    public void loadAllotment(int allottmentId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (listenerRegistration != null)
            listenerRegistration.remove();

        listenerRegistration = db.collection(DbPaths.COLLECTION_ALLOTMENT).document(Integer.toString(allottmentId))
                .addSnapshotListener((documentSnapshot, e) -> {

                    if (e != null) {
                        Msg.show(getApplication(), "Error connecting to server. Please check internet connection");
                        return;
                    }

                    if (!documentSnapshot.exists()) {
                        if(areWeInvoicing)
                            return;
                        allottment.setValue(null);
                        return;
                    }

                    Allotment loadedAllotment = documentSnapshot.toObject(Allotment.class);
                    if (loadedAllotment.getState() != Allotment.STATE_READY_FOR_INVOICE)
                        allottment.setValue(null);

                    allottment.setValue(loadedAllotment);
                });

    }

    public Allotment getAllotmentObject() {

        return allottment.getValue();

    }


    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null)
            listenerRegistration.remove();
    }
}
