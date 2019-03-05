package com.papco.sundar.cylinderinventory.data;

import com.google.common.base.CaseFormat;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Batch {

    public static final int TYPE_FCI=1;
    public static final int TYPE_REFILL=2;
    public static final int TYPE_INVOICE=3;
    public static final int TYPE_ECR=4;
    public static final int TYPE_REPAIR=5;
    public static final int TYPE_RCI=6;
    @Exclude private static final SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy, hh:mm a");


    private long id;
    private long timestamp;
    private int noOfCylinders;
    private int destinationId;
    private String destinationName;
    private int type;
    private List<Integer> cylinders;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getNoOfCylinders() {
        return noOfCylinders;
    }

    public void setNoOfCylinders(int noOfCylinders) {
        this.noOfCylinders = noOfCylinders;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getCylinders() {
        return cylinders;
    }

    public void setCylinders(List<Integer> cylinders) {
        this.cylinders = cylinders;
    }


    @Exclude
    public String getStringId(){
        return Long.toString(id);
    }

    @Exclude
    public String getStringTimeStamp(){
        return format.format(timestamp);
    }

    @Exclude
    public void setCurrentTime(){

        timestamp=Calendar.getInstance().getTimeInMillis();
    }

    @Exclude
    public String getBatchNumber(){

        String batchNumber="";
        switch (type){

            case TYPE_ECR:
                batchNumber="ecr-";
                break;

            case TYPE_FCI:
                batchNumber="fci-";
                break;

            case TYPE_INVOICE:
                batchNumber="inv-";
                break;

            case TYPE_REFILL:
                batchNumber="ref-";
                break;

            case TYPE_REPAIR:
                batchNumber="rci-";
                break;

            case TYPE_RCI:
                batchNumber="rep-";
                break;

        }

        batchNumber=batchNumber+Long.toString(id);
        return batchNumber;
    }
}
