package com.papco.sundar.cylinderinventory.screens.mainscreen;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;

import java.util.ArrayList;
import java.util.List;

public class BatchFeedAdapter extends RecyclerView.Adapter<BatchFeedAdapter.BatchVH> {

    public static final String TAG="SUNDAR";
    private List<DocumentSnapshot> data;
    private RecyclerListener<Batch> callback;
    private Context context;

    public BatchFeedAdapter(@NonNull Context context, @NonNull RecyclerListener<Batch> callback) {
        this.data = new ArrayList<>();
        this.callback=callback;
        this.context=context;
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


    public void setInitialData(QuerySnapshot querySnapshot) {

        if(data.size()==0){
            data=querySnapshot.getDocuments();
            notifyItemRangeInserted(0,data.size());
            return;
        }

        Log.d(TAG, Integer.toString(querySnapshot.getDocumentChanges().size())+" Documents changed");

        for(DocumentChange documentChange:querySnapshot.getDocumentChanges()){

            if(documentChange.getType()==DocumentChange.Type.ADDED)
                addDocument(documentChange.getDocument());

            /*if(documentChange.getType()==DocumentChange.Type.REMOVED)
                deleteDocument(documentChange.getDocument());

            if(documentChange.getType()==DocumentChange.Type.MODIFIED)
                updateDocument(documentChange.getDocument());*/

        }

    }

    private void deleteDocument(DocumentSnapshot documentToDelete){

        for(int i=0;i<data.size();++i){

            if(data.get(i).getId().equals(documentToDelete)){

                data.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }

    }

    private void updateDocument(DocumentSnapshot documentToUpdate){

        for(int i=0;i<data.size();++i){

            if(data.get(i).getId().equals(documentToUpdate)){

                data.set(i,documentToUpdate);
                notifyItemChanged(i);
                return;
            }
        }
    }

    private void addDocument(DocumentSnapshot documentToAdd){

        data.add(0,documentToAdd);
        notifyItemInserted(0);

    }

    public void addData(List<DocumentSnapshot> moreData){

        int oldDatacount=data.size();
        data.addAll(moreData);
        notifyItemRangeInserted(oldDatacount,moreData.size());

    }

    public List<DocumentSnapshot> getData() {
        return data;
    }

    public DocumentSnapshot getLastLoadedDocument(){

        if(data.size()==0)
            return null;

        return data.get(data.size()-1);
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onRecyclerItemClicked(data.get(getAdapterPosition()).toObject(Batch.class),getAdapterPosition());
                }
            });
        }

        public void bind() {

            Batch batch = data.get(getAdapterPosition()).toObject(Batch.class);
            int roundColor=1;
            switch (batch.getType()) {

                case Batch.TYPE_INVOICE:
                    heading.setText("Invoice");
                    roundColor= ContextCompat.getColor(context,R.color.invoice_green);
                    break;

                case Batch.TYPE_ECR:
                    heading.setText("Empty cylinder return");
                    roundColor= ContextCompat.getColor(context,R.color.ecr_blue);
                    break;

                case Batch.TYPE_FCI:
                    heading.setText("Full cylinder inward");
                    roundColor= ContextCompat.getColor(context,R.color.fci_green);
                    break;

                case Batch.TYPE_RCI:
                    heading.setText("Repair cylinder inward");
                    roundColor= ContextCompat.getColor(context,R.color.rci_orange);
                    break;

                case Batch.TYPE_REFILL:
                    heading.setText("Sent for refilling");
                    roundColor= ContextCompat.getColor(context,R.color.ref_pink);
                    break;

                case Batch.TYPE_REPAIR:
                    heading.setText("Sent for repair");
                    roundColor= ContextCompat.getColor(context,R.color.rep_red);
                    break;


            }

            colorView.getBackground().setColorFilter(roundColor, PorterDuff.Mode.MULTIPLY);
            clientName.setText(batch.getDestinationName());
            cylinderCount.setText(Integer.toString(batch.getNoOfCylinders())+" Cylinders");
            timestamp.setText(batch.getStringTimeStamp());

        }
    }
}
