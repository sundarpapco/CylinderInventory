package com.papco.sundar.cylinderinventory.screens.operations.allotment.create;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AllotmentItemAdapter extends RecyclerView.Adapter<AllotmentItemAdapter.ViewHolder> {

    private RecyclerListener<AllotmentListItem> callback;
    private List<AllotmentListItem> data;

    public AllotmentItemAdapter(RecyclerListener<AllotmentListItem> callback) {
        this.callback = callback;
        data = new ArrayList<>();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.allotment_item, parent, false);
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

    public void addData(AllotmentListItem newItem){
        data.add(newItem);
        notifyDataSetChanged();
        //notifyItemInserted(data.size());
    }

    public void updateData(int position,AllotmentListItem updatedItem){

        data.set(position,updatedItem);
        notifyItemChanged(position);
    }

    public boolean isDuplicate(int position,String cylinderTypeName){

        //give the position as -1 when checking duplicating before adding new item
        //else give the position to update to check for duplication ignoring that position

        AllotmentListItem item;
        for (int i = 0; i <data.size() ; i++) {
            if(i==position)
                continue;

            item=data.get(i);
            if(item.getCylinderTypeName().equals(cylinderTypeName)){
                return true;
            }
        }
        return false;
    }

    public List<AllotmentListItem> getData(){
        return data;
    }

    public void setData(List<AllotmentListItem> data){
        this.data=data;
        notifyDataSetChanged();
    }

    public void deleteData(int position){
        data.remove(position);
        notifyItemRemoved(position);
    }

    public int getTotalCylinderCount(){

        int count=0;
        for(AllotmentListItem item:data){
            count=count+item.getRequiredQuantity();
        }
        return count;
    }

    public HashMap<String,Integer> getRequirementHashMap(){

        if(data.size()==0)
            return null;

        HashMap<String,Integer> requirement=new HashMap<>();

        for(AllotmentListItem item:data){
            requirement.put(item.getCylinderTypeName(),item.getRequiredQuantity());
        }
        return requirement;

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView cylinderType, cylinderCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cylinderType = itemView.findViewById(R.id.allotment_list_item_type_name);
            cylinderCount = itemView.findViewById(R.id.allotment_list_item_cyl_count);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null)
                        callback.onRecyclerItemClicked(data.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(callback!=null)
                        callback.onRecyclerItemLongClicked(data.get(getAdapterPosition()),getAdapterPosition(),v);

                    return true;
                }
            });
        }

        public void bind() {

            AllotmentListItem item = data.get(getAdapterPosition());
            cylinderType.setText(item.getCylinderTypeName());
            cylinderCount.setText(Integer.toString(item.getRequiredQuantity()));

        }
    }
}
