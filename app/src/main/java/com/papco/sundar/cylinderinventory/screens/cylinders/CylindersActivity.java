package com.papco.sundar.cylinderinventory.screens.cylinders;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.papco.sundar.cylinderinventory.R;

public class CylindersActivity extends AppCompatActivity {



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
