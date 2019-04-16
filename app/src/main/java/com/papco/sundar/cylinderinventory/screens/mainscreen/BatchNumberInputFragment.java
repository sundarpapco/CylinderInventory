package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.papco.sundar.cylinderinventory.common.BaseClasses.TextInputFragment;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.screens.batchDetail.BatchDetailActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

public class BatchNumberInputFragment extends TextInputFragment {

    private MainActivityVM viewModel;
    private boolean isLoading=false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null)
            isLoading=savedInstanceState.getBoolean("isLoading");

        viewModel= ViewModelProviders.of(getActivity()).get(MainActivityVM.class);
        viewModel.getSearchedBatch().observe(this, batch -> {
            if(!isLoading)
                return;

            isLoading=false;

            if(batch==null){
                hideProgressBar();
                Msg.show(getActivity(),"Document not found");
                return;
            }
            else{
                launchBatchDetailActivity(batch);
                if(getDialog()!=null)
                    getDialog().dismiss();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isLoading",isLoading);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        if(isLoading)
            showProgressBar();
    }

    @Override
    protected void onOperation() {

        if(getActivity()==null)
            return;

        if (isValidBatchNumber(getEnteredText())) {
            isLoading=true;
            showProgressBar();
            viewModel.searchBatch(getEnteredText().trim());
        }else
            Msg.show(getActivity(), "Please enter a valid document name");
    }

    private void launchBatchDetailActivity(Batch batch){

        if(getActivity()!=null)
            BatchDetailActivity.start(getActivity(),batch);

    }

    private boolean isValidBatchNumber(String enteredText) {


        if (enteredText.length() < 5)
            return false;

        String batchPart = enteredText.substring(0, 4);

        switch (batchPart) {

            case "inv-":
                break;

            case "ecr-":
                break;

            case "ref-":
                break;

            case "fci-":
                break;

            case "rep-":
                break;

            case "rci-":
                break;

            default:
                return false;

        }


        String numberPart = enteredText.substring(4, enteredText.length());
        if (TextUtils.isEmpty(numberPart))
            return false;

        if (!TextUtils.isDigitsOnly(numberPart))
            return false;


        long longBatchNumber = Long.parseLong(numberPart);
        if (longBatchNumber > Integer.MAX_VALUE)
            return false;

        return true;
    }
}
