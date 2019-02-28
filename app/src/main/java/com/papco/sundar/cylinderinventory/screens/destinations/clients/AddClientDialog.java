package com.papco.sundar.cylinderinventory.screens.destinations.clients;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.common.AddDestinationFragment;

public class AddClientDialog extends AddDestinationFragment {

    @Override
    public String getTitle() {

        if(isEditingMode())
            return "Client name";
        else
            return "New client name";
    }

    @Override
    protected int getDestinationType() {
        return Destination.TYPE_CLIENT;
    }
}
