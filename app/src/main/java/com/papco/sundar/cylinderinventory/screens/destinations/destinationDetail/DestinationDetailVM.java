package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail;

import android.app.Application;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.cylinderList.DestinationCylindersData;
import com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.historyList.DestinationHistoryData;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class DestinationDetailVM extends AndroidViewModel {

    private DestinationCylindersData cylindersData;
    private DestinationHistoryData historyData;
    private MutableLiveData<Destination> loadedDestination;
    private ListenerRegistration destinationDocumentListener;

    public DestinationDetailVM(@NonNull Application application) {
        super(application);
    }


    public DestinationCylindersData getCylindersData(int destinationId) {

        if(cylindersData==null)
            cylindersData=new DestinationCylindersData(destinationId);
        else if(cylindersData.getDestinationId()!=destinationId)
            cylindersData=new DestinationCylindersData(destinationId);

        return cylindersData;
    }

    public DestinationHistoryData getHistoryData(int destinationId) {

        if(historyData==null)
            historyData=new DestinationHistoryData(destinationId);
        else if(historyData.getDestinationId()!=destinationId)
            historyData=new DestinationHistoryData(destinationId);

        return historyData;
    }



    public MutableLiveData<Destination> getLoadedDestination(int destinationId) {

        if(loadedDestination==null){
            loadedDestination=new MutableLiveData<>();
            loadDestination(destinationId);
        }

        return loadedDestination;
    }

    private void loadDestination(int destinationId) {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        String docId=Integer.toString(destinationId);

        destinationDocumentListener=db.collection(DbPaths.COLLECTION_DESTINATIONS)
                .document(docId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            Msg.show(getApplication(),"Error updating correct values. Please connect to internet");
                            return;
                        }

                        if(documentSnapshot.exists())
                            loadedDestination.setValue(documentSnapshot.toObject(Destination.class));
                        else
                            loadedDestination.setValue(null);
                    }
                });

    }


    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d("SUNDAR", "onCleared: ");
        if(destinationDocumentListener!=null)
            destinationDocumentListener.remove();
    }
}
