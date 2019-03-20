package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.cylinderList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Cylinder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class DestinationCylindersData {

    private MutableLiveData<List<Cylinder>> cylinders;
    private int destinationId;


    public DestinationCylindersData(int destinationId){

        cylinders=new MutableLiveData<>();
        this.destinationId=destinationId;
        loadData();
    }

    public int getDestinationId() {
        return destinationId;
    }

    public MutableLiveData<List<Cylinder>> getCylinders() {
        return cylinders;
    }

    private void loadData() {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        db.collection(DbPaths.COLLECTION_CYLINDERS)
                .whereEqualTo("locationId",destinationId)
                .orderBy("lastTransaction", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){
                            cylinders.setValue(createCylinderList(task.getResult().getDocuments()));
                        }else{
                            cylinders.setValue(null);
                        }

                    }
                });

    }

    private List<Cylinder> createCylinderList(List<DocumentSnapshot> snapshots){

        List<Cylinder> result=new ArrayList<>();
        for(DocumentSnapshot snapshot:snapshots){
            result.add(snapshot.toObject(Cylinder.class));
        }

        return result;

    }


}
