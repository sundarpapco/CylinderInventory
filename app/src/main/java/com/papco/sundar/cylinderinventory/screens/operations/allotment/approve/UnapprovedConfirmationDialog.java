package com.papco.sundar.cylinderinventory.screens.operations.allotment.approve;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class UnapprovedConfirmationDialog extends DialogFragment {

    private RadioButton radioIgnore;
    private RadioButton radioApproveLater;
    private Button btnApprove;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view=getActivity().getLayoutInflater().inflate(R.layout.unalloted_approval_confirmation,null);
        linkViews(view);
        initViews(view);

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void linkViews(View view) {

        radioIgnore=view.findViewById(R.id.radio_button_ignore);
        radioApproveLater=view.findViewById(R.id.radio_button_allot_later);
        btnApprove=view.findViewById(R.id.unalloted_conf_btn_approve);

    }

    private void initViews(View view) {

        radioApproveLater.setChecked(true);
        btnApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(radioApproveLater.isChecked())
                    ((ApproveAllotmentActivity)getActivity()).onApproveAllotment(false);
                else
                    ((ApproveAllotmentActivity)getActivity()).onApproveAllotment(true);

                getDialog().dismiss();
            }
        });

    }
}
