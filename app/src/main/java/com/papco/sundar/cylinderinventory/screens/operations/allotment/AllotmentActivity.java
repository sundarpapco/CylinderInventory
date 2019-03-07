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
import android.widget.ProgressBar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.screens.operations.outward.Invoice.InvoiceOperationActivity;

import java.util.ArrayList;
import java.util.List;

public class AllotmentActivity extends ConnectivityActivity implements RecyclerListener<Allotment> {


    private RecyclerView recyclerView;
    private AllotmentAdapter adapter;
    private ListenerRegistration listenerRegistration;
    private ProgressBar progressBar;


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
        progressBar=findViewById(R.id.allotment_progress_bar);
    }

    private void initViews() {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new AllotmentAdapter(this,this);
        recyclerView.addItemDecoration(new SpacingDecoration(this,SpacingDecoration.VERTICAL,0,16,26));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab=findViewById(R.id.allotment_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewAllotment();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAllotments();
    }

    private void setupToolBar() {

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Allotments");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

    }

    private void loadAllotments(){


        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        if(listenerRegistration!=null)
            listenerRegistration.remove();

        listenerRegistration=db.collection(DbPaths.COLLECTION_ALLOTMENT)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        progressBar.setVisibility(View.INVISIBLE);
                        if(e!=null){
                            Msg.show(AllotmentActivity.this,"Error connecting to server. Please check internet connection");
                            return;
                        }

                        List<DocumentSnapshot> documentSnapshots=querySnapshot.getDocuments();
                        List<Allotment> allotments=new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot:documentSnapshots){
                            allotments.add(documentSnapshot.toObject(Allotment.class));
                        }
                        adapter.setData(allotments);
                    }
                });


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

        if(item.getState()==Allotment.STATE_READY_FOR_INVOICE){

            InvoiceOperationActivity.start(this,item);

        }

        if(item.getState()==Allotment.STATE_ALLOTTED){

            pickupAllotment(item);

        }

    }

    @Override
    public void onRecyclerItemLongClicked(final Allotment item, int position, View view) {

        if(item.getState()!=Allotment.STATE_PICKED_UP)
            return;

        PopupMenu popupMenu=new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_allotment,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId()==R.id.mnu_allotment_pickup){

                    pickupAllotment(item);
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

    protected void pickupAllotment(Allotment allotment){

        FillCylindersActivity.start(this,allotment);

    }

}
