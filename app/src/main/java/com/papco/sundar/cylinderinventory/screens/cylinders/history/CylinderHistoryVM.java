package com.papco.sundar.cylinderinventory.screens.cylinders.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class CylinderHistoryVM extends AndroidViewModel {

    private CylHistoryDataSource historyDataSource;

    public CylinderHistoryVM(@NonNull Application application) {
        super(application);
    }

    CylHistoryDataSource getHistoryDataSource() {

        if(historyDataSource==null)
            historyDataSource=new CylHistoryDataSource(getApplication());

        return historyDataSource;

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        historyDataSource=null;
    }
}
