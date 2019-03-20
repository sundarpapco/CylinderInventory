package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.cylinderList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Cylinder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DestinationCylindersAdapter extends RecyclerView.Adapter<DestinationCylindersAdapter.ViewHolder> {

    private final long oneDayInMilliSecond = 86400000;
    private long currentTime;
    private List<Cylinder> data;

    public DestinationCylindersAdapter(){

        data=new ArrayList<>();
        currentTime= Calendar.getInstance().getTimeInMillis();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.dest_cylinder_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Cylinder> data){
        this.data=data;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView cylinderNumber,date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cylinderNumber=itemView.findViewById(R.id.dest_cyl_item_cyl_no);
            date=itemView.findViewById(R.id.dest_cyl_item_days);
        }

        public void bind(){

            Cylinder cylinder=data.get(getAdapterPosition());
            cylinderNumber.setText(Integer.toString(cylinder.getCylinderNo()));
            long days=(currentTime-cylinder.getLastTransaction())/oneDayInMilliSecond;
            date.setText(Long.toString(days)+" Days");
        }
    }
}
