package com.papco.sundar.cylinderinventory.screens.operations.allotment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Transaction;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.BaseTransaction;
import com.papco.sundar.cylinderinventory.common.BaseClasses.TransactionActivity;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.TransactionRunnerService;
import com.papco.sundar.cylinderinventory.logic.Transactions.CreateAllotmentTransaction;

public class CreateAllotmentActivity extends TransactionActivity {

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
    private ProgressBar progressBar;

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
        progressBar=findViewById(R.id.create_allot_progress_bar);

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

        String successMsg="Alloment created successfully";
        String progressMsg="Creating allotment";
        String failedMsg="Error creating allotment";

        if(isValidCylinderCount())
            startTransaction(successMsg,progressMsg,failedMsg,1);
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

    @Override
    public void showTransactionProgressBar() {

        progressBar.setVisibility(View.VISIBLE);
        if(TransactionRunnerService.isRunning())
            btnSave.setEnabled(false);

    }

    @Override
    public void hideTransactionProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        btnSave.setEnabled(true);
    }

    @Override
    public BaseTransaction getTransactionToRun(int requestCode) {

        int noOfCylinders=Integer.parseInt(cylindersField.getText().toString().trim());
        Destination destination=new Destination();
        destination.setId(getDestinationId());
        destination.setName(getDestinationName());
        return new CreateAllotmentTransaction(noOfCylinders,destination);
    }

    @Override
    public void onTransactionComplete(Task<Void> task, int requestCode) {
        super.onTransactionComplete(task, requestCode);

        if(task.isSuccessful()){
            Msg.show(this,"Allotment created succesfully");
            finish();
        }else{
            Msg.show(this,"Error creating allotment. Check interner connection");
        }
    }
}
