package com.papco.sundar.cylinderinventory.screens.operations.allotment.approve;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

public class ApproveAllotmentAdapter extends RecyclerView.Adapter<ApproveAllotmentAdapter.ViewHolder> {

    private List<AllotmentListItem> data;
    private RecyclerListener callback;
    private int approvedCylinderCount;

    public ApproveAllotmentAdapter(@NonNull RecyclerListener callback) {

        this.callback = callback;
        data = new ArrayList<>();
        approvedCylinderCount = 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.approve_allot_item, parent, false);
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

    public void setData(List<AllotmentListItem> newData) {
        this.data = newData;
        approvedCylinderCount = 0;
        for (AllotmentListItem item : newData) {
            if (item.isChecked())
                approvedCylinderCount += item.getApprovedQuantity();
        }
        if (callback != null)
            callback.onApprovedCylinderCountChanged(approvedCylinderCount);
        notifyDataSetChanged();
    }

    public void setData(HashMap<String, Integer> newData) {

        List<AllotmentListItem> data = new ArrayList<>();
        int requiredCount;
        AllotmentListItem item;
        approvedCylinderCount = 0;
        for (String key : newData.keySet()) {
            requiredCount = (Integer) newData.get(key);
            item = new AllotmentListItem();
            item.setChecked(true);
            item.setRequiredQuantity(requiredCount);
            item.setApprovedQuantity(requiredCount);
            item.setCylinderTypeName(key);
            data.add(item);
            approvedCylinderCount += requiredCount;
        }
        this.data = data;
        if (callback != null)
            callback.onApprovedCylinderCountChanged(approvedCylinderCount);
        notifyDataSetChanged();

    }

    public void updateData(int position, int approvalQuantity) {
        approvedCylinderCount = approvedCylinderCount + (approvalQuantity - data.get(position).getApprovedQuantity());
        data.get(position).setApprovedQuantity(approvalQuantity);
        if (callback != null)
            callback.onApprovedCylinderCountChanged(approvedCylinderCount);
        notifyItemChanged(position);
    }

    public int getApprovedCylinderCount() {
        return approvedCylinderCount;
    }

    public List<AllotmentListItem> getData() {
        return data;
    }

    public HashMap<String, Integer> getUnApprovedHashMap() {

        HashMap<String, Integer> resultMap = new HashMap<>();
        boolean hashMapValid = false;

        for (AllotmentListItem item : data) {

            if (item.isChecked()) {
                if (item.getRequiredQuantity() == item.getApprovedQuantity())
                    continue;
                else {
                    hashMapValid = true;
                    resultMap.put(item.getCylinderTypeName(), item.getRequiredQuantity() - item.getApprovedQuantity());
                }
            } else {
                hashMapValid = true;
                resultMap.put(item.getCylinderTypeName(), item.getRequiredQuantity());
            }
        }

        if (hashMapValid)
            return resultMap;
        else
            return null;

    }

    public HashMap<String, Integer> getApprovedHashMap() {

        HashMap<String, Integer> resultMap = new HashMap<>();
        boolean hashMapValid = false;

        for (AllotmentListItem item : data) {
            if (item.isChecked() && item.getApprovedQuantity() > 0) {
                hashMapValid = true;
                resultMap.put(item.getCylinderTypeName(), item.getApprovedQuantity());
            }
        }

        if (hashMapValid)
            return resultMap;
        else
            return null;

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatCheckBox checkBox;
        TextView cylinderType, reqCylinders, approvedCylinders;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.approve_allot_item_checkbox);
            cylinderType = itemView.findViewById(R.id.approve_allot_item_type_name);
            reqCylinders = itemView.findViewById(R.id.approve_allot_item_required_cyl);
            approvedCylinders = itemView.findViewById(R.id.approve_allot_item_approved_cyl_count);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!data.get(getAdapterPosition()).isChecked())
                        return;

                    if (callback != null)
                        callback.onRecyclerItemClicked(data.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCompatCheckBox checkBox = (AppCompatCheckBox) v;
                    AllotmentListItem item = data.get(getAdapterPosition());
                    item.setChecked(checkBox.isChecked());
                    if (checkBox.isChecked()) {
                        approvedCylinderCount = approvedCylinderCount + item.getApprovedQuantity();
                        itemView.setActivated(true);
                    } else {
                        approvedCylinderCount = approvedCylinderCount - item.getApprovedQuantity();
                        itemView.setActivated(false);
                    }
                    if (callback != null)
                        callback.onApprovedCylinderCountChanged(approvedCylinderCount);
                }
            });
        }

        public void bind() {

            AllotmentListItem item = data.get(getAdapterPosition());
            if (item.isChecked()) {
                checkBox.setChecked(true);
                itemView.setActivated(true);
            } else {
                checkBox.setChecked(false);
                itemView.setActivated(false);
            }
            cylinderType.setText(item.getCylinderTypeName());
            reqCylinders.setText(Integer.toString(item.getRequiredQuantity()) + " Cylinders");
            approvedCylinders.setText(Integer.toString(item.getApprovedQuantity()));

        }
    }

    public interface RecyclerListener {

        void onRecyclerItemClicked(AllotmentListItem item, int position);

        void onRecyclerItemLongClicked(AllotmentListItem item, int position, View view);

        void onApprovedCylinderCountChanged(int approvedCylinders);

    }
}
