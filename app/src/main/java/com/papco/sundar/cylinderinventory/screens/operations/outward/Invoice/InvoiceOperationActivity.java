package com.papco.sundar.cylinderinventory.screens.operations.outward.Invoice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.AllotmentStates;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.FillCylindersActivity;
import com.papco.sundar.cylinderinventory.screens.operations.outward.common.OperationOutwardActivity;

import java.util.Arrays;

public class InvoiceOperationActivity extends OperationOutwardActivity {

    public static void start(Context context, @NonNull Allotment allotment){

        Bundle args=new Bundle();
        args.putInt("allotmentId",allotment.getId());
        args.putInt("destinationId",allotment.getClientId());
        args.putString("destinationName",allotment.getClientName());
        args.putInt("allotmentCylinderCount",allotment.getNumberOfCylinders());

        Intent intent=new Intent(context, InvoiceOperationActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fab.hide();
        btnSave.setText("CLOSE INVOICE");
        setData(getAllotment().getCylinders());
    }

    @Override
    protected String getActivityTitle() {
        return "Invoice";
    }

    @Override
    protected void onSaveOperation() {
        Msg.show(this,"Close the invoice here");
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
        allotment.setState(AllotmentStates.STATE_READY_FOR_INVOICE);
        //simulating a dummy list instead of reading from parcel.
        //becasue we will be getting the cylinder list from internet and not through intent
        allotment.setCylinders(Arrays.asList(23,45,78,98,34));

        return allotment;
    }
}
