package com.papco.sundar.cylinderinventory.common.BaseClasses;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.List;

public abstract class BaseTransaction implements Transaction.Function<Void> {

    private List<DocumentSnapshot> prefetchedDocuments=null;

    public void setPrefetchedDocuments(List<DocumentSnapshot> documents){
        this.prefetchedDocuments=documents;
    }

    protected List<DocumentSnapshot> getPrefetchedDocuments(){
        return prefetchedDocuments;
    }

}
