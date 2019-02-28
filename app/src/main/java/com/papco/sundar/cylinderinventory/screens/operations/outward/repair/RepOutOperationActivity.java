package com.papco.sundar.cylinderinventory.screens.operations.outward.repair;

import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;

public class RepOutOperationActivity extends OperationOutwardActivity {

    @Override
    protected String getActivityTitle() {
        return "Send for repair";
    }

    @Override
    protected String getDestinationHint() {
        return "Repair station";
    }

    @Override
    protected void onSaveOperation() {
        showMessage("do the repair operation here");
    }
}


