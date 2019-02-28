package com.papco.sundar.cylinderinventory.screens.operations.outward.refill;

import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;

public class RefillOperationActivity extends OperationOutwardActivity {

    @Override
    protected String getActivityTitle() {
        return "Send for refilling";
    }

    @Override
    protected String getDestinationHint() {
        return "Refill station";
    }

    @Override
    protected void onSaveOperation() {
        showMessage("send refill to"+getDestinationName());
    }
}
