package com.papco.sundar.cylinderinventory.screens.cylinders.cylinderTypes;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.CylinderType;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CylinderTypesAdapter extends RecyclerView.Adapter<CylinderTypesAdapter.ViewHolder> implements Filterable {

    private List<CylinderType> unFilteredData;
    private List<CylinderType> data;
    private CylinderTypesListListener callback;
    private String searchQuery;

    public CylinderTypesAdapter(@NonNull CylinderTypesListListener callback){

        data=new ArrayList<>();
        unFilteredData=data;
        this.callback=callback;
        searchQuery="";

    }

    public CylinderTypesAdapter(@NonNull List<CylinderType> data,@NonNull CylinderTypesListListener callback){

        this.data=data;
        unFilteredData=data;
        this.callback=callback;
        searchQuery="";

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.cylinder_type_item,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind();

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<CylinderType> data, String searchQuery) {
        this.searchQuery=searchQuery;
        this.unFilteredData=data;
        this.data = data;
        filterData(searchQuery);

    }

    public boolean isDuplicate(String newTypeName){

        newTypeName=newTypeName.trim();
        for(CylinderType cylinderType:data){

            if(cylinderType.getName().equals(newTypeName))
                return true;
        }

        return false;
    }

    public void filterData(String searchString) {

        searchQuery=searchString;

        if (TextUtils.isEmpty(searchQuery)) {
            data = unFilteredData;
            notifyDataSetChanged();
        } else {
            getFilter().filter(searchString);
        }

    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            List<CylinderType> filteredList = new ArrayList<>();

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String stringToSearch = charSequence.toString().toLowerCase();

                for (CylinderType cylinderType : unFilteredData) {
                    if (cylinderType.getName().toLowerCase().contains(stringToSearch)) {

                        cylinderType.highlightedName = getHighlightedString(cylinderType.getName(), stringToSearch, Color.YELLOW);
                        filteredList.add(cylinderType);
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                data = (List<CylinderType>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    private SpannableString getHighlightedString(String source, String toHighlight, int highlightColour) {

        int searchStart = 0;
        int searchLength = toHighlight.length();
        int sourceLength = source.length();
        SpannableString resultString = new SpannableString(source);
        source = source.toLowerCase();
        toHighlight = toHighlight.toLowerCase();
        int currentIndex = source.indexOf(toHighlight, searchStart);

        while (currentIndex != -1) {

            resultString.setSpan(new BackgroundColorSpan(highlightColour), currentIndex, currentIndex + searchLength, 0);
            searchStart = currentIndex + searchLength;
            if (searchStart > sourceLength)
                break;

            currentIndex = source.indexOf(toHighlight, searchStart);

        }

        return resultString;

    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView typeName,cylinderCount;
        ImageView iconDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            typeName=itemView.findViewById(R.id.cylinder_type_item_name);
            cylinderCount=itemView.findViewById(R.id.cylinder_type_item_cyl_count);
            iconDelete=itemView.findViewById(R.id.cylinder_type_item_ic_delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback!=null)
                        callback.onCylinderTypeClicked(data.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(callback!=null){
                        callback.onCylinderTypeLongClicked(data.get(getAdapterPosition()));
                        return true;
                    }
                    return false;
                }
            });

            iconDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(callback!=null)
                        callback.onCylinderTypeDeleteClicked(data.get(getAdapterPosition()));

                }
            });

        }

        public void bind(){

            CylinderType cylinderType=data.get(getAdapterPosition());

            if(TextUtils.isEmpty(searchQuery))
                typeName.setText(cylinderType.getName());
            else
                typeName.setText(cylinderType.highlightedName);

            cylinderCount.setText(cylinderType.getStringNoOfCylinders()+" Cylinders");
            if(cylinderType.isEditable()){
                iconDelete.setVisibility(View.VISIBLE);
            }else{
                iconDelete.setVisibility(View.GONE);
            }

        }

    }

    public interface CylinderTypesListListener{

        void onCylinderTypeClicked(CylinderType cylinderType);
        void onCylinderTypeLongClicked(CylinderType cylinderType);
        void onCylinderTypeEditClicked(CylinderType cylinderType);
        void onCylinderTypeDeleteClicked(CylinderType cylinderType);
    }
}
