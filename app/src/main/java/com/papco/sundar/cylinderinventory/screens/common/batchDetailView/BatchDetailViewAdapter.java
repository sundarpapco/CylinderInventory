package com.papco.sundar.cylinderinventory.screens.common.batchDetailView;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.data.Cylinder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class BatchDetailViewAdapter extends RecyclerView.Adapter<BatchDetailViewAdapter.ViewHolder> {

    private List<Cylinder> data;
    private int backgroundColour;

    public BatchDetailViewAdapter(Context context, int batchType) {
        this.data = new ArrayList<>();
        loadBackgroundColour(context, batchType);
    }

    private void loadBackgroundColour(Context context, int batchType) {

        switch (batchType) {

            case Batch.TYPE_ECR:
                backgroundColour = ContextCompat.getColor(context, R.color.ecr_mild_blue);
                break;

            case Batch.TYPE_FCI:
                backgroundColour = ContextCompat.getColor(context, R.color.fci_mild_green);
                break;

            case Batch.TYPE_INVOICE:
                backgroundColour = ContextCompat.getColor(context, R.color.invoice_mild_green);
                break;

            case Batch.TYPE_RCI:
                backgroundColour = ContextCompat.getColor(context, R.color.rci_mild_orange);
                break;

            case Batch.TYPE_REFILL:
                backgroundColour = ContextCompat.getColor(context, R.color.ref_mild_pink);
                break;

            case Batch.TYPE_REPAIR:
                backgroundColour = ContextCompat.getColor(context, R.color.rep_mild_red);
                break;

        }

    }


    public void setData(List<Cylinder> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.batch_detail_cylinder_item, parent, false);


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

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView cylinderNumber;
        View backgroundShape;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cylinderNumber = itemView.findViewById(R.id.batch_detail_item_cylinder_number);
            backgroundShape = itemView;
        }

        public void bind() {

            cylinderNumber.setText(data.get(getAdapterPosition()).getStringId());
            backgroundShape.getBackground().setColorFilter(new PorterDuffColorFilter(backgroundColour, PorterDuff.Mode.MULTIPLY));
        }
    }
}
