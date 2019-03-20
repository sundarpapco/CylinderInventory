package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.historyList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DestinationHistoryAdapter extends RecyclerView.Adapter<DestinationHistoryAdapter.ViewHolder>{


    private List<DocumentSnapshot> data;
    private RecyclerListener<Batch> callback;

    public DestinationHistoryAdapter(@NonNull RecyclerListener<Batch> callback){
        data=new ArrayList<>();
        this.callback=callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.dest_history_item,parent,false);
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

    public void submitData(List<DocumentSnapshot> data){

        if(data.size()==0){
            this.data=data;
            notifyItemRangeInserted(0,data.size());
            return;
        }

        int position=this.data.size();
        this.data.addAll(data);
        notifyItemRangeInserted(position,data.size());

    }

    public DocumentSnapshot getLastLoadedSnapshot(){

        if(data.size()==0)
            return null;
        else
            return data.get(data.size()-1);

    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView batchType,cylinderCount,batchNumber,timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            batchType=itemView.findViewById(R.id.dest_history_item_batch_type);
            cylinderCount=itemView.findViewById(R.id.dest_history_item_cylinders);
            batchNumber=itemView.findViewById(R.id.dest_history_item_batch_number);
            timestamp=itemView.findViewById(R.id.dest_history_item_date);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Batch batch=data.get(getAdapterPosition()).toObject(Batch.class);
                    callback.onRecyclerItemClicked(batch,getAdapterPosition());
                }
            });

        }

        public void bind(){

            Batch batch=data.get(getAdapterPosition()).toObject(Batch.class);
            batchType.setText(batch.getStringBatchType());
            cylinderCount.setText(Integer.toString(batch.getNoOfCylinders())+" Cylinders");
            batchNumber.setText(batch.getBatchNumber());
            timestamp.setText(batch.getStringDate());

        }

    }
}
