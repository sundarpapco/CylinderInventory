package com.papco.sundar.cylinderinventory.logic.Transactions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Destination;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DeleteDestinationTransaction extends BaseTransaction {

    private Destination destination;

    public DeleteDestinationTransaction(@NonNull Destination destination){
        this.destination=destination;

    }

    @Nullable
    @Override
    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        Destination loadedDestination;
        DocumentReference destinationRef=db.collection(DbPaths.COLLECTION_DESTINATIONS).document(destination.getStringId());

        DocumentSnapshot destinationDoc=transaction.get(destinationRef);

        //Checking the client. The client may be deleted by some other parallel transaction. So
        // we should check for the existence of the the client and if exist, then check whether we
        //can edit this destination

        if(!destinationDoc.exists())
            throw new FirebaseFirestoreException("Invalid "+destination.getStringDestType(),
                    FirebaseFirestoreException.Code.CANCELLED);
        else
            loadedDestination=destinationDoc.toObject(Destination.class);

        if(!loadedDestination.isEditable())
            throw new FirebaseFirestoreException("This "+destination.getStringDestType() + " not deletable anymore",
                    FirebaseFirestoreException.Code.CANCELLED);

        transaction.delete(destinationRef);

        return null;
    }

}
