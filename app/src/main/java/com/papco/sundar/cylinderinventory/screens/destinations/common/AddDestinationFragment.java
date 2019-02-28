package com.papco.sundar.cylinderinventory.screens.destinations.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.AddDestinationTransaction;

public class AddDestinationFragment extends DialogFragment {

    public static Bundle getStartingArguments(boolean isEditing, Destination destination){

        Bundle arguments=new Bundle();
        arguments.putBoolean(KEY_MODE,isEditing);
        if(destination!=null){
            arguments.putInt("destId",destination.getId());
            arguments.putString("destName",destination.getName());
            arguments.putInt("destType",destination.getDestType());
            arguments.putInt("destCylCount",destination.getCylinderCount());
        }
        return arguments;

    }

    private static final String KEY_MODE="isEditing";

    TextView heading;
    EditText destinationName;
    Button btnSave;
    Destination editingDestination;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view=getActivity().getLayoutInflater().inflate(R.layout.new_destination,null);
        linkViews(view);
        initViews(view);

        AlertDialog.Builder builder=new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();
    }


    private void linkViews(View view) {

        heading=view.findViewById(R.id.dest_new_heading);
        destinationName=view.findViewById(R.id.dest_new_name);
        btnSave=view.findViewById(R.id.dest_new_btnSave);
    }

    private void initViews(View view) {

        heading.setText(getTitle());
        if(isEditingMode() && getDestination()!=null){
            destinationName.setText(getDestination().getName());
        }else
            destinationName.setText("");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameIsValid())
                    onSaveDestination();
                else
                    showMessage("Please enter a valid name");

            }
        });

    }

    public String getTitle(){
        return "Destination name";
    }

    private boolean nameIsValid(){

        if(TextUtils.isEmpty(destinationName.getText().toString().trim()))
            return false;
        else
            return true;
    }

    protected String getEnteredName(){

        return destinationName.getText().toString().trim();
    }

    public void onSaveDestination(){

        if(isEditingMode()){

        }else{

            addDestination();
        }

    }

    public boolean isEditingMode(){

        if(getArguments()!=null)
            return getArguments().getBoolean(KEY_MODE,false);

        return false;
    }

    protected Destination getDestination(){

        if(getArguments()==null)
            return null;

        if(getArguments().getInt("destId",-1)==-1)
            return null;

        if(editingDestination==null){

            editingDestination=new Destination();
            Bundle arg=getArguments();

            editingDestination.setId(arg.getInt("destId"));
            editingDestination.setName(arg.getString("destName"));
            editingDestination.setDestType(arg.getInt("destType"));
            editingDestination.setCylinderCount(arg.getInt("destCylCount"));

        }

        return editingDestination;
    }

    protected int getDestinationType(){

        return Destination.TYPE_WAREHOUSE;
    }

    private void addDestination() {

        FirebaseFirestore db=FirebaseFirestore.getInstance();

        Destination destination=new Destination();
        destination.setDestType(getDestinationType());
        destination.setCylinderCount(0);
        destination.setName(getEnteredName());
        db.runTransaction(new AddDestinationTransaction(destination))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            if(getActivity()!=null)
                                Msg.show(requireContext(),"Added successfully");

                            if(getDialog()!=null)
                                getDialog().dismiss();

                        }else{

                            if(getActivity()!=null)
                                Msg.show(requireContext(),"Error in adding. Check internet connection");
                        }

                    }
                });

    }

    protected void showMessage(String msg){

        Toast.makeText(requireActivity(),msg,Toast.LENGTH_SHORT).show();
    }
}
