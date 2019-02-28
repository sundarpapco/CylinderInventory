package com.papco.sundar.cylinderinventory.screens.destinations.repairs;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.common.AddDestinationFragment;

public class AddRepairsDialog extends AddDestinationFragment {

    @Override
    public String getTitle() {
        if(isEditingMode() && getDestination()!=null)
            return "Repair station name";
        else
            return "New repair station name";
    }

    @Override
    protected int getDestinationType() {
        return Destination.TYPE_REPAIR_STATION;
    }
}
