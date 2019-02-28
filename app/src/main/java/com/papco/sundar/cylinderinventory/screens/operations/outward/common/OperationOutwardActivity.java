package com.papco.sundar.cylinderinventory.screens.operations.outward.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.screens.operations.common.OperationActivity;

public class OperationOutwardActivity extends OperationActivity {

    public static void start(Context context, @NonNull Destination destination,Class<? extends OperationOutwardActivity> className){

        Bundle bundle=new Bundle();
        bundle.putInt("destinationId",destination.getId());
        bundle.putString("destinationName",destination.getName());

        Intent intent=new Intent(context, className);
        intent.putExtras(bundle);
        context.startActivity(intent);

    }

    private TextInputLayout destinationNameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        destinationNameLayout=findViewById(R.id.operation_destination_layout);
        loadDestinationName(getDestinationName());
        destinationNameLayout.setHint(getDestinationHint());

    }

    protected String getDestinationHint(){

        return "Client";

    }

    protected int getDestinationId(){

        if(getIntent().getExtras()==null)
            return -1;

        return getIntent().getExtras().getInt("destinationId",-1);
    }

    protected String getDestinationName(){

        if(getIntent().getExtras()==null)
            return "";

        return getIntent().getExtras().getString("destinationName","");
    }

}
