package com.papco.sundar.cylinderinventory.common;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.Toast;

public class Msg {

    public static void show(@NonNull Context context,String msg){

        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();

    }
}
