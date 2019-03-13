package com.papco.sundar.cylinderinventory.screens.operations.outward.refill;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.refills.RefillsActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;

public class SelectRefillStationActivity extends RefillsActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideFab();
    }

    @Override
    public String getActivityTitle() {
        return "Select Refill station";
    }

    @Override
    public void onRecyclerItemClicked(Destination item, int position) {

        OperationOutwardActivity.start(this,item,RefillOperationActivity.class);
        finish();
    }
}
