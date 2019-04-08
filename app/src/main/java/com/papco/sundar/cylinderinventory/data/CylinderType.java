package com.papco.sundar.cylinderinventory.data;

import android.text.SpannableString;

import com.google.firebase.firestore.Exclude;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CylinderType {

    @Exclude
    public static final String TYPE_GLOBAL="Global";

    //This class does not have id because, the name of the type is the id and the
    //document name in the firestore database
    private String name;
    private int noOfCylinders;
    private boolean editable;

    @Exclude
    public SpannableString highlightedName;

    public CylinderType(){

        name="Default";
        noOfCylinders=0;
        editable=true;

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoOfCylinders() {
        return noOfCylinders;
    }

    public void setNoOfCylinders(int noOfCylinders) {
        this.noOfCylinders = noOfCylinders;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Exclude
    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Exclude
    @Override
    public boolean equals(@Nullable Object obj) {

        return name.equals(((CylinderType)obj).name);
    }

    @Exclude
    public String getStringNoOfCylinders(){
        return Integer.toString(noOfCylinders);
    }


}
