package com.papco.sundar.cylinderinventory.screens.destinations.clients;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.DestinationDetailActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.common.AddDestinationFragment;
import com.papco.sundar.cylinderinventory.screens.destinations.common.DestinationActivity;

public class ClientsActivity extends DestinationActivity {


    @Override
    public String getActivityTitle() {
        return "Clients";
    }

    @Override
    public void onShowNewDestinationDialog() {
        AddDestinationFragment fragment=new AddClientDialog();
        fragment.setArguments(AddDestinationFragment.getStartingArguments(false,null));
        fragment.show(getSupportFragmentManager(),"addDestination");
    }

    @Override
    protected int getDestinationType() {
        return Destination.TYPE_CLIENT;
    }

}
