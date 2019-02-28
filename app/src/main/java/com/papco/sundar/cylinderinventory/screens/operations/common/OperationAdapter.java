package com.papco.sundar.cylinderinventory.screens.operations.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;

import java.util.List;

public class OperationAdapter extends RecyclerView.Adapter<OperationAdapter.InwardVH> {


    private List<Integer> data;
    private RecyclerListener<Integer> callback;

    public OperationAdapter(@NonNull List<Integer> data, @NonNull RecyclerListener<Integer> callback){

        this.data=data;
        this.callback=callback;

    }

    @NonNull
    @Override
    public InwardVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cylinder_list_item,viewGroup,false);
        return new InwardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InwardVH inwardVH, int i) {
        inwardVH.bind();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Integer> newData){
        this.data=newData;
        notifyDataSetChanged();
    }

    public void addNumber(int newNumber){
        data.add(newNumber);
        notifyItemInserted(data.size()-1);
    }

    public void removeNumber(int position){
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void updateNumber(int number,int position){

        data.set(position,number);
        notifyItemChanged(position);

    }

    public List<Integer> getData(){
        return data;
    }

    public boolean contains(int id){

        for(int i=0;i<data.size();++i){
            if(data.get(i)==id)
                return true;
        }

        return false;
    }

    class InwardVH extends RecyclerView.ViewHolder{

        TextView cylinderNumber;

        public InwardVH(@NonNull final View itemView) {
            super(itemView);
            cylinderNumber=itemView.findViewById(R.id.cyl_list_item_no);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onRecyclerItemClicked(data.get(getAdapterPosition()),getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callback.onRecyclerItemLongClicked(data.get(getAdapterPosition()),getAdapterPosition(),itemView );
                    return true;
                }
            });
        }

        public void bind(){

            cylinderNumber.setText(Integer.toString(data.get(getAdapterPosition())));
        }
    }
}
