package com.papco.sundar.cylinderinventory.screens.destinations.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionDialogFragment;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.Transactions.AddDestinationTransaction;
import com.papco.sundar.cylinderinventory.logic.Transactions.EditDestinationTransaction;

public class AddDestinationFragment extends TransactionDialogFragment {

    public static Bundle getStartingArguments(boolean isEditing, Destination destination) {

        Bundle arguments = new Bundle();
        arguments.putBoolean(KEY_MODE, isEditing);
        if (destination != null) {
            arguments.putInt("destId", destination.getId());
            arguments.putString("destName", destination.getName());
            arguments.putInt("destType", destination.getDestType());
            arguments.putInt("destCylCount", destination.getCylinderCount());
        }
        return arguments;

    }

    private static final String KEY_MODE = "isEditing";

    private TextView heading;
    private EditText destinationName;
    private Button btnSave;
    private ProgressBar progressBar;
    private Destination editingDestination;

    private String successMsg = "Destination added successfully";
    private String progressMsg = "Adding Destination";
    private String failureMsg = "Failed to add destination. Check internet connection";

    private final int REQ_CODE_ADD = 1;
    private final int REQ_CODE_EDIT = 2;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.new_destination, null);
        linkViews(view);
        initViews(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();
    }


    private void linkViews(View view) {

        heading = view.findViewById(R.id.dest_new_heading);
        destinationName = view.findViewById(R.id.dest_new_name);
        btnSave = view.findViewById(R.id.dest_new_btnSave);
        progressBar = view.findViewById(R.id.dest_new_progressBar);
    }

    private void initViews(View view) {

        heading.setText(getTitle());
        if (isEditingMode() && getDestination() != null) {
            destinationName.setText(getDestination().getName());
        } else
            destinationName.setText("");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameIsValid())
                    onSaveDestination();
                else
                    Msg.show(requireContext(), "Please enter a valid name");

            }
        });

    }

    public String getTitle() {
        return "Destination name";
    }

    private boolean nameIsValid() {

        if (TextUtils.isEmpty(destinationName.getText().toString().trim()))
            return false;
        else
            return true;
    }

    protected String getEnteredName() {

        return destinationName.getText().toString().trim();
    }

    public void onSaveDestination() {

        if (isEditingMode())
            editDestination();
        else
            addDestination();

    }

    public boolean isEditingMode() {

        if (getArguments() != null)
            return getArguments().getBoolean(KEY_MODE, false);

        return false;
    }

    protected Destination getDestination() {

        if (getArguments() == null)
            return null;

        if (getArguments().getInt("destId", -1) == -1)
            return null;

        if (editingDestination == null) {

            editingDestination = new Destination();
            Bundle arg = getArguments();

            editingDestination.setId(arg.getInt("destId"));
            editingDestination.setName(arg.getString("destName"));
            editingDestination.setDestType(arg.getInt("destType"));
            editingDestination.setCylinderCount(arg.getInt("destCylCount"));

        }

        return editingDestination;
    }

    protected int getDestinationType() {

        return Destination.TYPE_WAREHOUSE;
    }

    private void addDestination() {

        switch (getDestinationType()) {

            case Destination.TYPE_CLIENT:
                successMsg = "Client added successfully";
                progressMsg = "Adding client";
                failureMsg = "Failed to add client. Check internet connection";
                break;

            case Destination.TYPE_REFILL_STATION:
                successMsg = "Refill station added successfully";
                progressMsg = "Adding refill station";
                failureMsg = "Failed to add refill station. Check internet connection";
                break;

            case Destination.TYPE_REPAIR_STATION:
                successMsg = "Repair station added successfully";
                progressMsg = "Adding repair station";
                failureMsg = "Failed to add repair station. Check internet connection";
                break;

        }

        startTransaction(successMsg, progressMsg, failureMsg, REQ_CODE_ADD);


    }

    private void editDestination() {

        switch (getDestinationType()) {

            case Destination.TYPE_CLIENT:
                successMsg = "Client edited successfully";
                progressMsg = "Editing client";
                failureMsg = "Failed to edit client. Check internet connection";
                break;

            case Destination.TYPE_REFILL_STATION:
                successMsg = "Refill station edited successfully";
                progressMsg = "Editing refill station";
                failureMsg = "Failed to edit refill station. Check internet connection";
                break;

            case Destination.TYPE_REPAIR_STATION:
                successMsg = "Repair station edited successfully";
                progressMsg = "Editing repair station";
                failureMsg = "Failed to edit repair station. Check internet connection";
                break;

        }

        startTransaction(successMsg, progressMsg, failureMsg, REQ_CODE_EDIT);

    }

    //region Transaction overloads ---------------------------------------------

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

        if (requestCode == REQ_CODE_ADD) {

            Destination destination = new Destination();
            destination.setDestType(getDestinationType());
            destination.setCylinderCount(0);
            destination.setName(getEnteredName());

            return new AddDestinationTransaction(destination);
        }

        if (requestCode == REQ_CODE_EDIT) {

            Destination destination=getDestination();
            destination.setName(getEnteredName());
            return new EditDestinationTransaction(destination);
        }

        return null;

    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);


        if (task.isSuccessful()) {
            Msg.show(requireContext(), successMsg);
            getDialog().dismiss();

        } else {
            Msg.show(requireContext(), failureMsg);
        }
    }

    //endregion ---------------------------------------------------------------------
}
