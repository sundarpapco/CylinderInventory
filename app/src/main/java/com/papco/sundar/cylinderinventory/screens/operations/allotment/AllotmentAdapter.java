package com.papco.sundar.cylinderinventory.screens.operations.allotment;


import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;

import java.util.ArrayList;
import java.util.List;

public class AllotmentAdapter extends RecyclerView.Adapter<AllotmentAdapter.AllotmentVH> {

    private List<Allotment> data;
    private RecyclerListener<Allotment> callback;
    private Context context;

    public AllotmentAdapter(Context context, @NonNull RecyclerListener<Allotment> callback) {

        this.data = new ArrayList<>();
        this.callback = callback;
        this.context = context;

    }


    @NonNull
    @Override
    public AllotmentVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.batch_item, viewGroup, false);
        return new AllotmentVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllotmentVH allotmentVH, int i) {
        allotmentVH.bind();
    }

    @Override
    public int getItemCount() {

        return data.size();
    }

    public void setData(List<Allotment> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    class AllotmentVH extends RecyclerView.ViewHolder {

        private View colorView;
        private TextView status, destination, cylinderCount, timeStamp;


        public AllotmentVH(@NonNull final View itemView) {
            super(itemView);

            status = itemView.findViewById(R.id.batch_item_title);
            destination = itemView.findViewById(R.id.batch_item_destination_name);
            cylinderCount = itemView.findViewById(R.id.batch_item_no_of_cylinders);
            colorView = itemView.findViewById(R.id.view);
            timeStamp = itemView.findViewById(R.id.batch_item_timestamp);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onRecyclerItemClicked(data.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callback.onRecyclerItemLongClicked(data.get(getAdapterPosition()), getAdapterPosition(), itemView);
                    return true;
                }
            });
        }

        public void bind() {

            int roundColor = 1;
            Allotment allotment = data.get(getAdapterPosition());
            switch (allotment.getState()) {

                case Allotment.STATE_ALLOTTED:
                    status.setText("Waiting for approval");
                    roundColor = ContextCompat.getColor(context, R.color.allotment_waiting_for_approval);
                    break;

                case Allotment.STATE_APPROVED:
                    status.setText("Waiting for pickup");
                    roundColor = ContextCompat.getColor(context, R.color.allotment_waiting_for_pickup);
                    break;


                case Allotment.STATE_PICKED_UP:
                    status.setText("Picked up");
                    roundColor= ContextCompat.getColor(context,R.color.allotment_picked_up);
                    break;

                case Allotment.STATE_READY_FOR_INVOICE:
                    status.setText("Ready for Invoice");
                    roundColor= ContextCompat.getColor(context,R.color.allotment_ready_for_invoice);
                    break;
            }
            colorView.getBackground().setColorFilter(roundColor, PorterDuff.Mode.MULTIPLY);
            timeStamp.setText(allotment.getStringTimeStamp());
            destination.setText(allotment.getClientName());
            cylinderCount.setText(Integer.toString(allotment.getNumberOfCylinders()) + " Cylinders");
        }
    }
}
