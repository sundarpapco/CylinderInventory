package com.papco.sundar.cylinderinventory.screens.operations.allotment.approve;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ApprovalConfirmationDialog extends DialogFragment {

    private Button btnApprove;
    private ProgressBar progressBar;
    private TextView msgView,titleView;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.alert_fragment, null);
        linkViews(view);
        initViews();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();

    }

    private void linkViews(View view) {

        btnApprove=view.findViewById(R.id.alert_fragment_btnPositive);
        progressBar=view.findViewById(R.id.alert_fragment_progressBar);
        msgView=view.findViewById(R.id.alert_fragment_message);
        titleView=view.findViewById(R.id.alert_fragment_heading);
    }

    private void initViews() {

        titleView.setText("Approve");
        msgView.setText("Approve all the cylinders?");
        btnApprove.setText("APPROVE");
        btnApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((ApproveAllotmentActivity) getActivity()).onApproveAllotment(true);
                getDialog().dismiss();
            }
        });

    }
}
