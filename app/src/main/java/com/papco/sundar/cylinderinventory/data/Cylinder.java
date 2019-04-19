package com.papco.sundar.cylinderinventory.data;

import com.google.firebase.firestore.Exclude;

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
    private String cylinderTypeName;

    private int snapRefillCount;
    private int snapLocationId;
    private String snapLocationName;
    private long snapLastTransaction;
    private boolean snapIsEmpty;
    private boolean snapIsDamaged;
    private int snapDamageCount;


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

        snapRefillCount=refillCount;
        snapLocationId=locationId;
        snapLocationName=locationName;
        snapLastTransaction=-1;
        snapIsEmpty=false;
        snapIsDamaged=false;
        snapDamageCount=0;

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

    public String getCylinderTypeName() {
        return cylinderTypeName;
    }

    public void setCylinderTypeName(String cylinderTypeName) {
        this.cylinderTypeName = cylinderTypeName;
    }

    public int getSnapRefillCount() {
        return snapRefillCount;
    }

    public void setSnapRefillCount(int snapRefillCount) {
        this.snapRefillCount = snapRefillCount;
    }

    public int getSnapLocationId() {
        return snapLocationId;
    }

    public void setSnapLocationId(int snapLocationId) {
        this.snapLocationId = snapLocationId;
    }

    public String getSnapLocationName() {
        return snapLocationName;
    }

    public void setSnapLocationName(String snapLocationName) {
        this.snapLocationName = snapLocationName;
    }

    public long getSnapLastTransaction() {
        return snapLastTransaction;
    }

    public void setSnapLastTransaction(long snapLastTransaction) {
        this.snapLastTransaction = snapLastTransaction;
    }

    public boolean isSnapIsEmpty() {
        return snapIsEmpty;
    }

    public void setSnapIsEmpty(boolean snapIsEmpty) {
        this.snapIsEmpty = snapIsEmpty;
    }

    public boolean isSnapIsDamaged() {
        return snapIsDamaged;
    }

    public void setSnapIsDamaged(boolean snapIsDamaged) {
        this.snapIsDamaged = snapIsDamaged;
    }

    public int getSnapDamageCount() {
        return snapDamageCount;
    }

    public void setSnapDamageCount(int snapDamageCount) {
        this.snapDamageCount = snapDamageCount;
    }

    @Exclude
    public String getStringId(){

        return Integer.toString(cylinderNo);
    }

    @Exclude
    public String getStringPurchaseDate(){

        SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy");
        return format.format(new Date(purchaseDate));
    }

    @Exclude
    public String getStringLastTransaction(){
        SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy");
        return format.format(new Date(lastTransaction));
    }

    @Exclude
    public void takeSnapShot(){

        snapRefillCount=refillCount;
        snapLocationId=locationId;
        snapLocationName=locationName;
        snapLastTransaction=lastTransaction;
        snapIsEmpty=isEmpty;
        snapIsDamaged=isDamaged;
        snapDamageCount=damageCount;

    }

    @Exclude
    public boolean hasValidSnapshot(){

        return snapLastTransaction!=-1;
    }

    @Exclude
    public void restoreSnapshot(){

        refillCount=snapRefillCount;
        locationId=snapLocationId;
        locationName=snapLocationName;
        lastTransaction=snapLastTransaction;
        isEmpty=snapIsEmpty;
        isDamaged=snapIsDamaged;
        damageCount=snapDamageCount;

        invalidateSnapshot();

    }

    @Exclude
    public void invalidateSnapshot(){

        snapLastTransaction=-1;
    }
}
