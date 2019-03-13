package com.papco.sundar.cylinderinventory.screens.cylinders.inactiveCylinders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.data.Destination;

import java.util.Calendar;
import java.util.List;

public class InactiveCylindersAdapter extends RecyclerView.Adapter<InactiveCylindersAdapter.InactiveVH> {

    private List<Cylinder> data;
    private final long oneDayInMilliSecond = 86400000;
    private long currentTime;

    public InactiveCylindersAdapter(List<Cylinder> data) {
        this.data = data;
    }


    @NonNull
    @Override
    public InactiveVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inactive_cylinder_item, viewGroup, false);
        return new InactiveVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InactiveVH inactiveVH, int i) {
        inactiveVH.bind();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Cylinder> data) {
        currentTime = Calendar.getInstance().getTimeInMillis();
        this.data = data;
        notifyDataSetChanged();
    }

    class InactiveVH extends RecyclerView.ViewHolder {

        TextView inactiveDays, cylinderNumber, location, status;

        public InactiveVH(@NonNull View itemView) {
            super(itemView);

            inactiveDays = itemView.findViewById(R.id.inactive_item_days);
            cylinderNumber = itemView.findViewById(R.id.inactive_item_cyl_no);
            location = itemView.findViewById(R.id.inactive_item_location);
            status = itemView.findViewById(R.id.inactive_item_status);
        }

        public void bind() {

            Cylinder cylinder = data.get(getAdapterPosition());
            int inactiveDays;
            cylinderNumber.setText("Cylinder number: " + cylinder.getStringId());
            location.setText(cylinder.getLocationName());
            inactiveDays = (int) ((currentTime - cylinder.getLastTransaction()) / oneDayInMilliSecond);
            this.inactiveDays.setText(Integer.toString(inactiveDays) + " Days");

            if (cylinder.isDamaged()) {
                if (cylinder.getLocationId() == Destination.TYPE_WAREHOUSE)
                    status.setText("DAMAGED");
                else
                    status.setText("SENT FOR REPAIR");

                return;
            }


            if (cylinder.isEmpty())
                if (cylinder.getLocationId() == Destination.TYPE_WAREHOUSE)
                    status.setText("EMPTY");
                else
                    status.setText("SENT FOR REFILLING");
            else if (cylinder.getLocationId() != Destination.TYPE_WAREHOUSE)
                status.setText("WITH CLIENT");
            else
                status.setText("FULL");
        }
    }
}
