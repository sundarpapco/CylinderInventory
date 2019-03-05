package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Batch;

import java.util.List;

public class BatchFeedAdapter extends RecyclerView.Adapter<BatchFeedAdapter.BatchVH> {

    private List<Batch> data;

    public BatchFeedAdapter(@NonNull List<Batch> data) {
        this.data = data;
    }


    @NonNull
    @Override
    public BatchVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.batch_item, viewGroup, false);
        return new BatchVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BatchVH batchVH, int i) {
        batchVH.bind();

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Batch> data) {
        this.data=data;
        notifyDataSetChanged();
    }

    public List<Batch> getData() {
        return data;
    }

    class BatchVH extends RecyclerView.ViewHolder {

        TextView heading, clientName, cylinderCount, timestamp;
        View colorView;

        public BatchVH(@NonNull View itemView) {
            super(itemView);

            heading = itemView.findViewById(R.id.batch_item_title);
            clientName = itemView.findViewById(R.id.batch_item_destination_name);
            cylinderCount = itemView.findViewById(R.id.batch_item_no_of_cylinders);
            timestamp = itemView.findViewById(R.id.batch_item_timestamp);
            colorView=itemView.findViewById(R.id.view);
        }

        public void bind() {

            Batch batch = data.get(getAdapterPosition());
            switch (batch.getType()) {

                case Batch.TYPE_INVOICE:
                    heading.setText("Invoice");
                    colorView.setBackgroundResource(R.drawable.round_green);
                    break;

                case Batch.TYPE_ECR:
                    heading.setText("Empty cylinder return");
                    colorView.setBackgroundResource(R.drawable.round_lightblue);
                    break;

                case Batch.TYPE_FCI:
                    heading.setText("Full cylinder inward");
                    colorView.setBackgroundResource(R.drawable.round_darkgreen);
                    break;

                case Batch.TYPE_RCI:
                    heading.setText("Repair cylinder inward");
                    colorView.setBackgroundResource(R.drawable.round_orange);
                    break;

                case Batch.TYPE_REFILL:
                    heading.setText("Sent for refilling");
                    colorView.setBackgroundResource(R.drawable.round_blue);
                    break;

                case Batch.TYPE_REPAIR:
                    heading.setText("Sent for repair");
                    colorView.setBackgroundResource(R.drawable.round_red);
                    break;


            }

            clientName.setText(batch.getDestinationName());
            cylinderCount.setText(Integer.toString(batch.getNoOfCylinders())+" Cylinders");
            timestamp.setText(batch.getStringTimeStamp());

        }
    }
}
