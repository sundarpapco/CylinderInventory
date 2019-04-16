package com.papco.sundar.cylinderinventory.helpers;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.data.Aggregation;

public class CounterAggregation {

    private DocumentReference counterRef;
    private Aggregation counter;
    private int initialValue;

    public CounterAggregation(String aggPath){
        this(aggPath,0);
    }

    public CounterAggregation(String aggPath,int initialValue){
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        counterRef=db.document(aggPath);
        this.initialValue=initialValue;
    }

    public void initializeWith(Transaction transaction) throws FirebaseFirestoreException{

        if(counter!=null)
            throw new FirebaseFirestoreException("Cannot initialize counter twice",
                    FirebaseFirestoreException.Code.CANCELLED);

        DocumentSnapshot counterDoc =transaction.get(counterRef);

        if(counterDoc.exists()){
            counter=counterDoc.toObject(Aggregation.class);
        }else{
            counter=new Aggregation();
            counter.setCount(initialValue);
        }
    }

    public void writeWith(Transaction transaction) throws FirebaseFirestoreException{

        if(counter==null)
            throw new FirebaseFirestoreException("Cannot write without initialization",
                    FirebaseFirestoreException.Code.CANCELLED);

        counter.setCount(counter.getCount()+1);
        transaction.set(counterRef,counter);

    }

    public int getNextNumber(){

        if(counter==null)
            return -1;
        else
            return counter.getCount()+1;
    }

}
