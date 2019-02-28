package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.data.AllotmentStates;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.screens.operations.outward.Invoice.InvoiceOperationActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllotmentActivity extends AppCompatActivity implements RecyclerListener<Allotment> {


    private RecyclerView recyclerView;
    private AllotmentAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allotment);
        linkViews();
        initViews();
        setupToolBar();

    }

    private void linkViews() {

        recyclerView=findViewById(R.id.allotment_recycler);
    }

    private void initViews() {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new AllotmentAdapter(this,this);
        recyclerView.addItemDecoration(new SpacingDecoration(this,SpacingDecoration.VERTICAL,0,16,26));
        recyclerView.setAdapter(adapter);
        adapter.setData(getDummyData());

        FloatingActionButton fab=findViewById(R.id.allotment_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewAllotment();
            }
        });


    }


    private void setupToolBar() {

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Allotments");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return false;

    }

    @Override
    public void onRecyclerItemClicked(Allotment item, int position) {

        if(item.getState()==AllotmentStates.STATE_READY_FOR_INVOICE){

            InvoiceOperationActivity.start(this,item);

        }

    }

    @Override
    public void onRecyclerItemLongClicked(final Allotment item, int position, View view) {

        if(item.getState()!=AllotmentStates.STATE_ALLOTED)
            return;

        PopupMenu popupMenu=new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_allotment,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId()==R.id.mnu_allotment_pickup){

                    onPickupAllotment(item);
                    return true;
                }

                return false;
            }
        });
        popupMenu.show();

    }

    protected void onCreateNewAllotment(){

        Intent intent=new Intent(this,SelectClientActivity.class);
        startActivity(intent);

    }

    protected void onPickupAllotment(Allotment allotment){

        // TODO: 25-02-2019 add code to pickup allotment here after validation
        FillCylindersActivity.start(this,allotment);

    }



    private List<Allotment> getDummyData(){

        List<Allotment> data=new ArrayList<>();
        Allotment item1=new Allotment();
        item1.setId(1);
        item1.setClientId(1);
        item1.setNumberOfCylinders(8);
        item1.setState(AllotmentStates.STATE_ALLOTED);
        item1.setClientName("Sri vidhya Hospitals");

        Allotment item2=new Allotment();
        item2.setId(2);
        item2.setClientId(2);
        item2.setNumberOfCylinders(4);
        item2.setState(AllotmentStates.STATE_PICKED_UP);
        item2.setClientName("Appolo hospitals");

        Allotment item3=new Allotment();
        item3.setId(3);
        item3.setClientId(3);
        item3.setNumberOfCylinders(12);
        item3.setState(AllotmentStates.STATE_READY_FOR_INVOICE);
        item3.setClientName("Malar hospitals pvt ltd");
        item3.setCylinders(Arrays.asList(23,56,78,97));

        data.add(item1);
        data.add(item2);
        data.add(item3);
        return data;

    }
}
