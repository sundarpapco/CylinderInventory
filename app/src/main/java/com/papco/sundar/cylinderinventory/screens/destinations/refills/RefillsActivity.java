package com.papco.sundar.cylinderinventory.screens.destinations.refills;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.common.AddDestinationFragment;
import com.papco.sundar.cylinderinventory.screens.destinations.common.DestinationActivity;

public class RefillsActivity extends DestinationActivity {


    @Override
    public String getActivityTitle() {
        return "Refill stations";
    }

    @Override
    public void onShowEditDestinationDialog(Destination destination) {
        /*AddDestinationFragment fragment=new AddRefillsDialog();
        fragment.setArguments(AddDestinationFragment.getStartingArguments(true,destination));
        fragment.show(getSupportFragmentManager(),"addRefillStation");*/
    }

    @Override
    public void onShowNewDestinationDialog() {
        AddDestinationFragment fragment=new AddRefillsDialog();
        fragment.setArguments(AddDestinationFragment.getStartingArguments(false,null));
        fragment.show(getSupportFragmentManager(),"editRefillStation");
    }

    @Override
    protected int getDestinationType() {
        return Destination.TYPE_REFILL_STATION;
    }
}
