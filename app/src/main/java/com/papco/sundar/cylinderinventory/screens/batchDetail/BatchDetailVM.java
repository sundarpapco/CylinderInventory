package com.papco.sundar.cylinderinventory.screens.batchDetail;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class BatchDetailVM extends AndroidViewModel {

    private MutableLiveData<List<Integer>> cylinderNumbers;

    public BatchDetailVM(@NonNull Application application) {
        super(application);
        cylinderNumbers=new MutableLiveData<>();
    }

    public MutableLiveData<List<Integer>> getCylinderNumbers() {

        return cylinderNumbers;
    }

    public void loadCylinders(String batchNumber){

        Log.d("SUNDAR", "loadCylinders: "+batchNumber);
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        db.collection(DbPaths.COLLECTION_BATCHES).document(batchNumber)
                .get().addOnCompleteListener(task -> {

                    if(task.isSuccessful()){
                        Batch result=task.getResult().toObject(Batch.class);
                        cylinderNumbers.setValue(result.getCylinders());
                    }else{
                        cylinderNumbers.setValue(null);
                    }

                });

    }
}
