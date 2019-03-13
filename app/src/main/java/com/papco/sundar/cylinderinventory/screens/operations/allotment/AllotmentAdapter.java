package com.papco.sundar.cylinderinventory.screens.operations.allotment;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.NonNull;
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
    private GradientDrawable round;

    public AllotmentAdapter(Context context,@NonNull RecyclerListener<Allotment> callback) {

        this.data = new ArrayList<>();
        this.callback = callback;
        round=(GradientDrawable)context.getResources().getDrawable(R.drawable.batch_item_round);

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

    public void setData(List<Allotment> data){
        this.data=data;
        Log.d("SUNDAR", "getItemCount: "+Integer.toString(this.data.size()));
        notifyDataSetChanged();
    }

    class AllotmentVH extends RecyclerView.ViewHolder {

        private View colorView;
        private TextView status, destination, cylinderCount,timeStamp;


        public AllotmentVH(@NonNull final View itemView) {
            super(itemView);

            status = itemView.findViewById(R.id.batch_item_title);
            destination = itemView.findViewById(R.id.batch_item_destination_name);
            cylinderCount = itemView.findViewById(R.id.batch_item_no_of_cylinders);
            colorView=itemView.findViewById(R.id.view);
            timeStamp=itemView.findViewById(R.id.batch_item_timestamp);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onRecyclerItemClicked(data.get(getAdapterPosition()),getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callback.onRecyclerItemLongClicked(data.get(getAdapterPosition()),getAdapterPosition(),itemView);
                    return true;
                }
            });
        }

        public void bind() {

            Allotment allotment = data.get(getAdapterPosition());
            switch (allotment.getState()) {

                case Allotment.STATE_ALLOTTED:
                    status.setText("Alloted");
                    colorView.setBackgroundResource(R.drawable.round_blue);
                    break;

                case Allotment.STATE_PICKED_UP:
                    status.setText("Picked up");
                    colorView.setBackgroundResource(R.drawable.round_orange);
                    break;

                case Allotment.STATE_READY_FOR_INVOICE:
                    status.setText("Ready for Invoice");
                    colorView.setBackgroundResource(R.drawable.batch_item_round);
                    break;
            }
            timeStamp.setText(allotment.getStringTimeStamp());
            destination.setText(allotment.getClientName());
            cylinderCount.setText(Integer.toString(allotment.getNumberOfCylinders())+ " Cylinders");
        }
    }
}
