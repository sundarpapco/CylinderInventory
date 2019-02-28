package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Destination;

public class CreateAllotmentActivity extends AppCompatActivity {

    public static void start(Context context, @NonNull Destination destination){

        Bundle bundle=new Bundle();
        bundle.putInt("destinationId",destination.getId());
        bundle.putString("destinationName",destination.getName());

        Intent intent=new Intent(context, CreateAllotmentActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);

    }


    private TextInputEditText clientField;
    private TextInputEditText cylindersField;
    private Button btnSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_allotment);
        linkViews();
        initViews();
        setupToolBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void linkViews() {

        clientField=findViewById(R.id.create_allot_client_name);
        cylindersField=findViewById(R.id.create_allot_cyl_count);
        btnSave=findViewById(R.id.create_allot_btnSave);

    }

    private void initViews() {

        clientField.setText(getDestinationName());
        clientField.setKeyListener(null);
        cylindersField.requestFocus();
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateAllotment();
            }
        });

    }

    private void setupToolBar() {

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }

    protected void onCreateAllotment(){

        // TODO: 25-02-2019 Create the allotment here

        if(isValidCylinderCount())
            Msg.show(this,"Create allotment here");
    }

    private boolean isValidCylinderCount(){

        boolean result=true;

        if(TextUtils.isEmpty(cylindersField.getText().toString()))
            result=false;
        else {

            Integer number = Integer.parseInt(cylindersField.getText().toString());
            if (number <= 0)
                result = false;
        }

        if(!result)
            Msg.show(this,"Invalid cylinder count");

        return result;

    }

    protected int getDestinationId(){

        if(getIntent().getExtras()==null)
            return -1;

        return getIntent().getExtras().getInt("destinationId",-1);
    }

    protected String getDestinationName(){

        if(getIntent().getExtras()==null)
            return "";

        return getIntent().getExtras().getString("destinationName","");
    }

    private void showKeyboard(){

        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(cylindersField, 0);
    }
}
