package com.papco.sundar.cylinderinventory.screens.destinations.refills;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.common.AddDestinationFragment;

public class AddRefillsDialog extends AddDestinationFragment {

    @Override
    public String getTitle() {
        if(isEditingMode())
            return "Refill station name";
        else
            return "New refill station name";
    }

    @Override
    protected int getDestinationType() {
        return Destination.TYPE_REFILL_STATION;
    }
}
