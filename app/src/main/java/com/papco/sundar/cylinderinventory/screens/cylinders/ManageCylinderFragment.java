package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

import java.util.Map;

public class ManageCylinderFragment extends Fragment {

    private NestedScrollView detailsView;
    private ProgressBar progressBar;
    private View clickSense;
    private EditText searchBox;
    private FirebaseFirestore db;
    ListenerRegistration listener1;
    ListenerRegistration listener2;

    private TextView cylinderNo,purchaseDate,supplier,remarks,refills,repairs,location,status;

    private Button btnMarkRepair,btnDelete;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.manage_cylinder,container,false);
        linkViews(view);
        initViews(view);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        ((CylindersActivity)getActivity()).getSupportActionBar().setTitle("Manage cylinder");
    }

    private void linkViews(View view){

        detailsView=view.findViewById(R.id.mng_cyl_details);
        progressBar=view.findViewById(R.id.mng_cyl_progress_bar);
        clickSense=view.findViewById(R.id.mng_cyl_click_sense);

        //cylinderNo,purchaseDate,supplier,remarks,refills,repairs,location,status;
        cylinderNo=view.findViewById(R.id.mng_cyl_no);
        purchaseDate=view.findViewById(R.id.mng_cyl_purchase_date);
        supplier=view.findViewById(R.id.mng_cyl_supplier);
        remarks=view.findViewById(R.id.mng_cyl_remarks);
        refills=view.findViewById(R.id.mng_cyl_refills);
        repairs=view.findViewById(R.id.mng_cyl_repairs);
        location=view.findViewById(R.id.mng_cyl_location);
        status=view.findViewById(R.id.mng_cyl_status);

    }

    private void initViews(View view) {

        detailsView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        btnDelete=view.findViewById(R.id.mng_cyl_btn_delete);
        btnMarkRepair=view.findViewById(R.id.mng_cyl_btn_mark_repair);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeletion();
            }
        });

        btnMarkRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmMarkRepair();
            }
        });

        searchBox=view.findViewById(R.id.mng__cyl_search);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId== EditorInfo.IME_ACTION_SEARCH){
                    searchCylinder(searchBox.getText().toString());
                }

                return false;
            }
        });
        clickSense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                searchCylinder(searchBox.getText().toString());
            }
        });

    }

    private void confirmDeletion(){

        AlertDialog.Builder builder=new AlertDialog.Builder(requireActivity());
        builder.setTitle("DELETE");
        builder.setMessage("Are you sure want to delete this cylinder?\n" +
                "This operation cannot be undone");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCylinder();
            }
        });

        builder.setNegativeButton("CANCEL",null);
        builder.show();

    }

    private void confirmMarkRepair(){

        AlertDialog.Builder builder=new AlertDialog.Builder(requireActivity());
        builder.setTitle("DAMAGE");
        builder.setMessage("Are you sure want to mark this cylinder as damaged?");
        builder.setPositiveButton("MARK DAMAGED", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                markCylinderDamaged();
            }
        });

        builder.setNegativeButton("CANCEL",null);
        builder.show();

    }

    private void deleteCylinder(){


    }

    private void markCylinderDamaged(){


    }

    private void searchCylinder(String searchId){

        if(TextUtils.isEmpty(searchId))
            return;

        searchId=searchId.trim();

        showProgressBar();
        checkForActiveCylinder(searchId);
    }

    private void checkForActiveCylinder(final String searchId){

        //check if the cylinder exists in active state
        db.document("/cylinders/"+searchId).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists())
                        loadCylinder(task.getResult());
                    else
                        checkInGraveYard(searchId);
                }else{
                    checkInternetPrompt();
                }
            }
        });

    }

    private void checkInGraveYard(final String searchId){

        //check if the cylinder exists in active state
        db.document("/graveyard/"+searchId).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists())
                        loadCylinder(task.getResult());
                    else
                        showNotFound();
                }else{
                    checkInternetPrompt();
                }
            }
        });

    }

    private void checkInternetPrompt(){

        progressBar.setVisibility(View.INVISIBLE);
        detailsView.setVisibility(View.INVISIBLE);
        if(getActivity()!=null)
            Msg.show(requireContext(), "Error connecting to server. Please check internet connection");

    }

    private void showNotFound(){

        detailsView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        if(getActivity()!=null)
            Msg.show(requireContext(), "Cylinder not found");

    }

    private void loadCylinder(DocumentSnapshot data){

        Cylinder cylinder=data.toObject(Cylinder.class);

        detailsView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        cylinderNo.setText(cylinder.getStringId());
        purchaseDate.setText(cylinder.getStringPurchaseDate());
        supplier.setText(cylinder.getSupplier());
        remarks.setText(cylinder.getRemarks());
        refills.setText(Integer.toString(cylinder.getRefillCount()));
        repairs.setText(Integer.toString(cylinder.getDamageCount()));
        location.setText(cylinder.getLocationName());

        if(cylinder.getLocationId()== Destination.TYPE_GRAVEYARD){
            status.setText("DEACTIVATED");
            btnDelete.setVisibility(View.INVISIBLE);
            btnMarkRepair.setVisibility(View.INVISIBLE);
            return;
        }

        // we can delete a cylinder or mark as repair only when its in the warehouse
        if(cylinder.getLocationId()==Destination.TYPE_WAREHOUSE){
            btnDelete.setVisibility(View.VISIBLE);
            btnMarkRepair.setVisibility(View.VISIBLE);
        }else {
            btnDelete.setVisibility(View.INVISIBLE);
            btnMarkRepair.setVisibility(View.INVISIBLE);
        }

        // we cannot mark a cylinder as repair when a cylinder is already damaged
        if(cylinder.isDamaged()){
            status.setText("DAMAGED");
            btnMarkRepair.setVisibility(View.INVISIBLE);
            return;
        }


        if(cylinder.isEmpty())
            status.setText("EMPTY");
        else
            status.setText("FULL");

    }

    private void showProgressBar(){

        detailsView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

    }

    private void hideProgressBar(){

        detailsView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void hideKeyboard(){

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(clickSense.getWindowToken(), 0);
    }
}
