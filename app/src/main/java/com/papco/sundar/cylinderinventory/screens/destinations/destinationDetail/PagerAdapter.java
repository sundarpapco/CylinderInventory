package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail;

import com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.cylinderList.DestinationCylinderListFragment;
import com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.historyList.DestinationHistoryFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {

    Fragment cylinderFragment,historyFragment;

    public PagerAdapter(@NonNull FragmentManager fm,int destId) {
        super(fm);
        cylinderFragment=new DestinationCylinderListFragment();
        cylinderFragment.setArguments(DestinationCylinderListFragment.getArguments(destId));
        historyFragment=new DestinationHistoryFragment();
        historyFragment.setArguments(DestinationHistoryFragment.getArguments(destId));
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                return cylinderFragment;

            case 1:
                return historyFragment;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){

            case 0:
                return "Cylinders";

            case 1:
                return "History";
        }


        return "Unknown";
    }
}
