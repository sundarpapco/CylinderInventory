package com.papco.sundar.cylinderinventory.screens.destinations.common;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.DividerDecoration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.screens.destinations.clients.ClientsActivity;

import java.util.ArrayList;
import java.util.List;

public class DestinationActivity extends ConnectivityActivity implements RecyclerListener<Destination> {

    private RecyclerView recyclerView;
    private DestinationAdapter adapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.destination_list);
        linkViews();
        setupToolBar();
        initViews();
        loadDestinationList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRecyclerItemClicked(Destination item,int position) {

        onShowEditDestinationDialog(item);

    }

    @Override
    public void onRecyclerItemLongClicked(Destination item, int position, View view) {

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
                onShowNewDestinationDialog();
            }
        });

        //recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new DestinationAdapter(new ArrayList<Destination>(),this);
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


    private void loadDestinationList() {

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        showProgress();
        db.collection(DbPaths.COLLECTION_DESTINATIONS)
                .whereEqualTo("destType",getDestinationType())
                .orderBy("name")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            Msg.show(DestinationActivity.this,"Error connecting to server. Check internet connection");
                            return;
                        }

                        List<DocumentSnapshot> documentSnapshots=querySnapshot.getDocuments();
                        List<Destination> data=new ArrayList<>();
                        for(DocumentSnapshot doc:documentSnapshots){
                            data.add(doc.toObject(Destination.class));
                        }
                        hideProgress();
                        setData(data);
                    }
                });


    }


    public String getActivityTitle(){

        return "Destination";
    }

    protected void setData(List<Destination> data){

        if(adapter==null){
            adapter=new DestinationAdapter(data,this);
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

    protected void hideFab(){

        fab.hide();

    }

    protected int getDestinationType(){

        return Destination.TYPE_CLIENT;
    }

    public void onShowEditDestinationDialog(Destination destination){

    }

    public void onShowNewDestinationDialog(){


    }

    protected void showMessage(String msg){

        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

    }
}
