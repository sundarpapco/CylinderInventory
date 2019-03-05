package com.papco.sundar.cylinderinventory.screens.operations.outward.repair;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.repairs.RepairStationsActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;

public class SelectRepairStationActivity extends RepairStationsActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideFab();
    }

    @Override
    public String getActivityTitle() {
        return "Select repair stations";
    }

    @Override
    public void onRecyclerItemClicked(Destination item, int position) {
        OperationOutwardActivity.start(this,item, RepairOperationActivity.class);
        finish();
    }
}
