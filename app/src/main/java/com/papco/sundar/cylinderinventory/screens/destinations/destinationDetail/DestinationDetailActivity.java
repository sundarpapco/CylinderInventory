package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionActivity;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.Transactions.DeleteDestinationTransaction;
import com.papco.sundar.cylinderinventory.screens.destinations.clients.AddClientDialog;
import com.papco.sundar.cylinderinventory.screens.destinations.common.AddDestinationFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

public class DestinationDetailActivity extends TransactionActivity {

    private static final String KEY_DEST_ID="dest_id";
    private static final String KEY_DEST_NAME="dest_name";
    private static final String KEY_DEST_CYL_COUNT="dest_cyl_count";
    private static final String KEY_DEST_TYPE="dest_type";


    public static void start(@NonNull Context context, @NonNull Destination destination){

        Bundle args=new Bundle();
        args.putInt(KEY_DEST_ID,destination.getId());
        args.putString(KEY_DEST_NAME,destination.getName());
        args.putInt(KEY_DEST_CYL_COUNT,destination.getCylinderCount());
        args.putInt(KEY_DEST_TYPE,destination.getDestType());

        Intent intent=new Intent(context, DestinationDetailActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);

    }


    private TabLayout tabLayout;
    private PagerAdapter pagerAdapter;
    private Destination loadedDestination;
    private ViewPager viewPager;
    private TextView destinationName,cylinderCount;
    private ProgressBar progressBar;
    private DestinationDetailVM viewModel;
    private boolean canBeEdited=false;

    //Strings for the deleteDestination Transaction which will be run by this activity
    private String successMsg,progressMsg,failureMsg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.destination_detail);
        linkViews();
        initViews();
        initToolBar();
        initViewModel();

    }

    private void linkViews() {

        tabLayout=findViewById(R.id.dest_detail_tabs);
        viewPager=findViewById(R.id.dest_detail_viewpager);
        destinationName=findViewById(R.id.dest_detail_dest_name);
        cylinderCount=findViewById(R.id.dest_detail_cylinder_count);
        progressBar=findViewById(R.id.dest_detail_progress);
    }

    private void initViews() {

        pagerAdapter =new PagerAdapter(getSupportFragmentManager(),getDestination().getId());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        loadDestinationInfo();

    }

    private void initToolBar() {

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        switch (getDestination().getDestType()){

            case Destination.TYPE_CLIENT:
                actionBar.setTitle("Client details");
                break;

            case Destination.TYPE_REFILL_STATION:
                actionBar.setTitle("Refill station details");
                break;

            case Destination.TYPE_REPAIR_STATION:
                actionBar.setTitle("Repair station details");
                break;
        }

    }

    private void initViewModel(){

        viewModel= ViewModelProviders.of(this).get(DestinationDetailVM.class);

        viewModel.getLoadedDestination(getDestination().getId()).observe(this, new Observer<Destination>() {
            @Override
            public void onChanged(Destination destination) {

                if(destination==null){
                    //meaning, someone has deleted the destination in parallel. Just close this activity
                    finish();
                    return;
                }
                loadedDestination=destination;
                loadDestinationInfo();
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(!canBeEdited){
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
        }else {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.edit_destination,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }

        if(item.getItemId()==R.id.mnu_dest_edit){
            onEditDestination();
            return true;
        }

        if(item.getItemId()==R.id.mnu_dest_delete){
            showDeleteConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Sure want to delete "
                +getDestination().getStringDestType()
                + " " + getDestination().getName()+" ?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDeleteDestination();
            }
        });

        builder.setNegativeButton("CANCEL",null);
        builder.show();

    }

    private void onDeleteDestination(){

        successMsg=getDestination().getStringDestType()+" deleted successfully";
        progressMsg="Deleting "+getDestination().getStringDestType();
        failureMsg="Failed to delete "+getDestination().getStringDestType();

        startTransaction(successMsg,progressMsg,failureMsg,1);

    }

    private void onEditDestination(){

        AddDestinationFragment fragment=new AddClientDialog();
        fragment.setArguments(AddDestinationFragment.getStartingArguments(true,getDestination()));
        fragment.show(getSupportFragmentManager(),"editDestination");

    }

    private void loadDestinationInfo(){
        destinationName.setText(getDestination().getName());
        cylinderCount.setText(Integer.toString(getDestination().getCylinderCount())+" Cylinders");
        canBeEdited=getDestination().isEditable();
        invalidateOptionsMenu();

    }

    private Destination getDestination(){

        if(loadedDestination!=null)
            return loadedDestination;

        Bundle args=getIntent().getExtras();
        if(args==null)
            return null;

        loadedDestination=new Destination();
        loadedDestination.setId(args.getInt(KEY_DEST_ID));
        loadedDestination.setCylinderCount(args.getInt(KEY_DEST_CYL_COUNT));
        loadedDestination.setName(args.getString(KEY_DEST_NAME));
        loadedDestination.setDestType(args.getInt(KEY_DEST_TYPE));

        return loadedDestination;

    }

    // region ----------- Transaction Overloads

    @Override
    public void showTransactionProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTransactionProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {
        return new DeleteDestinationTransaction(getDestination());
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task.isSuccessful()){
            Msg.show(this,successMsg);
            //we don't need to finish the activity here as there is a active listener watching
            //this destination document and VM will buzz with null value when deleted. There
            // the activity will be closed
        }else{
            Msg.show(this,failureMsg);
        }

    }


    //endregion

}
