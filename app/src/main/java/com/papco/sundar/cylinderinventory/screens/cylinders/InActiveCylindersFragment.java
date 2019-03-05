package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.screens.mainscreen.BatchFeedAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class InActiveCylindersFragment extends Fragment {

    private View clickSense;
    private final long oneDayInMilliSecond=86400000;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextInputEditText searchField;
    private InactiveCylindersAdapter adapter;
    private int enteredDays=-1;
    private ListenerRegistration listenerRegistration;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.inactive_cylinders_list,container,false);
        linkViews(view);
        initViews(view);
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        if(enteredDays!=-1)
            loadList();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((CylindersActivity)getActivity()).getSupportActionBar().setTitle("Inactive cylinders");
    }

    private void linkViews(View view){

        clickSense= view.findViewById(R.id.inactive_cyl_click_sense);
        progressBar=view.findViewById(R.id.inactive_cyl_progress);
        recyclerView=view.findViewById(R.id.inactive_recycler);
        searchField=view.findViewById(R.id.inactive_cyl_search);
    }

    private void initViews(View view){
        clickSense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if(isValidDate())
                    loadList();
            }
        });

        searchField=view.findViewById(R.id.inactive_cyl_search);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId== EditorInfo.IME_ACTION_SEARCH){
                    hideKeyboard();
                    if(isValidDate())
                        loadList();
                }

                return false;
            }
        });

        SpacingDecoration decoration=new SpacingDecoration(requireContext(),SpacingDecoration.VERTICAL,18,12,24);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter=new InactiveCylindersAdapter(new ArrayList<Cylinder>());
        recyclerView.setAdapter(adapter);

    }

    private boolean isValidDate(){

        if(enteredDays!=-1){
            return false;
        }

        String enteredString=searchField.getText().toString().trim();
        if(TextUtils.isEmpty(enteredString))
            return false;

        int enteredDays=Integer.parseInt(enteredString);
        if(enteredDays==0)
            return false;

        return true;


    }

    private void loadList(){

        /*if(enteredDays<7){
            Msg.show(requireContext(),"Minimum 7 days required else the result will be huge");
            return;
        }*/

        if(listenerRegistration!=null)
            listenerRegistration.remove();

        if(enteredDays==-1)
            enteredDays=Integer.parseInt(searchField.getText().toString().trim());

        showProgressBar();
        setSubTitle("");
        long searchTime= Calendar.getInstance().getTimeInMillis();
        searchTime=searchTime-(oneDayInMilliSecond*enteredDays);

        //Log.d("SUNDAR", Integer.toString(enteredDays));
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        listenerRegistration=db.collection(DbPaths.COLLECTION_CYLINDERS)
                .whereLessThan("lastTransaction",searchTime)
                .orderBy("lastTransaction")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        listenerRegistration.remove();
                        enteredDays=-1;

                        if(e!=null){
                            Msg.show(InActiveCylindersFragment.this.requireContext()
                                    ,"Error fetching data. Please check internet connection");
                            return;
                        }else {

                            Msg.show(InActiveCylindersFragment.this.getContext(),Integer.toString(querySnapshot.getDocuments().size()));
                            List<Cylinder> cylinders = new ArrayList<>();
                            for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                                cylinders.add(snapshot.toObject(Cylinder.class));
                            }
                            if (cylinders.size() == 0)
                                setSubTitle("");
                            else
                                setSubTitle(Integer.toString(cylinders.size()) + " cylinders");
                            adapter.setData(cylinders);

                        }

                        hideProgressBar();
                    }
                });

    }

    private void setSubTitle(String subTitle){

        ActionBar actionBar=((CylindersActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private void showProgressBar(){

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

    }

    private void hideProgressBar(){

        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onStop() {
        super.onStop();

        if(listenerRegistration!=null)
            listenerRegistration.remove();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ActionBar actionBar=((CylindersActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("");
    }

    private void hideKeyboard(){

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(clickSense.getWindowToken(), 0);
    }
}
