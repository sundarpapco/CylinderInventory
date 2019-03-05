package com.papco.sundar.cylinderinventory.data;

import android.text.SpannableString;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Destination {

    public static final int TYPE_WAREHOUSE=1;
    public static final int TYPE_GRAVEYARD=2;
    public static final int TYPE_CLIENT=3;
    public static final int TYPE_REFILL_STATION=4;
    public static final int TYPE_REPAIR_STATION=5;


    private int id=0;
    private String name="";
    private int cylinderCount=0;
    private int destType=TYPE_CLIENT;
    @Exclude
    public SpannableString highlightedName;

    public int getId() {
        return id;
    }

    @Exclude
    public String getStringId(){
        return Integer.toString(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCylinderCount() {
        return cylinderCount;
    }

    public void setCylinderCount(int cylinderCount) {
        this.cylinderCount = cylinderCount;
    }

    public int getDestType() {
        return destType;
    }

    public void setDestType(int destType) {
        this.destType = destType;
    }
}
