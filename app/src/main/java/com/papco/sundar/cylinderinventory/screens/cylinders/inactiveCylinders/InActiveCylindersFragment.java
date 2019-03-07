package com.papco.sundar.cylinderinventory.screens.cylinders.inactiveCylinders;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.SpacingDecoration;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.logic.ConnectionMonitor;
import com.papco.sundar.cylinderinventory.screens.cylinders.CylindersActivity;
import com.papco.sundar.cylinderinventory.screens.cylinders.inactiveCylinders.InactiveResultFilter.FilterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class InActiveCylindersFragment extends Fragment {

    private View clickSense;
    private final long oneDayInMilliSecond = 86400000;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextInputEditText searchField;
    private TextInputEditText filterField;
    private InactiveCylindersAdapter adapter;
    private boolean loadingData = false;
    private ListenerRegistration listenerRegistration;
    private TextInputLayout filterFieldLayout;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            loadingData = savedInstanceState.getBoolean("loadingData", false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("loadingData", loadingData);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.inactive_cylinders_list, container, false);
        linkViews(view);
        initViews(view);
        return view;

    }


    @Override
    public void onResume() {
        super.onResume();
        ((CylindersActivity) getActivity()).getSupportActionBar().setTitle("Inactive cylinders");
    }

    private void linkViews(View view) {

        clickSense = view.findViewById(R.id.inactive_cyl_click_sense);
        progressBar = view.findViewById(R.id.inactive_cyl_progress);
        recyclerView = view.findViewById(R.id.inactive_recycler);
        searchField = view.findViewById(R.id.inactive_cyl_search);
        filterField = view.findViewById(R.id.inactive_filter);
        filterFieldLayout=view.findViewById(R.id.inactive_filter_layout);
    }

    private void initViews(View view) {
        clickSense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                validateAndLoadList();
            }
        });

        searchField = view.findViewById(R.id.inactive_cyl_search);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    validateAndLoadList();
                }

                return false;
            }
        });

        filterField.setKeyListener(null);
        filterField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterMenu(v);
            }
        });


        SpacingDecoration decoration = new SpacingDecoration(requireContext(), SpacingDecoration.VERTICAL, 18, 12, 24);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InactiveCylindersAdapter(new ArrayList<Cylinder>());
        recyclerView.setAdapter(adapter);

    }

    private void showFilterMenu(View view) {

        final FilterType currentFilter = getSelectedFilter();
        final PopupMenu menu = new PopupMenu(getActivity(), view);
        menu.getMenuInflater().inflate(R.menu.mnu_filter_type, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.mnu_filter_type_all:
                        filterField.setText("All");
                        break;

                    case R.id.mnu_filter_type_clients:
                        filterField.setText("Clients");
                        break;

                    case R.id.mnu_filter_type_warehouse:
                        filterField.setText("Warehouse");
                        break;

                    case R.id.mnu_filter_refill_stations:
                        filterField.setText("Refill stations");
                        break;

                    case R.id.mnu_filter_repair_stations:
                        filterField.setText("Repair stations");
                        break;
                }

                if (currentFilter != getSelectedFilter())
                    validateAndLoadList();

                return true;
            }
        });
        menu.show();
    }

    private FilterType getSelectedFilter() {

        String stringFilter = filterField.getText().toString();

        switch (stringFilter) {

            case "All":
                return FilterType.ALL;

            case "Clients":
                return FilterType.CLIENTS;

            case "Warehouse":
                return FilterType.WAREHOUSE;

            case "Refill stations":
                return FilterType.REFILL_STATIONS;

            case "Repair stations":
                return FilterType.REPAIR_STATIONS;
        }

        return FilterType.ALL;

    }

    private void validateAndLoadList() {


        if (!loadingData && isValidDate())
            if (ConnectionMonitor.isInternetConnected(getActivity()))
                loadList();
            else
                Msg.show(getActivity(), "Please check internet connection");

    }

    private boolean isValidDate() {

        String enteredString = searchField.getText().toString().trim();
        if (TextUtils.isEmpty(enteredString))
            return false;

        int enteredDays = Integer.parseInt(enteredString);
        if (enteredDays == 0)
            return false;

        return true;


    }


    private void loadList() {


        /*if(enteredDays<7){
            Msg.show(requireContext(),"Minimum 7 days required else the result will be huge");
            return;
        }*/

        //final Context context=getActivity().getApplicationContext();
        showProgressBar();
        if (listenerRegistration != null)
            listenerRegistration.remove();
        final Context context = getActivity().getApplicationContext();
        long searchTime = Calendar.getInstance().getTimeInMillis();
        int enteredDays = Integer.parseInt(searchField.getText().toString());
        searchTime = searchTime - (oneDayInMilliSecond * enteredDays);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        listenerRegistration = db.collection(DbPaths.COLLECTION_CYLINDERS)
                .whereLessThan("lastTransaction", searchTime)
                .orderBy("lastTransaction")
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Msg.show(context, "Error fetching data. Please check internet connection");
                            return;
                        }

                        if (!querySnapshot.getMetadata().isFromCache()) {


                            new InactiveResultFilter(new InactiveResultFilter.InactiveFilterResult() {
                                @Override
                                public void onInactiveResultFiltered(List<Cylinder> cylinders) {
                                    if (cylinders.size() == 0)
                                        setSubTitle("");
                                    else
                                        setSubTitle(Integer.toString(cylinders.size()) + " cylinders");
                                    adapter.setData(cylinders);
                                    hideProgressBar();

                                }
                            }, getSelectedFilter()).execute(querySnapshot);

                        } else {
                            adapter.setData(new ArrayList<Cylinder>());
                            setSubTitle("");
                        }

                    }
                });

    }

    private void setSubTitle(String subTitle) {

        ActionBar actionBar = ((CylindersActivity) getActivity()).getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private void showProgressBar() {

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        loadingData = true;

    }

    private void hideProgressBar() {

        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        loadingData = false;

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ActionBar actionBar = ((CylindersActivity) getActivity()).getSupportActionBar();
        actionBar.setSubtitle("");
        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(clickSense.getWindowToken(), 0);
    }
}
