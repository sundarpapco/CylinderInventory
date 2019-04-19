package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionFragment;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.Transactions.DeleteCylinderTransaction;
import com.papco.sundar.cylinderinventory.logic.Transactions.MarkDamageTransaction;
import com.papco.sundar.cylinderinventory.screens.cylinders.history.CylinderHistoryActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class ManageCylinderFragment extends TransactionFragment {

    private final int REQUEST_REPAIR = 1;
    private final int REQUEST_DELETE = 2;

    private NestedScrollView detailsView;
    private ProgressBar progressBar;
    private ProgressBar smallProgressBar;
    private View clickSense;
    private EditText searchBox;
    private FirebaseFirestore db;
    private ListenerRegistration activeListener;

    private TextView cylinderNo, purchaseDate, supplier, remarks, refills, repairs, location, status, cylinderType;
    private TextView lastTransaction,showHistory;

    private Button btnMarkRepair, btnDelete;

    //region system Overloads --------------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.manage_cylinder, container, false);
        linkViews(view);
        initViews(view);
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        if (detailsView.getVisibility()==View.VISIBLE)
            checkForActiveCylinder(cylinderNo.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((CylindersActivity) getActivity()).getSupportActionBar().setTitle("Manage cylinder");
    }

    @Override
    public void onStop() {
        super.onStop();
        clearCallbacks();
    }

    //endregion ------------------------------------------------------------

    private void linkViews(View view) {

        detailsView = view.findViewById(R.id.mng_cyl_details);
        progressBar = view.findViewById(R.id.mng_cyl_progress_bar);
        smallProgressBar = view.findViewById(R.id.small_progress);
        clickSense = view.findViewById(R.id.mng_cyl_click_sense);

        //cylinderNo,purchaseDate,supplier,remarks,refills,repairs,location,status;
        cylinderNo = view.findViewById(R.id.mng_cyl_no);
        purchaseDate = view.findViewById(R.id.mng_cyl_purchase_date);
        supplier = view.findViewById(R.id.mng_cyl_supplier);
        remarks = view.findViewById(R.id.mng_cyl_remarks);
        refills = view.findViewById(R.id.mng_cyl_refills);
        repairs = view.findViewById(R.id.mng_cyl_repairs);
        location = view.findViewById(R.id.mng_cyl_location);
        status = view.findViewById(R.id.mng_cyl_status);
        cylinderType=view.findViewById(R.id.mng_cyl_cylinder_type);
        lastTransaction=view.findViewById(R.id.mng_cyl_last_transaction);
        showHistory=view.findViewById(R.id.mng_cyl_show_history);

    }

    private void initViews(View view) {

        detailsView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        btnDelete = view.findViewById(R.id.mng_cyl_btn_delete);
        btnMarkRepair = view.findViewById(R.id.mng_cyl_btn_mark_repair);

        btnDelete.setOnClickListener(v -> confirmDeletion());

        btnMarkRepair.setOnClickListener(v -> confirmMarkRepair());

        searchBox = view.findViewById(R.id.mng__cyl_search);
        searchBox.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCylinder(searchBox.getText().toString());
            }

            return false;
        });
        clickSense.setOnClickListener(v -> {
            hideKeyboard();
            searchCylinder(searchBox.getText().toString());
        });
        showHistory.setOnClickListener(v -> {
            launchCylinderHistoryActivity();
        });

    }

    private void launchCylinderHistoryActivity() {

        CylinderHistoryActivity.start(getActivity(),Integer.parseInt(cylinderNo.getText().toString()));
    }

    //region Operation initiators --------------------------------

    private void confirmDeletion() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("DELETE");
        builder.setMessage("Are you sure want to delete this cylinder?\n" +
                "This operation cannot be undone");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCylinder();
            }
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();

    }

    private void confirmMarkRepair() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("DAMAGE");
        builder.setMessage("Are you sure want to mark this cylinder as damaged?");
        builder.setPositiveButton("MARK DAMAGED", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                markCylinderDamaged();
            }
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();

    }

    private void deleteCylinder() {


        String successMsg = "Success: Cylinder deleted";
        String progressMsg="Deleting cylinder";
        String failureMsg = "Deleting cylinder failed. Please try again";

        startTransaction(successMsg,progressMsg,failureMsg,REQUEST_DELETE);

    }

    private void markCylinderDamaged() {

        String successMsg = "Success: Cylinder marked as damaged";
        String progressMsg="Marking cylinder as damaged";
        String failureMsg = "Could not mark cylinder as damaged. Please try again";

        startTransaction(successMsg,progressMsg,failureMsg,REQUEST_REPAIR);

    }

    //endregion ----------------------------------------------------

    //region Cylinder searching methods ------------------------------

    private void searchCylinder(String searchId) {

        if (TextUtils.isEmpty(searchId))
            return;

        searchId = searchId.trim();

        showProgressBar();
        checkForActiveCylinder(searchId.trim());
    }

    private void checkForActiveCylinder(final String searchId) {

        if (TextUtils.isEmpty(searchId))
            return;

        if (activeListener != null)
            activeListener.remove();


        showProgressBar();

        activeListener = db.document("/cylinders/" + searchId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            checkInternetPrompt();
                            activeListener.remove();
                            activeListener = null;
                            return;
                        }

                        if (!documentSnapshot.exists()) {
                            activeListener.remove();
                            activeListener = null;
                            checkInGraveYard(searchId);
                            return;
                        }

                        loadCylinder(documentSnapshot);
                    }
                });


    }

    private void checkInGraveYard(final String searchId) {

        //check if the cylinder exists in active state
        db.document("/graveyard/" + searchId).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists())
                                loadCylinder(task.getResult());
                            else
                                showNotFound();
                        } else {
                            checkInternetPrompt();
                        }
                    }
                });

    }

    private void checkInternetPrompt() {

        progressBar.setVisibility(View.INVISIBLE);
        detailsView.setVisibility(View.INVISIBLE);
        if (getActivity() != null)
            Msg.show(requireContext(), "Error connecting to server. Please check internet connection");

    }

    private void showNotFound() {

        detailsView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        if (getActivity() != null)
            Msg.show(requireContext(), "Cylinder not found");

    }

    private void loadCylinder(DocumentSnapshot data) {

        Cylinder cylinder = data.toObject(Cylinder.class);

        hideProgressBar();

        cylinderNo.setText(cylinder.getStringId());
        purchaseDate.setText(cylinder.getStringPurchaseDate());
        supplier.setText(cylinder.getSupplier());
        remarks.setText(cylinder.getRemarks());
        refills.setText(Integer.toString(cylinder.getRefillCount()));
        repairs.setText(Integer.toString(cylinder.getDamageCount()));
        location.setText(cylinder.getLocationName());
        cylinderType.setText(cylinder.getCylinderTypeName());
        lastTransaction.setText(cylinder.getStringLastTransaction());

        if (cylinder.getLocationId() == Destination.TYPE_GRAVEYARD) {
            status.setText("DEACTIVATED");
            btnDelete.setVisibility(View.INVISIBLE);
            btnMarkRepair.setVisibility(View.INVISIBLE);
            return;
        }

        // we can delete a cylinder or mark as repair only when its in the warehouse
        if (cylinder.getLocationId() == Destination.TYPE_WAREHOUSE) {
            btnDelete.setVisibility(View.VISIBLE);
            btnMarkRepair.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.INVISIBLE);
            btnMarkRepair.setVisibility(View.INVISIBLE);
        }

        // we cannot mark a cylinder as repair when a cylinder is already damaged
        if (cylinder.isDamaged()) {
            if(cylinder.getLocationId()==Destination.TYPE_WAREHOUSE)
                status.setText("DAMAGED");
            else
                status.setText("SENT FOR REPAIR");

            btnMarkRepair.setVisibility(View.INVISIBLE);
            return;
        }


        if (cylinder.isEmpty())
            if(cylinder.getLocationId()==Destination.TYPE_WAREHOUSE)
                status.setText("EMPTY");
            else
                status.setText("SENT FOR REFILLING");
        else
            if(cylinder.getLocationId()!=Destination.TYPE_WAREHOUSE)
                status.setText("WITH CLIENT");
            else
                status.setText("FULL");

    }

    private void showProgressBar() {

        detailsView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

    }

    private void hideProgressBar() {

        detailsView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    // endregion ------------------------------------------------------

    //region Transaction Overloads --------------------------------------------------

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {

        int cylinderNumber = Integer.parseInt(cylinderNo.getText().toString());

        if (requestCode == REQUEST_REPAIR)
            return new MarkDamageTransaction(cylinderNumber);

        if (requestCode == REQUEST_DELETE)
            return new DeleteCylinderTransaction(cylinderNumber);

        return null;
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if (!task.isSuccessful()) {
            FirebaseFirestoreException exception = (FirebaseFirestoreException) task.getException();
            if (exception.getCode() == FirebaseFirestoreException.Code.CANCELLED)
                Msg.show(getActivity(), exception.getMessage());
            else
                Msg.show(requireActivity(), "Error connecting to server. Check internet connection");
        }
    }

    @Override
    public void showTransactionProgressBar() {

        smallProgressBar.setVisibility(View.VISIBLE);
        btnMarkRepair.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    @Override
    public void hideTransactionProgressBar() {

        smallProgressBar.setVisibility(View.INVISIBLE);
        btnMarkRepair.setEnabled(true);
        btnDelete.setEnabled(true);

    }

    //endregion ----------------------------------------------------------------

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(clickSense.getWindowToken(), 0);
    }

    public void clearCallbacks() {

        if (activeListener != null)
            activeListener.remove();
        activeListener = null;

    }

}
