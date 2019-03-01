package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.logic.Transactions.AddCylindersTransaction;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;

public class AddCylinderFragment extends Fragment implements TransactionRunnerService.TransactionListener {

    private FirebaseFirestore db;
    private TextView infoView;
    private ListenerRegistration listener;
    private TextInputEditText supplierField,cylinderCountField,remarksField;
    private int lastCylinderNumber=-1;
    private ProgressBar progressBar;
    private TransactionRunnerService transactionService;
    private TransactionServiceConnection connection;
    private Button btnSave;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.add_cylinders,container,false);
        linkViews(view);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        getLastCylinderNumber();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((CylindersActivity)getActivity()).getSupportActionBar().setTitle("Add cylinders");

    }

    private void linkViews(View view){

        infoView=view.findViewById(R.id.add_cyl_info_text);
        supplierField=view.findViewById(R.id.add_cyl_supplier_name);
        cylinderCountField=view.findViewById(R.id.add_cyl_cylinders_count);
        remarksField=view.findViewById(R.id.add_cyl_remarks);
        progressBar=view.findViewById(R.id.add_cyl_progressbar);
        btnSave=view.findViewById(R.id.add_cyl_btn_add);

    }

    private void initViews(View view){

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAdd();
            }
        });
        infoView.setText("Loading cylinder number... Please wait...");
        cylinderCountField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateInfoView();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void confirmAdd(){

        if(!isAllDataValid())
            return;

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("ADD");
        builder.setMessage("Sure want to add the cylinders?");
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addCylinders();
            }
        });
        builder.setNegativeButton("CANCEL",null);
        builder.show();

    }

    private void getLastCylinderNumber(){

        Log.d("SUNDAR", "getLastCylinderNumber: ");
        listener=db.document(DbPaths.COUNT_CYLINDERS_TOTAL).addSnapshotListener(
                MetadataChanges.INCLUDE,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if(e!=null){
                    Msg.show(AddCylinderFragment.this.getActivity(),"Fetching last numbder failed. Please check intenet connection");
                }

                if(documentSnapshot!=null && documentSnapshot.exists()){
                    if(documentSnapshot.getMetadata().isFromCache())
                        return;

                    onLastCylinderNumberFetched(documentSnapshot.getLong("count").intValue());
                }

            }
        });



    }

    private void onLastCylinderNumberFetched(int lastNumber){

        lastCylinderNumber=lastNumber;
        updateInfoView();
    }

    private void updateInfoView(){

        if(TextUtils.isEmpty(cylinderCountField.getText().toString())){
            infoView.setText("");
            return;
        }

        int cylinderCount=Integer.parseInt(cylinderCountField.getText().toString());

        if(cylinderCount==0){
            infoView.setText("Invalid cylinder count");
            return;
        }

        if(cylinderCount==1){
            infoView.setText("Cylinder "+Integer.toString(lastCylinderNumber+1)+" will be added");
        }else {
            infoView.setText("Cylinders " + Integer.toString(lastCylinderNumber+1)+ " to " +
                    Integer.toString(lastCylinderNumber+cylinderCount)+" will be added");
        }

    }

    private void addCylinders(){

        if(TransactionRunnerService.isRunning()){
            Msg.show(requireContext(),"Already a transaction is running. Try after some time");
            return;
        }

        showProgressBar();

        String successMsg="Successfully added the cylinders";
        String failMsg="Adding cylinder failed. Check internet connection and try again";

        //starting the service
        Intent intent=TransactionRunnerService.getStartingIntent(
                getActivity(),successMsg,"Running transaction",failMsg,1);
        getActivity().startService(intent);

        //binding to the service
        Intent bindIntent = new Intent(getActivity(), TransactionRunnerService.class);
        connection = new TransactionServiceConnection();
        getActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);

        //on Successfull binding, onServiceBinded will be called where we allot work to service


    }

    private void finishFragment() {

        ((CylindersActivity)getActivity()).popBackStack();

    }

    private boolean isAllDataValid(){

        if(TextUtils.isEmpty(supplierField.getText().toString())){
            Msg.show(getActivity(),"Please enter valid supplier");
            return false;
        }

        if(TextUtils.isEmpty(cylinderCountField.getText().toString())){
            Msg.show(getActivity(),"Please enter valid number of cylinders");
            return false;
        }

        if(Integer.parseInt(cylinderCountField.getText().toString())>20){
            Msg.show(requireContext(),"Maximum of 20 cylinders can be added at one time");
            return false;
        }

        if(Integer.parseInt(cylinderCountField.getText().toString())<=0){
            Msg.show(getActivity(),"Please enter valid number of cylinders");
            return false;
        }

        return true;

    }

    private void onServiceBinded(TransactionRunnerService service){

        int noOfCylinders=Integer.parseInt(cylinderCountField.getText().toString());
        String supplier=supplierField.getText().toString();
        String remarks=remarksField.getText().toString();

        AddCylindersTransaction transaction=new AddCylindersTransaction(noOfCylinders,supplier,remarks);
        service.setCallback(this);
        service.startTransaction(transaction);

    }

    @Override
    public void onTransactionComplete(Task<Void> task,int requestCode) {

        hideProgressBar();

        if(task.isSuccessful()) {
            Msg.show(getActivity(), "Added cylinders successfully");
            finishFragment();
        }else{
            Msg.show(getActivity(),"Failed to add cylinders. Check internet connection and try again");
        }

    }

    private void showProgressBar(){

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

    }

    private void hideProgressBar(){

        progressBar.setVisibility(View.INVISIBLE);
        btnSave.setEnabled(true);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("SUNDAR", "stopping addcylinder fragment ");
        hideProgressBar();
        listener.remove();
        listener=null;
        if(transactionService!=null)
            transactionService.clearCallback();
    }


    class TransactionServiceConnection implements ServiceConnection {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            // TODO: 19-11-2018 update the layout for progress
            transactionService = ((TransactionRunnerService.TransactionBinder) iBinder).getService();
            onServiceBinded(transactionService);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            transactionService = null;

        }
    }
}
