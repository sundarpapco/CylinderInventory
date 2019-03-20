package com.papco.sundar.cylinderinventory.data;

import android.text.SpannableString;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Destination {

    public static final int TYPE_WAREHOUSE = 1;
    public static final int TYPE_GRAVEYARD = 2;
    public static final int TYPE_CLIENT = 3;
    public static final int TYPE_REFILL_STATION = 4;
    public static final int TYPE_REPAIR_STATION = 5;


    private int id = 0;
    private String name = "";
    private int cylinderCount = 0;
    private int destType = TYPE_CLIENT;
    private boolean editable=true;

    @Exclude
    public SpannableString highlightedName;

    public int getId() {
        return id;
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

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Exclude
    public String getStringId() {
        return Integer.toString(id);
    }

    @Exclude
    public String getStringDestType() {

        switch (destType) {

            case Destination.TYPE_CLIENT:
                return "Client";

            case Destination.TYPE_GRAVEYARD:
                return "Graveyard";

            case Destination.TYPE_REFILL_STATION:
                return "Refill Station";

            case Destination.TYPE_REPAIR_STATION:
                return "Repair Station";

            case Destination.TYPE_WAREHOUSE:
                return "Warehouse";

        }

        return "Destination";

    }
}
