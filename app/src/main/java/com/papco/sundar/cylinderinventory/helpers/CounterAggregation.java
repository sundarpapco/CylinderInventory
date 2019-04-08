package com.papco.sundar.cylinderinventory.helpers;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.data.Aggregation;

public class CounterAggregation {

    private DocumentReference counterRef;
    private DocumentSnapshot counterDoc;
    private Aggregation counter;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    public CounterAggregation(String aggPath){
        counterRef=db.document(aggPath);
    }

    public void initializeWith(Transaction transaction) throws FirebaseFirestoreException{

        if(counter!=null)
            throw new FirebaseFirestoreException("Cannot initialize counter twice",
                    FirebaseFirestoreException.Code.CANCELLED);

        counterDoc =transaction.get(counterRef);

        if(counterDoc.exists()){
            counter=counterDoc.toObject(Aggregation.class);
        }else{
            counter=new Aggregation();
            counter.setCount(0);
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
