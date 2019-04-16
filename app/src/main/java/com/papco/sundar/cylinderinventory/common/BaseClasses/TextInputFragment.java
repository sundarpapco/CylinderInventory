package com.papco.sundar.cylinderinventory.common.BaseClasses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TextInputFragment extends DialogFragment {

    private static final String KEY_HEADING = "key_heading_text";
    private static final String KEY_BUTTON_TEXT = "key_button_text";
    private static final String KEY_DEFAULT_TEXT = "key_default_text";

    public static Bundle getArguments(String heading, String defaultText, String buttonText) {

        Bundle args = new Bundle();
        args.putString(KEY_HEADING, heading);
        args.putString(KEY_DEFAULT_TEXT, defaultText);
        args.putString(KEY_BUTTON_TEXT, buttonText);

        return args;

    }

    private TextView heading;
    private EditText destinationName;
    private Button btnSave;
    private ProgressBar progressBar;

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

    protected void initViews(View view) {

        heading.setText(getHeading());
        destinationName.setText(getDefaultText());
        btnSave.setText(getKeyButtonText());
        btnSave.setOnClickListener(v -> {
            onOperation();
        });

    }

    protected void onOperation(){

    }

    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        destinationName.setEnabled(false);
        btnSave.setEnabled(false);
    }

    protected void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        destinationName.setEnabled(true);
        btnSave.setEnabled(true);
    }

    protected String getEnteredText(){
        return destinationName.getText().toString().trim();
    }

    private String getHeading(){

        Bundle args=getArguments();
        if(args==null)
            return "unKnown";

        return args.getString(KEY_HEADING,"unKnown");
    }

    private String getDefaultText(){

        Bundle args=getArguments();
        if(args==null)
            return "unKnown";

        return args.getString(KEY_DEFAULT_TEXT,"unKnown");
    }

    private String getKeyButtonText(){

        Bundle args=getArguments();
        if(args==null)
            return "unKnown";

        return args.getString(KEY_BUTTON_TEXT,"unKnown");
    }
}

