package com.papco.sundar.cylinderinventory.screens.destinations.repairs;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.common.AddDestinationFragment;
import com.papco.sundar.cylinderinventory.screens.destinations.common.DestinationActivity;

public class RepairStationsActivity extends DestinationActivity {


    @Override
    public String getActivityTitle() {
        return "Repair stations";
    }

    @Override
    public void onShowEditDestinationDialog(Destination destination) {
        /*AddDestinationFragment fragment=new AddRepairsDialog();
        fragment.setArguments(AddDestinationFragment.getStartingArguments(true,destination));
        fragment.show(getSupportFragmentManager(),"addRefillStation");*/
    }

    @Override
    public void onShowNewDestinationDialog() {
        AddDestinationFragment fragment=new AddRepairsDialog();
        fragment.setArguments(AddDestinationFragment.getStartingArguments(false,null));
        fragment.show(getSupportFragmentManager(),"editRefillStation");
    }

    @Override
    protected int getDestinationType() {
        return Destination.TYPE_REPAIR_STATION;
    }
}
