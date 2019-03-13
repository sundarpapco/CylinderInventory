package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.BaseClasses.ConnectivityActivity;
import com.papco.sundar.cylinderinventory.screens.cylinders.inactiveCylinders.InActiveCylindersFragment;

public class CylindersActivity extends ConnectivityActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);
        linkViews();
        setupToolBar();
        initViews();
        if(savedInstanceState==null)
            loadInitialFragment();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Boolean result=false;

        switch (item.getItemId()){
            case android.R.id.home:
                if(getSupportFragmentManager().getBackStackEntryCount()==0)
                    finish();
                else
                    popBackStack();
                result=true;
                break;
        }

        return result;
    }


    private void linkViews(){



    }

    private void setupToolBar() {

        Toolbar toolbar=findViewById(R.id.holder_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

    }

    private void initViews(){



    }



    private void loadInitialFragment(){

        Fragment summeryFragment=new CylinderSummaryFragment();
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.fragment_container,summeryFragment);
        transaction.commit();

    }

    public void showAddCylinderScreen(){

        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.fragment_container,new AddCylinderFragment());
        transaction.addToBackStack("NEW CYLINDERS");
        transaction.commit();

    }

    public void showManageCylinderScreen(){

        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.fragment_container,new ManageCylinderFragment());
        transaction.addToBackStack("Manage cylinders");
        transaction.commit();

    }

    public void showInActiveCylinderScreen(){

        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.fragment_container,new InActiveCylindersFragment());
        transaction.addToBackStack("Inactive cylinders");
        transaction.commit();

    }

    public void popBackStack(){

        getSupportFragmentManager().popBackStack();
    }
}
