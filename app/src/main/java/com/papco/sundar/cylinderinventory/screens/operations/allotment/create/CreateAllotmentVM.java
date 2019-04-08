package com.papco.sundar.cylinderinventory.screens.operations.allotment.create;

import android.app.Application;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.CylinderType;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentListItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class CreateAllotmentVM extends AndroidViewModel {

    private MutableLiveData<List<CylinderType>> cylinderTypes;
    private List<AllotmentListItem> allotmentListBackup;
    private int spinnerSelectionPositionBackup;

    public CreateAllotmentVM(@NonNull Application application) {
        super(application);
        cylinderTypes=new MutableLiveData<>();
        prepareCylinderTypesAdapter();
    }

    public MutableLiveData<List<CylinderType>> getCylinderTypes() {
        return cylinderTypes;
    }

    public List<AllotmentListItem> getAllotmentListBackup() {
        return allotmentListBackup;
    }

    public void setAllotmentListBackup(List<AllotmentListItem> allotmentListBackup) {
        this.allotmentListBackup = allotmentListBackup;
    }

    public int getSpinnerSelectionPositionBackup() {
        return spinnerSelectionPositionBackup;
    }

    public void setSpinnerSelectionPositionBackup(int spinnerSelectionPositionBackup) {
        this.spinnerSelectionPositionBackup = spinnerSelectionPositionBackup;
    }

    private void prepareCylinderTypesAdapter() {

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        db.collection(DbPaths.COLLECTION_CYLINDER_TYPES)
                .orderBy("name")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(!task.isSuccessful()){
                            Msg.show(getApplication(),"Error connecting to server. Please check internet connection");
                            return;
                        }

                        List<CylinderType> types=new ArrayList<>();

                        if(task.getResult().size()==0){
                            types.add(getDefaultCylinderStyleItem());
                        }else{
                            CylinderType cylinderType;
                            for(DocumentSnapshot documentSnapshot:task.getResult()){
                                cylinderType=documentSnapshot.toObject(CylinderType.class);
                                if(cylinderType.getNoOfCylinders()>0)
                                    types.add(cylinderType);
                            }
                        }

                        cylinderTypes.setValue(types);

                    }
                });

    }

    private CylinderType getDefaultCylinderStyleItem(){

        CylinderType defaultType=new CylinderType();
        defaultType.setName("Default");
        defaultType.setNoOfCylinders(1);
        return defaultType;
    }
}
