package com.papco.sundar.cylinderinventory.common.BaseClasses;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class DeleteConfirmationDialog extends TransactionDialogFragment {

    private TextView heading,msg;
    private Button btn;
    private ProgressBar progressBar;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view=getActivity().getLayoutInflater().inflate(R.layout.delete_confirmation_dialog,null);
        linkViews(view);
        initViews();

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();

    }

    private void linkViews(View view) {

        heading=view.findViewById(R.id.del_conf_heading);
        msg=view.findViewById(R.id.del_conf_msg);
        btn=view.findViewById(R.id.del_conf_btn);
        progressBar=view.findViewById(R.id.del_conf_progressBar);
    }

    private void initViews() {

        heading.setText(getHeading());
        msg.setText(getMessage());
        btn.setText(getButtonText());
        btn.setOnClickListener(v -> onButtonClicked());
    }

    protected String getHeading(){

        return "Delete";
    }

    protected String getMessage(){

        return "Are you sure want to delete?";
    }

    protected String getButtonText(){

        return "DELETE";
    }

    protected void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
        btn.setEnabled(false);
    }

    protected void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
        btn.setEnabled(true);
    }

    protected void onButtonClicked(){

    }

}
