package com.papco.sundar.cylinderinventory.helpers;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.papco.sundar.cylinderinventory.data.Cylinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public  final class CylinderUtils {

    private CylinderUtils(){

    }

    public static List<List<Cylinder>> getMasterList(List<DocumentSnapshot> cylinderSnapshots)  throws FirebaseFirestoreException{

        List<Cylinder> cylinderList=new ArrayList<>();
        for(DocumentSnapshot documentSnapshot:cylinderSnapshots){
            if(!documentSnapshot.exists())
                throw new FirebaseFirestoreException("Invalid cylinders found",FirebaseFirestoreException.Code.CANCELLED);

            cylinderList.add(documentSnapshot.toObject(Cylinder.class));
        }

        Collections.sort(cylinderList,new CylinderTypeComparator());

        List<Cylinder> monoList=new ArrayList<>();
        List<List<Cylinder>> masterList=new ArrayList<>();
        String currentCylinderType="";
        String cylinderName;

        for(Cylinder cylinder:cylinderList){

            cylinderName=cylinder.getCylinderTypeName();
            if(currentCylinderType.equals(cylinderName)){
                monoList.add(cylinder);
            }else{
                if(monoList.size()>0)
                    masterList.add(monoList);
                monoList=new ArrayList<>();
                monoList.add(cylinder);
                currentCylinderType=cylinderName;
            }

        }

        if(monoList.size()>0)
            masterList.add(monoList);

        return masterList;
    }

    public static class CylinderTypeComparator implements Comparator<Cylinder>{


        @Override
        public int compare(Cylinder cylinder1, Cylinder cylinder2) {

            if(cylinder1==null)
                return -1;

            if(cylinder2==null)
                return 1;

            return cylinder1.getCylinderTypeName().compareTo(cylinder2.getCylinderTypeName());
        }
    }
}
