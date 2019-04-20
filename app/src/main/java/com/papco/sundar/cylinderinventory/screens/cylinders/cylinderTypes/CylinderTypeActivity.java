package com.papco.sundar.cylinderinventory.screens.cylinders.cylinderTypes;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.DividerDecoration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.CylinderType;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CylinderTypeActivity extends ConnectivityActivity implements CylinderTypesAdapter.CylinderTypesListListener {


    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private FloatingActionButton fab;
    private CylinderTypesAdapter adapter;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.destination_list);
        linkViews();
        setupToolBar();
        initViews();
        loadCylinderTypesList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerRegistration!=null)
            listenerRegistration.remove();
    }

    private void linkViews() {

        recyclerView=findViewById(R.id.recycler_dest_list);
        progressBar=findViewById(R.id.dest_list_progress_bar);
        searchView=findViewById(R.id.dest_list_search);
    }

    private void setupToolBar() {

        Toolbar toolbar=findViewById(R.id.destination_list_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        actionBar.setTitle(getActivityTitle());

    }

    private void initViews() {

        //fab
        fab=findViewById(R.id.dest_list_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddCylinderType();
            }
        });

        //recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new CylinderTypesAdapter(this);
        DividerDecoration divider=new DividerDecoration(this,getResources().getColor(R.color.borderGrey));
        divider.setMargins(16,16);
        recyclerView.addItemDecoration(divider);

        recyclerView.setAdapter(adapter);


        //searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterData(newText);
                return true;
            }
        });

    }

    public void loadCylinderTypesList(){

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        showProgress();
        listenerRegistration=db.collection(DbPaths.COLLECTION_CYLINDER_TYPES)
                .orderBy("name")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            Msg.show(CylinderTypeActivity.this,"Error connecting to server. Check internet connection");
                            return;
                        }

                        List<DocumentSnapshot> documentSnapshots=querySnapshot.getDocuments();
                        List<CylinderType> data=new ArrayList<>();
                        for(DocumentSnapshot doc:documentSnapshots){
                            data.add(doc.toObject(CylinderType.class));
                        }
                        hideProgress();
                        setData(data);
                    }
                });


    }

    protected void setData(List<CylinderType> data){


        if(adapter==null){
            adapter=new CylinderTypesAdapter(data,this);
            recyclerView.setAdapter(adapter);
        }else{
            adapter.setData(data,searchView.getQuery().toString());
        }


    }

    protected void showProgress(){

        //recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

    }

    protected void hideProgress(){

        progressBar.setVisibility(View.INVISIBLE);
        //recyclerView.setVisibility(View.VISIBLE);
    }

    public boolean isDuplicate(String newTypeName){

        return adapter.isDuplicate(newTypeName);
    }

    @Override
    public void onCylinderTypeClicked(CylinderType cylinderType) {

        if(cylinderType.getNoOfCylinders()==0)
            onCylinderTypeEditClicked(cylinderType);
    }

    @Override
    public void onCylinderTypeLongClicked(CylinderType cylinderType) {

    }

    @Override
    public void onCylinderTypeEditClicked(CylinderType cylinderType) {

        AddCylinderTypeDialogFragment addFragment=new AddCylinderTypeDialogFragment();
        addFragment.setArguments(AddCylinderTypeDialogFragment.getStartingArgs(true,cylinderType));
        addFragment.show(getSupportFragmentManager(),"EditCylinderType");

    }

    @Override
    public void onCylinderTypeDeleteClicked(CylinderType cylinderType) {

        CylinderTypeDeleteFragment delFragment=new CylinderTypeDeleteFragment();
        delFragment.setArguments(CylinderTypeDeleteFragment.getStartingArgs(cylinderType.getName()));
        delFragment.show(getSupportFragmentManager(),"deleteCylinderType");

    }

    public void onAddCylinderType(){

        AddCylinderTypeDialogFragment addFragment=new AddCylinderTypeDialogFragment();
        addFragment.setArguments(AddCylinderTypeDialogFragment.getStartingArgs(false,null));
        addFragment.show(getSupportFragmentManager(),"AddCylinderType");

    }

    private String getActivityTitle(){

        return "Cylinder Types";
    }


}
