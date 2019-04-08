package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionFragment;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.CylinderType;
import com.papco.sundar.cylinderinventory.logic.Transactions.AddCylindersTransaction;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;

import java.util.ArrayList;
import java.util.List;

public class AddCylinderFragment extends TransactionFragment {

    private FirebaseFirestore db;
    private TextView infoView;
    private ListenerRegistration listener,cylinderTypesListener;
    private TextInputEditText supplierField,cylinderCountField,remarksField;
    private int lastCylinderNumber=0;
    private ProgressBar progressBar;
    private Button btnSave;
    private AppCompatSpinner spinner;
    private List<CylinderType> cylinderTypes=new ArrayList<>();
    private ArrayAdapter<CylinderType> spinnerAdapter;

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
        spinner=view.findViewById(R.id.add_cyl_cyliner_type_spinner);

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

        initSpinner();

    }

    private void initSpinner(){

        CylinderType defaultType=new CylinderType();
        defaultType.setName("Default");
        defaultType.setNoOfCylinders(0);
        cylinderTypes.add(defaultType);

        spinnerAdapter=new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item,
                R.id.spinner_item_text,
                cylinderTypes);

        spinner.setAdapter(spinnerAdapter);
        loadCylinderTypesList();
    }

    private void loadCylinderTypesList(){

        if(cylinderTypesListener!=null)
            cylinderTypesListener.remove();

        cylinderTypesListener=db.collection(DbPaths.COLLECTION_CYLINDER_TYPES)
                .orderBy("name")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null){
                            Msg.show(getActivity(),"Error connecting to server. Please check internet connection");
                            return;
                        }

                        List<CylinderType> types=new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot:querySnapshot.getDocuments()){
                            types.add(documentSnapshot.toObject(CylinderType.class));
                        }

                        if(types.size()==0){

                            CylinderType defaultType=new CylinderType();
                            defaultType.setNoOfCylinders(0);
                            defaultType.setName("Default");
                            spinnerAdapter.clear();
                            spinnerAdapter.add(defaultType);
                            return;
                        }

                        spinnerAdapter.clear();
                        spinnerAdapter.addAll(types);
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

        String successMsg="Successfully added the cylinders";
        String failMsg="Adding cylinder failed. Check internet connection and try again";
        String progressMsg="Adding cylinders";
        startTransaction(successMsg,progressMsg,failMsg,1);

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

        if(Integer.parseInt(cylinderCountField.getText().toString())>50){
            Msg.show(requireContext(),"Maximum of 50 cylinders can be added at one time");
            return false;
        }

        if(Integer.parseInt(cylinderCountField.getText().toString())<=0){
            Msg.show(getActivity(),"Please enter valid number of cylinders");
            return false;
        }

        if(((CylinderType)spinner.getSelectedItem()).getName().equals("Default")){
            Msg.show(getActivity(),"A valid cylinder type is needed");
            return false;
        }

        return true;

    }

    //Transaction overrides

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {

        int numberOfCylinders=Integer.parseInt(cylinderCountField.getText().toString().trim());
        String supplier=supplierField.getText().toString();
        String remarks=remarksField.getText().toString();
        CylinderType cylinderType=(CylinderType)spinner.getSelectedItem();
        return new AddCylindersTransaction(numberOfCylinders,supplier,remarks,cylinderType.getName());

    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task.isSuccessful()) {
            Msg.show(getActivity(), "Added cylinders successfully");
            finishFragment();
        }else{
            Msg.show(getActivity(),"Failed to add cylinders. Check internet connection and try again");
        }
    }

    @Override
    public void showTransactionProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
    }

    @Override
    public void hideTransactionProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        btnSave.setEnabled(true);
    }


    @Override
    public void onStop() {
        super.onStop();
        listener.remove();
        listener=null;
        if(cylinderTypesListener!=null)
            cylinderTypesListener.remove();
    }

}
