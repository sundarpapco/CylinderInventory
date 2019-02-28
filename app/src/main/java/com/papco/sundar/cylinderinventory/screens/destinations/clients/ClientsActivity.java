package com.papco.sundar.cylinderinventory.screens.destinations.clients;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.common.constants.DbPaths;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.screens.destinations.common.AddDestinationFragment;
import com.papco.sundar.cylinderinventory.screens.destinations.common.DestinationActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.common.DestinationAdapter;

import java.util.ArrayList;
import java.util.List;

public class ClientsActivity extends DestinationActivity {


    @Override
    public String getActivityTitle() {
        return "Clients";
    }

    @Override
    public void onShowEditDestinationDialog(Destination destination) {
        AddDestinationFragment fragment=new AddClientDialog();
        fragment.setArguments(AddDestinationFragment.getStartingArguments(true,destination));
        fragment.show(getSupportFragmentManager(),"editDestination");
    }

    @Override
    public void onShowNewDestinationDialog() {
        AddDestinationFragment fragment=new AddClientDialog();
        fragment.setArguments(AddDestinationFragment.getStartingArguments(false,null));
        fragment.show(getSupportFragmentManager(),"addDestination");
    }

    @Override
    protected int getDestinationType() {
        return Destination.TYPE_CLIENT;
    }

}
