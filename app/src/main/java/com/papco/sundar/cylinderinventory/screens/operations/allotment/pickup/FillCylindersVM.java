package com.papco.sundar.cylinderinventory.screens.operations.allotment.pickup;

import android.app.Application;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class FillCylindersVM extends AndroidViewModel {

    private List<Integer> cylindersBackup;
    private MutableLiveData<Allotment> currentAllotment;
    private ListenerRegistration allotmentListener;
    private boolean areWeApproving=false;
    private boolean allotmentLoaded=false;

    public FillCylindersVM(@NonNull Application application) {
        super(application);
        currentAllotment = new MutableLiveData<>();
    }

    public boolean isAreWeApproving() {
        return areWeApproving;
    }

    public void setAreWeApproving(boolean areWeApproving) {
        this.areWeApproving = areWeApproving;
    }

    public List<Integer> getCylindersBackup() {
        return cylindersBackup;
    }

    public void setCylindersBackup(List<Integer> cylindersBackup) {
        this.cylindersBackup = cylindersBackup;
    }

    public MutableLiveData<Allotment> getCurrentAllotment() {
        return currentAllotment;
    }

    public void loadAllotment(int allotmentId) {

        if (allotmentListener != null)
            allotmentListener.remove();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        allotmentListener = db.collection(DbPaths.COLLECTION_ALLOTMENT).document(Integer.toString(allotmentId))
                .addSnapshotListener((documentSnapshot, e) -> {

                    if (e != null) {
                        Msg.show(getApplication(), e.getMessage());
                        return;
                    }

                    if (areWeApproving) //when we fill and save, then this document will change. This will check if we are the one who is approving
                        return;

                    if (documentSnapshot.exists()) {

                        Allotment allotment = documentSnapshot.toObject(Allotment.class);
                        if (allotment.getState() == Allotment.STATE_APPROVED) { //this allotment has been approved somewhere else
                            currentAllotment.setValue(allotment);
                            allotmentLoaded=true;
                            markAllotmentAsPickedUp(documentSnapshot);
                            return;
                        }

                        if(allotment.getState()==Allotment.STATE_PICKED_UP) {
                            if(!allotmentLoaded) {
                                currentAllotment.setValue(allotment);
                                allotmentLoaded = true;
                            }

                            return;
                        }

                        currentAllotment.setValue(null);

                    } else
                        currentAllotment.setValue(null); //this allotment has been deleted somewhere else

                });
    }

    private void markAllotmentAsPickedUp(DocumentSnapshot documentSnapshot){

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        Allotment allotment=documentSnapshot.toObject(Allotment.class);
        allotment.setState(Allotment.STATE_PICKED_UP);

        db.collection(DbPaths.COLLECTION_ALLOTMENT).document(allotment.getStringId())
                .set(allotment).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Msg.show(getApplication(),"Error connecting to server. Please check internet connection");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(allotmentListener!=null)
            allotmentListener.remove();
    }
}
