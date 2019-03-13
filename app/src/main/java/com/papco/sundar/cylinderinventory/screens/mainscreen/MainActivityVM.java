package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.app.Application;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;

import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


public class MainActivityVM extends AndroidViewModel {

    private MutableLiveData<QuerySnapshot> firstPage;
    private MutableLiveData<List<DocumentSnapshot>> loadedPage;
    private ListenerRegistration firstPageListener;
    private FirebaseFirestore db;

    public MainActivityVM(@NonNull Application application) {
        super(application);

        firstPage=new MutableLiveData<>();
        loadedPage=new MutableLiveData<>();
        db=FirebaseFirestore.getInstance();

    }

    public void loadFirstPage(){

        if(firstPageListener!=null)
            firstPageListener.remove();

        firstPageListener=db.collection(DbPaths.COLLECTION_BATCHES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(BatchFeedScrollListener.PAGE_SIZE)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {

                        if(e!=null) {
                            Msg.show(getApplication(),"Couldn't refresh feed");
                            return;
                        }

                        firstPage.setValue(querySnapshot);
                    }
                });
    }

    public void loadNextPage(DocumentSnapshot lastSnapshot){

        if(lastSnapshot==null)
            return;

        db.collection(DbPaths.COLLECTION_BATCHES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(BatchFeedScrollListener.PAGE_SIZE)
                .startAfter(lastSnapshot)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(!task.isSuccessful()){
                            Msg.show(getApplication(),"Couldn't refresh feed");
                            return;
                        }

                        loadedPage.setValue(task.getResult().getDocuments());

                    }
                });

    }

    public MutableLiveData<QuerySnapshot> getFirstPage() {
        return firstPage;
    }

    public MutableLiveData<List<DocumentSnapshot>> getLoadedPage() {
        return loadedPage;
    }
}
