package com.papco.sundar.cylinderinventory.data;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

@IgnoreExtraProperties
public class Allotment {

    public static final int STATE_ALLOTTED =1;
    public static final int STATE_APPROVED=2;
    public static final int STATE_PICKED_UP=3;
    public static final int STATE_READY_FOR_INVOICE=4;

    @Exclude private static final SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy, hh:mm a");

    private int id;
    private int clientId;
    private String clientName;
    private int numberOfCylinders;
    private int state;
    private long timeStamp=0;
    private HashMap<String,Integer> requirement;
    private List<Integer> cylinders=null;
    private List<String> cylinderTypes=null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public int getNumberOfCylinders() {
        return numberOfCylinders;
    }

    public void setNumberOfCylinders(int numberOfCylinders) {
        this.numberOfCylinders = numberOfCylinders;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public HashMap<String, Integer> getRequirement() {
        return requirement;
    }

    public void setRequirement(HashMap<String, Integer> requirement) {
        this.requirement = requirement;
    }

    public List<Integer> getCylinders() {
        return cylinders;
    }

    public void setCylinders(List<Integer> cylinders) {
        this.cylinders = cylinders;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<String> getCylinderTypes() {
        return cylinderTypes;
    }

    public void setCylinderTypes(List<String> cylinderTypes) {
        this.cylinderTypes = cylinderTypes;
    }

    @Exclude
    public String getStringId(){

        return Integer.toString(id);
    }

    @Exclude
    public String getStringTimeStamp(){

        return format.format(timeStamp);
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
