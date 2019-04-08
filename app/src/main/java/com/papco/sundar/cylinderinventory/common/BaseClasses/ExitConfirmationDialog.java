package com.papco.sundar.cylinderinventory.common.BaseClasses;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ExitConfirmationDialog extends DialogFragment {

    private static final String KEY_TITLE="key_title";
    private static final String KEY_MESSAGE="key_message";
    private static final String KEY_BUTTON_TEXT="button_text";

    public static Bundle getArguments(String title,String msg,String btnMsg){

        Bundle args=new Bundle();
        args.putString(KEY_TITLE,title);
        args.putString(KEY_MESSAGE,msg);
        args.putString(KEY_BUTTON_TEXT,btnMsg);

        return args;

    }

    private String defaultTitle="Exit";
    private String defaultMessage="Are you sure want to exit?";
    private String defaultBtnText="EXIT";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(getTitle());
        builder.setMessage(getMessage());
        builder.setPositiveButton(getBtnText(), (dialog, which) -> getActivity().finish());
        builder.setNegativeButton("CANCEL",null);
        return builder.create();

    }

    private String getMessage(){

        Bundle args=getArguments();

        if(args==null)
            return defaultMessage;

        return args.getString(KEY_MESSAGE,defaultMessage);


    }

    private String getTitle(){

        Bundle args=getArguments();

        if(args==null)
            return defaultTitle;

        return args.getString(KEY_TITLE,defaultTitle);


    }

    private String getBtnText(){

        Bundle args=getArguments();

        if(args==null)
            return defaultBtnText;

        return args.getString(KEY_MESSAGE,defaultBtnText);


    }

}
