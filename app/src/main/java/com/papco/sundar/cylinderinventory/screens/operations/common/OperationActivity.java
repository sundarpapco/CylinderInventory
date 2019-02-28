package com.papco.sundar.cylinderinventory.screens.operations.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;

import java.util.ArrayList;
import java.util.List;

public class OperationActivity extends AppCompatActivity implements RecyclerListener<Integer> {

    private RecyclerView recyclerView;
    private OperationAdapter adapter;
    private FrameLayout progressBar;
    private TextView cylinderCount;
    private TextInputLayout destinationLayout;
    private TextInputEditText destinationField;
    protected FloatingActionButton fab;
    protected Button btnSave;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.operation);
        linkViews();
        initViews();
        setupToolBar();
        hideDestinationLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    private void linkViews() {

        recyclerView=findViewById(R.id.operation_inward_recycler);
        progressBar=findViewById(R.id.operation_inward_progressbar);
        cylinderCount=findViewById(R.id.operation_inward_cyl_count);
        destinationLayout=findViewById(R.id.operation_destination_layout);
        destinationField=findViewById(R.id.operation_destination);
    }

    private void initViews() {

        //recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new OperationAdapter(new ArrayList<Integer>(),this);
        DividerItemDecoration divider=new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);
        recyclerView.setAdapter(adapter);


        progressBar.setVisibility(View.GONE);
        cylinderCount.setText("0");

        //fab
        fab=findViewById(R.id.operation_inward_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddCylinderNumber();
            }
        });

        //save button
        btnSave=findViewById(R.id.operation_inward_btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveOperation();
            }
        });

        //destinationField not editable
        destinationField.setKeyListener(null);

    }

    protected void onSaveOperation() {

        showMessage("Save the operation here");
    }

    private void setupToolBar(){
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        actionBar.setTitle(getActivityTitle());
    }

    protected void setData(List<Integer> data){

        if (adapter == null) {
            adapter=new OperationAdapter(data,this);
            recyclerView.setAdapter(adapter);
        }else
            adapter.setData(data);

        cylinderCount.setText(Integer.toString(data.size()));

    }

    protected String getActivityTitle(){
        return "Operation Inward";
    }

    public boolean isAlreadyAdded(int no){
        if(adapter!=null)
            return adapter.contains(no);
        else
            return false;
    }

    public void addNumber(int no){
        if(adapter!=null)
            adapter.addNumber(no);

        int cylindercount=Integer.parseInt(cylinderCount.getText().toString());
        cylinderCount.setText(Integer.toString(cylindercount+1));
    }

    public void removeNumber(int position){

        adapter.removeNumber(position);
        int cylindercount=Integer.parseInt(cylinderCount.getText().toString());
        cylinderCount.setText(Integer.toString(cylindercount-1));
    }

    public void updateNumber(int number,int position){
        if(adapter!=null)
            adapter.updateNumber(number,position);
    }

    protected void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgress(){
        progressBar.setVisibility(View.GONE);
    }

    protected void hideDestinationLayout(){
        destinationLayout.setVisibility(View.GONE);
    }

    protected int getNumberOfCylindersAdded(){

        return Integer.parseInt(cylinderCount.getText().toString());
    }

    protected void loadDestinationName(String destName){

        destinationLayout.setVisibility(View.VISIBLE);
        destinationField.setText(destName);

    }


    @Override
    public void onRecyclerItemClicked(Integer item,int position) {
        onCylinderNumberEdit(item,position);
    }

    @Override
    public void onRecyclerItemLongClicked(Integer item, final int position, View view) {

        PopupMenu popupMenu=new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.mnu_delete,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId()==R.id.mnu_delete){
                    removeNumber(position);
                }

                return false;
            }
        });
        popupMenu.show();

    }

    private void onCylinderNumberEdit(int cylNumber,int position){
        AddCylinderNumberFragment fragment=new AddCylinderNumberFragment();
        fragment.setArguments(AddCylinderNumberFragment.getStartingArguments(true,cylNumber,position));
        fragment.show(getSupportFragmentManager(),"editCylinderNumber");
    }

    private void onAddCylinderNumber(){
        AddCylinderNumberFragment fragment=new AddCylinderNumberFragment();
        fragment.setArguments(AddCylinderNumberFragment.getStartingArguments(false,-1,-1));
        fragment.show(getSupportFragmentManager(),"addCylinderNumber");
    }

    public boolean canAddMoreCylinderNumbers(){
        return true;
    }

    protected void showMessage(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}
