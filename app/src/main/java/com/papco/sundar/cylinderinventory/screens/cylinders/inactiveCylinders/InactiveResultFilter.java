package com.papco.sundar.cylinderinventory.screens.cylinders.inactiveCylinders;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class InactiveResultFilter extends AsyncTask<QuerySnapshot, Void, List<Cylinder>> {


    private WeakReference<InactiveFilterResult> callback;
    private FilterType filter;


    public InactiveResultFilter(InactiveFilterResult callback, @NonNull FilterType filter) {

        if (callback != null)
            this.callback = new WeakReference<>(callback);
        else
            this.callback = new WeakReference<>(null);

        this.filter = filter;
    }

    @Override
    protected List<Cylinder> doInBackground(QuerySnapshot... querySnapshots) {

        List<Cylinder> result = new ArrayList<>();

        Cylinder cylinder;
        for (DocumentSnapshot snapshot : querySnapshots[0].getDocuments()) {

            cylinder = snapshot.toObject(Cylinder.class);

            if (filter == FilterType.ALL)
                result.add(cylinder);
            else if (getCylinderLocation(cylinder) == filter)
                result.add(cylinder);

        }

        return result;
    }

    @Override
    protected void onPostExecute(List<Cylinder> cylinders) {

        if (callback != null && callback.get() != null) {
            callback.get().onInactiveResultFiltered(cylinders);
        }

    }

    private FilterType getCylinderLocation(Cylinder cylinder) {

        if (cylinder.getLocationId() == Destination.TYPE_WAREHOUSE)
            return FilterType.WAREHOUSE;

        if (cylinder.isDamaged())
            return FilterType.REPAIR_STATIONS;

        if (cylinder.isEmpty())
            return FilterType.REFILL_STATIONS;

        return FilterType.CLIENTS;

    }

    public enum FilterType {

        ALL,
        WAREHOUSE,
        CLIENTS,
        REFILL_STATIONS,
        REPAIR_STATIONS;
    }

    public interface InactiveFilterResult {

        void onInactiveResultFiltered(List<Cylinder> resultList);
    }
}
