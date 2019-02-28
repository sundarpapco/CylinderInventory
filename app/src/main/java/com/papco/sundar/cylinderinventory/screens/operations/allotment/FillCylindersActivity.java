package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;

public class FillCylindersActivity extends OperationOutwardActivity {

    public static void start(Context context,@NonNull Allotment allotment){

        Bundle args=new Bundle();
        args.putInt("allotmentId",allotment.getId());
        args.putInt("destinationId",allotment.getClientId());
        args.putString("destinationName",allotment.getClientName());
        args.putInt("allotmentCylinderCount",allotment.getNumberOfCylinders());

        Intent intent=new Intent(context,FillCylindersActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolBarSubTitle();
    }

    private void setupToolBarSubTitle() {

        ActionBar actionBar=getSupportActionBar();
        actionBar.setSubtitle(getAllottedNumberOfCylinders()+" Cylinders");
    }

    @Override
    protected String getActivityTitle() {
        return "Add cylinders for allotment";
    }

    @Override
    public boolean canAddMoreCylinderNumbers() {

        return getNumberOfCylindersAdded()< getAllottedNumberOfCylinders();
    }

    protected int getAllottedNumberOfCylinders(){

        Bundle args=getIntent().getExtras();

        if(args==null)
            return 0;
        else
            return args.getInt("allotmentCylinderCount",0);

    }

    protected Allotment getAllotment(){

        Bundle args=getIntent().getExtras();

        if(args==null)
            return null;

        Allotment allotment=new Allotment();

        allotment.setId(args.getInt("allotmentId",-1));
        allotment.setClientId(args.getInt("destinationId",-1));
        allotment.setClientName(args.getString("destinationName",""));
        allotment.setNumberOfCylinders(args.getInt("allotmentCylinderCount",0));
        allotment.setCylinders(null);

        return allotment;
    }
}
