package com.papco.sundar.cylinderinventory.screens.operations.allotment.approve;

import android.app.Application;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentListItem;

import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class ApproveAllotmentVM extends AndroidViewModel {

    private MutableLiveData<Allotment> currentAllotment;
    private ListenerRegistration allotmentListener;
    private List<AllotmentListItem> approvalListBackup;
    private boolean areWeApproving=false;

    public ApproveAllotmentVM(@NonNull Application application) {
        super(application);
        currentAllotment=new MutableLiveData<>();
    }

    public MutableLiveData<Allotment> getCurrentAllotment() {
        return currentAllotment;
    }

    public void setCurrentAllotment(MutableLiveData<Allotment> currentAllotment) {
        this.currentAllotment = currentAllotment;
    }

    public List<AllotmentListItem> getApprovalListBackup() {
        return approvalListBackup;
    }

    public void setApprovalListBackup(List<AllotmentListItem> approvalListBackup) {
        this.approvalListBackup = approvalListBackup;
    }

    public boolean isAreWeApproving() {
        return areWeApproving;
    }

    public void setAreWeApproving(boolean areWeApproving) {
        this.areWeApproving = areWeApproving;
    }

    public void loadAllotment(int allotmentId){

        if(allotmentListener!=null)
            allotmentListener.remove();

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        allotmentListener=db.collection(DbPaths.COLLECTION_ALLOTMENT).document(Integer.toString(allotmentId))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            Msg.show(getApplication(),e.getMessage());
                            return;
                        }

                        if(areWeApproving) //when we approve, then this document will change. This will check if we are the one who is approving
                            return;

                        if(documentSnapshot.exists()){
                            Allotment allotment=documentSnapshot.toObject(Allotment.class);
                            if(allotment.getState()!=Allotment.STATE_ALLOTTED) //this allotment has been approved somewhere else
                                currentAllotment.setValue(null);
                            else
                                currentAllotment.setValue(allotment);
                        }else
                            currentAllotment.setValue(null); //this allotment has been deleted somewhere else

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
