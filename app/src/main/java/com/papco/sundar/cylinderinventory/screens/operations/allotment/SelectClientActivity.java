package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.destinations.clients.ClientsActivity;

public class SelectClientActivity extends ClientsActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideFab();
    }

    @Override
    public String getActivityTitle() {
        return "Select client";
    }

    @Override
    public void onRecyclerItemClicked(Destination item, int position) {

        CreateAllotmentActivity.start(this,item);
        finish();

    }
}
