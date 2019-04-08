package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Aggregation;
import com.papco.sundar.cylinderinventory.data.CylinderType;

import java.util.ArrayList;
import java.util.List;

public class CylinderSummaryFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView total, full, empty, damaged, clients, repair, refill, graveyard;
    private ProgressBar progressBar;
    private ConstraintLayout detailsView;
    private ListenerRegistration listener, cylinderTypesListener;
    private QuerySnapshot loadedSnapshot;
    private AppCompatSpinner spinner;
    private ArrayAdapter<CylinderType> spinnerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cylinder_summary, container, false);
        linkViews(view);
        initViews(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        queryForDocuments("Global");

    }

    @Override
    public void onResume() {
        super.onResume();
        ((CylindersActivity) getActivity()).getSupportActionBar().setTitle("Cylinders summary");

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cylinders_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.mnu_manage_cylinder:
                ((CylindersActivity) getActivity()).showManageCylinderScreen();
                return true;

            case R.id.mnu_inactive_cylinder:
                ((CylindersActivity) getActivity()).showInActiveCylinderScreen();
                return true;

        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null)
            listener.remove();
        if (cylinderTypesListener != null)
            cylinderTypesListener.remove();
    }


    private void linkViews(View view) {

        spinner = view.findViewById(R.id.spinner);
        progressBar = view.findViewById(R.id.cyl_summary_progress_bar);
        detailsView = view.findViewById(R.id.cyl_summary_details);
        total = view.findViewById(R.id.cyl_summary_total_count);
        full = view.findViewById(R.id.cyl_summary_full_count);
        empty = view.findViewById(R.id.cyl_summary_empty_count);
        damaged = view.findViewById(R.id.cyl_summary_empty_damage_count);
        clients = view.findViewById(R.id.cyl_summary_clients_count);
        refill = view.findViewById(R.id.cyl_summary_refilling_count);
        repair = view.findViewById(R.id.cyl_summary_repair_count);
        graveyard = view.findViewById(R.id.cyl_summary_grave_count);

    }

    private void initViews(View view) {

        FloatingActionButton fab = view.findViewById(R.id.add_cyl_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CylindersActivity) getActivity()).showAddCylinderScreen();
            }
        });

        initSpinner();

    }

    private void initSpinner() {

        CylinderType defaultType = new CylinderType();
        defaultType.setName("Global");
        defaultType.setNoOfCylinders(0);

        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item,
                R.id.spinner_item_text);

        spinnerAdapter.add(defaultType);

        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                queryForDocuments(getSelectedCylinderType());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        loadCylinderTypesList();


    }

    private void showProgressBar() {
        detailsView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        detailsView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void loadCylinderTypesList() {

        if (cylinderTypesListener != null)
            cylinderTypesListener.remove();

        cylinderTypesListener = db.collection(DbPaths.COLLECTION_CYLINDER_TYPES)
                .orderBy("name")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Msg.show(getActivity(), "Error connecting to server. Please check internet connection");
                            return;
                        }

                        List<CylinderType> types = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                            types.add(documentSnapshot.toObject(CylinderType.class));
                        }

                        CylinderType defaultType = new CylinderType();
                        defaultType.setNoOfCylinders(0);
                        defaultType.setName("Global");
                        spinnerAdapter.clear();
                        spinnerAdapter.add(defaultType);

                        if (types.size() > 0)
                            spinnerAdapter.addAll(types);

                        queryForDocuments(getSelectedCylinderType());
                    }
                });


    }

    private void queryForDocuments(String cylinderType) {

        if (listener != null)
            listener.remove();

        Query query=getQueryForType(cylinderType);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        showProgressBar();

        listener = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    if (getActivity() != null)
                        Msg.show(requireContext(), "Error loading the summary data." +
                                " Check internet connection");
                }

                loadDetails(querySnapshot);

            }
        });
    }

    //used to construct the query to get the aggregation documents
    //pass -1 for getting the global cylinder aggregations
    private Query getQueryForType(String cylinderType) {

        if (cylinderType.equals("Global")) {
            return db.collection("aggregation").whereEqualTo("type", Aggregation.TYPE_CYLINDERS);
        }

        return db.collection(DbPaths.COLLECTION_CYLINDER_TYPES).document(cylinderType)
                .collection("aggregation");
    }

    private String getSelectedCylinderType(){

        return ((CylinderType)spinner.getSelectedItem()).getName();

    }

    private void loadDetails(QuerySnapshot querySnapshot) {

        total.setText("0");
        damaged.setText("0");
        full.setText("0");
        empty.setText("0");
        clients.setText("0");
        refill.setText("0");
        repair.setText("0");
        graveyard.setText("0");

        loadedSnapshot = querySnapshot;
        List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
        hideProgressBar();
        long totalCylinders=0;
        for (DocumentSnapshot document : documentSnapshots) {

            switch (document.getId()) {

                case "cylinders_damaged":
                    damaged.setText(asString(document.getLong("count")));
                    totalCylinders=totalCylinders+document.getLong("count");
                    break;

                case "cylinders_full":
                    full.setText(asString(document.getLong("count")));
                    totalCylinders=totalCylinders+document.getLong("count");
                    break;

                case "cylinders_empty":
                    empty.setText(asString(document.getLong("count")));
                    totalCylinders=totalCylinders+document.getLong("count");
                    break;

                case "cylinders_clients":
                    clients.setText(asString(document.getLong("count")));
                    totalCylinders=totalCylinders+document.getLong("count");
                    break;

                case "cylinders_refill_stations":
                    refill.setText(asString(document.getLong("count")));
                    totalCylinders=totalCylinders+document.getLong("count");
                    break;

                case "cylinders_repair_stations":
                    repair.setText(asString(document.getLong("count")));
                    totalCylinders=totalCylinders+document.getLong("count");
                    break;

                case "cylinders_graveyard":
                    graveyard.setText(asString(document.getLong("count")));
                    break;
            }

        }

        total.setText(Long.toString(totalCylinders));
        if(!getSelectedCylinderType().equals("Global")) {
            graveyard.setText("--");
        }

    }

    private String asString(long value) {

        return Long.toString(value);
    }


}
