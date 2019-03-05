package com.papco.sundar.cylinderinventory.data;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Aggregation {

    public static final int TYPE_CYLINDERS=1;
    public static final int TYPE_DESTINATION=2;
    public static final int TYPE_ALLOTMENT=3;
    public static final int TYPE_BATCH=4;

    private int count;
    private int type;

    public Aggregation(){
        count=0;
        type=TYPE_CYLINDERS;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
