package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.papco.sundar.cylinderinventory.R;

public class InActiveCylindersFragment extends Fragment {

    private View clickSense;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.inactive_cylinders_list,container,false);
        linkViews(view);
        initViews(view);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        ((CylindersActivity)getActivity()).getSupportActionBar().setTitle("Inactive cylinders");
    }

    private void linkViews(View view){

        clickSense= view.findViewById(R.id.inactive_cyl_click_sense);
    }

    private void initViews(View view){
        clickSense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                loadList("");
            }
        });

        EditText searchView=view.findViewById(R.id.inactive_cyl_search);
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId== EditorInfo.IME_ACTION_SEARCH){
                    hideKeyboard();
                    loadList("");

                }

                return false;
            }
        });

    }

    private void loadList(String cylNo){

        // TODO: 21-02-2019 load the inactive list here
        showMessage("Load the list here");

    }

    private void showMessage(String msg){

        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard(){

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(clickSense.getWindowToken(), 0);
    }
}
