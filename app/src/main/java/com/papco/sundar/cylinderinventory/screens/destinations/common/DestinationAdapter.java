package com.papco.sundar.cylinderinventory.screens.destinations.common;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Destination;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;

import java.util.ArrayList;
import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ClientViewHolder> implements Filterable {

    private List<Destination> unfilteredData;
    private List<Destination> data;
    private RecyclerListener<Destination> callback;
    private String searchQuery;

    public DestinationAdapter(@NonNull List<Destination> data, @NonNull RecyclerListener callback) {
        this.unfilteredData = data;
        this.data = data;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.destination_list_item, viewGroup, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder clientViewHolder, int i) {
        clientViewHolder.bindView();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Destination> data,String searchQuery) {
        this.searchQuery=searchQuery;
        this.unfilteredData=data;
        this.data = data;
        filterData(searchQuery);

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

    public void filterData(String searchString) {

        searchQuery=searchString;

        if (TextUtils.isEmpty(searchQuery)) {
            data = unfilteredData;
            notifyDataSetChanged();
        } else {
            getFilter().filter(searchString);
        }

    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            List<Destination> filteredList = new ArrayList<>();

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String stringToSearch = charSequence.toString().toLowerCase();

                for (Destination destination : unfilteredData) {
                    if (destination.getName().toLowerCase().contains(stringToSearch)) {

                        destination.highlightedName = getHighlightedString(destination.getName(), stringToSearch, Color.YELLOW);
                        filteredList.add(destination);
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                data = (List<Destination>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class ClientViewHolder extends RecyclerView.ViewHolder {

        TextView destName, destCyclinderCount;

        ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            destName = itemView.findViewById(R.id.destination_item_name);
            destCyclinderCount = itemView.findViewById(R.id.destination_item_cyl_count);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onRecyclerItemClicked(data.get(getAdapterPosition()), getAdapterPosition());
                }
            });

        }

        void bindView() {

            Destination destination = data.get(getAdapterPosition());
            if (!TextUtils.isEmpty(searchQuery))
                destName.setText(destination.highlightedName);
            else
                destName.setText(destination.getName());

            destCyclinderCount.setText(Integer.toString(destination.getCylinderCount()) + " Cylinders");
        }
    }
}
