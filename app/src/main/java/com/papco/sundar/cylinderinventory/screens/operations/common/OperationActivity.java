package com.papco.sundar.cylinderinventory.screens.operations.common;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ExitConfirmationDialog;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionActivity;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;

import java.util.ArrayList;
import java.util.List;

public class OperationActivity extends TransactionActivity implements RecyclerListener<Integer> {

    private RecyclerView recyclerView;
    private OperationAdapter adapter;
    private TextView cylinderCount;
    private TextInputLayout destinationLayout;
    private TextInputEditText destinationField;
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private Button btnSave;



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

    @Override
    public void onBackPressed() {
        if(getCylinders().size()==0)
            super.onBackPressed();
        else{

            ExitConfirmationDialog exitDialog=new ExitConfirmationDialog();
            exitDialog.show(getSupportFragmentManager(),"exitDialog");

        }
    }

    private void linkViews() {

        recyclerView=findViewById(R.id.operation_inward_recycler);
        cylinderCount=findViewById(R.id.operation_inward_cyl_count);
        destinationLayout=findViewById(R.id.operation_destination_layout);
        destinationField=findViewById(R.id.operation_destination);
        progressBar=findViewById(R.id.operation_progress_bar);
    }

    private void initViews() {

        //recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new OperationAdapter(new ArrayList<Integer>(),this);
        DividerItemDecoration divider=new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);
        recyclerView.setAdapter(adapter);


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

    protected void hideFab(){
        fab.hide();
    }

    protected void showFab(){
        fab.show();
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

    protected List<Integer> getCylinders(){
        return adapter.getData();
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

    protected void hideDestinationLayout(){
        destinationLayout.setVisibility(View.GONE);
    }

    protected int getNumberOfCylindersAdded(){

        return getCylinders().size();
    }

    protected void loadDestinationName(String destName){

        destinationLayout.setVisibility(View.VISIBLE);
        destinationField.setText(destName);

    }


    @Override
    public void showTransactionProgressBar() {

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setEnabled(false);
        fab.hide();
        btnSave.setEnabled(false);

    }

    @Override
    public void hideTransactionProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setEnabled(true);
        fab.show();
        btnSave.setEnabled(true);
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
