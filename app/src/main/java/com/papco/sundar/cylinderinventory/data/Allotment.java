package com.papco.sundar.cylinderinventory.data;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.List;

@IgnoreExtraProperties
public class Allotment {

    public static final int STATE_ALLOTTED =1;
    public static final int STATE_PICKED_UP=2;
    public static final int STATE_READY_FOR_INVOICE=3;
    @Exclude private static final SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy, hh:mm a");

    private int id;
    private int clientId;
    private String clientName;
    private int numberOfCylinders;
    private int state;
    private long timeStamp=0;
    private List<Integer> cylinders;

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

    @Exclude
    public String getStringId(){

        return Integer.toString(id);
    }

    @Exclude
    public String getStringTimeStamp(){

        return format.format(timeStamp);
    }

}
