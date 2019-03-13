package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;

import java.util.List;

public class CylinderSummaryFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView total, full, empty, damaged, clients, repair, refill, graveyard;
    private ProgressBar progressBar;
    private ConstraintLayout detailsView;
    private ListenerRegistration listener;
    private QuerySnapshot loadedSnapshot;

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

        queryForDocuments();

    }

    private void queryForDocuments() {

        if (listener != null)
            listener.remove();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        showProgressBar();

        listener = db.collection("aggregation")
                .whereEqualTo("type", 1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
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

    private void linkViews(View view) {

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

    private void showProgressBar() {
        detailsView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        detailsView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }


    private void initViews(View view) {

        FloatingActionButton fab = view.findViewById(R.id.add_cyl_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CylindersActivity) getActivity()).showAddCylinderScreen();
            }
        });

    }

    private void loadDetails(QuerySnapshot querySnapshot) {

        loadedSnapshot = querySnapshot;
        List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
        hideProgressBar();
        for (DocumentSnapshot document : documentSnapshots) {

            switch (document.getId()) {

                case "cylinders_total":
                    total.setText(asString(document.getLong("count")));
                    break;

                case "cylinders_damaged":
                    damaged.setText(asString(document.getLong("count")));
                    break;

                case "cylinders_full":
                    full.setText(asString(document.getLong("count")));
                    break;

                case "cylinders_empty":
                    empty.setText(asString(document.getLong("count")));
                    break;

                case "cylinders_clients":
                    clients.setText(asString(document.getLong("count")));
                    break;

                case "cylinders_refill_stations":
                    refill.setText(asString(document.getLong("count")));
                    break;

                case "cylinders_repair_stations":
                    repair.setText(asString(document.getLong("count")));
                    break;

                case "cylinders_graveyard":
                    graveyard.setText(asString(document.getLong("count")));
                    break;
            }

        }


    }


    private String asString(long value) {

        return Long.toString(value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null)
            listener.remove();
    }
}
