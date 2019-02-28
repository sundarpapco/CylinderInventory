package com.papco.sundar.cylinderinventory.data;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Cylinder {

    private int cylinderNo;
    private String supplier;
    private long purchaseDate;
    private int refillCount;
    private int locationId;
    private String locationName;
    private long lastTransaction;
    private boolean isEmpty;
    private boolean isDamaged;
    private String remarks;
    private int damageCount;

    public Cylinder(){

        cylinderNo=-1;
        supplier="Anonymous";
        purchaseDate=0;
        refillCount=0;
        locationId=Destination.TYPE_WAREHOUSE;
        lastTransaction=0;
        isDamaged=false;
        isEmpty=false;
        remarks="";
        locationName="WAREHOUSE";
        damageCount=0;

    }

    public int getCylinderNo() {
        return cylinderNo;
    }

    public void setCylinderNo(int cylinderNo) {
        this.cylinderNo = cylinderNo;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public long getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(long purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public int getRefillCount() {
        return refillCount;
    }

    public void setRefillCount(int refillCount) {
        this.refillCount = refillCount;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public long getLastTransaction() {
        return lastTransaction;
    }

    public void setLastTransaction(long lastTransaction) {
        this.lastTransaction = lastTransaction;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public void setDamaged(boolean damaged) {
        isDamaged = damaged;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getDamageCount() {
        return damageCount;
    }

    public void setDamageCount(int damageCount) {
        this.damageCount = damageCount;
    }

    public String getStringId(){

        return Integer.toString(cylinderNo);
    }

    public String getStringPurchaseDate(){

        SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy");
        return format.format(new Date(purchaseDate));
    }
}
