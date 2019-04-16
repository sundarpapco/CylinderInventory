package com.papco.sundar.cylinderinventory.data;

import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Batch {

    public static final int TYPE_FCI = 1;
    public static final int TYPE_REFILL = 2;
    public static final int TYPE_INVOICE = 3;
    public static final int TYPE_ECR = 4;
    public static final int TYPE_REPAIR = 5;
    public static final int TYPE_RCI = 6;
    @Exclude
    private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy, hh:mm a");
    @Exclude
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


    private long id;
    private long timestamp;
    private int noOfCylinders;
    private int destinationId;
    private String destinationName;
    private int type;
    private List<Integer> cylinders;
    private List<String> cylinderTypes;


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

    public List<String> getCylinderTypes() {
        return cylinderTypes;
    }

    public void setCylinderTypes(List<String> cylinderTypes) {
        this.cylinderTypes = cylinderTypes;
    }

    @Exclude
    public String getStringId() {
        return Long.toString(id);
    }

    @Exclude
    public String getStringTimeStamp() {
        return format.format(timestamp);
    }

    @Exclude
    public String getStringDate() {
        return dateFormat.format(timestamp);
    }

    @Exclude
    public String getBatchNumber() {

        String batchNumber = "";
        switch (type) {

            case TYPE_ECR:
                batchNumber = "ecr-";
                break;

            case TYPE_FCI:
                batchNumber = "fci-";
                break;

            case TYPE_INVOICE:
                batchNumber = "inv-";
                break;

            case TYPE_REFILL:
                batchNumber = "ref-";
                break;

            case TYPE_REPAIR:
                batchNumber = "rep-";
                break;

            case TYPE_RCI:
                batchNumber = "rci-";
                break;

        }

        batchNumber = batchNumber + Long.toString(id);
        return batchNumber;
    }

    @Exclude
    public String getStringBatchType() {

        String batchType = "";
        switch (type) {

            case TYPE_ECR:
                batchType = "Empty cylinder return";
                break;

            case TYPE_FCI:
                batchType = "Full cylinder inward";
                break;

            case TYPE_INVOICE:
                batchType = "Invoice";
                break;

            case TYPE_REFILL:
                batchType = "Sent for refill";
                break;

            case TYPE_REPAIR:
                batchType = "sent for repair";
                break;

            case TYPE_RCI:
                batchType = "Repaired cylinder inward";
                break;

        }

        return batchType;
    }

    @Exclude
    public List<List<Cylinder>> getTypedMasterList() {

        if (cylinders == null || cylinderTypes == null)
            return null;

        if (cylinders.size() == 0 || cylinderTypes.size() == 0)
            return null;

        List<List<Cylinder>> masterList = new ArrayList<>();
        List<Cylinder> monoList = new ArrayList<>();
        String currentCylinderType = "";
        String cylinderType;
        Cylinder cylinder;

        for (int i = 0; i < cylinders.size(); i++) {

            cylinderType = cylinderTypes.get(i);
            if (!currentCylinderType.equals(cylinderType)) {
                if (monoList.size() > 0)
                    masterList.add(monoList);
                monoList = new ArrayList<>();
                currentCylinderType=cylinderType;
            }

            cylinder = new Cylinder();
            cylinder.setCylinderNo(cylinders.get(i));
            cylinder.setCylinderTypeName(cylinderType);
            monoList.add(cylinder);

        }

        if(monoList.size()>0)
            masterList.add(monoList);

        if(masterList.size()>0)
            return masterList;
        else
            return null;
    }

    @Exclude
    public void setCylindersAndTypes(List<List<Cylinder>> masterList){

        if(masterList==null || masterList.size()==0){
            cylinders=null;
            cylinderTypes=null;
        }

        cylinders=new ArrayList<>();
        cylinderTypes=new ArrayList<>();

        for(List<Cylinder> monoList:masterList){
            for(Cylinder cylinder:monoList){
                cylinders.add(cylinder.getCylinderNo());
                cylinderTypes.add(cylinder.getCylinderTypeName());
            }
        }

    }


}
