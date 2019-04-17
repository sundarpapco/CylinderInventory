package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Batch;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class BatchFeedAdapter extends RecyclerView.Adapter<BatchFeedAdapter.BatchVH>
        implements FeedDataSource.Callback, BatchFeedScrollListener.Callback {

    private List<DocumentSnapshot> data;
    private FeedAdapterCallBack callback;
    private Context context;
    private BatchFeedScrollListener scrollListener;

    private FeedDataSource dataSource;

    BatchFeedAdapter(@NonNull Context context, @NonNull FeedAdapterCallBack callback) {
        this.data = new LinkedList<>();
        this.callback = callback;
        this.context = context;
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


    void setDataSource(@NonNull FeedDataSource dataSource) {

        this.dataSource = dataSource;
        dataSource.setCallback(this);
    }

    void setFilters(int typeFilter, long timeFilter) {

        if (dataSource == null) {
            Msg.show(context, "Set data source before setting filters");
            return;
        }

        callback.onStartLoadingData();
        data.clear();
        notifyDataSetChanged();
        dataSource.loadInitialData(typeFilter, timeFilter);
    }

    void setScrollListener(BatchFeedScrollListener scrollListener) {
        this.scrollListener = scrollListener;
        scrollListener.setCallback(this);
    }

    private void loadNextPage() {

        if (data.size() == 0 || dataSource == null)
            return;

        dataSource.loadAfter(data.get(data.size() - 1));

    }

    void onConfigChange() {

        dataSource.setConfigChangeBackup(data);
        dataSource.clearCallback();
        scrollListener.clearCallback();
    }

    private void runDiff(List<DocumentSnapshot> newList) {

        List<DocumentSnapshot> oldList;

        if (data.size() <= BatchFeedScrollListener.PAGE_SIZE)
            oldList = data;
        else
            oldList = data.subList(0, BatchFeedScrollListener.PAGE_SIZE);

        DiffUtilRunner diffUtilRunner=new DiffUtilRunner(result -> {

            oldList.clear();
            data.addAll(0,newList);
            result.dispatchUpdatesTo(BatchFeedAdapter.this);

        });

        diffUtilRunner.execute(oldList,newList);

    }


    @Override
    public void onInitialLoadComplete(List<DocumentSnapshot> initialData) {

        if (data.size() == 0) {
            this.data = initialData;
            notifyDataSetChanged();
        } else {
            //its a invalidation request
            if (data.size() <= BatchFeedScrollListener.PAGE_SIZE) {
                runDiff(initialData);
            } else
                dataSource.loadBefore(data.get(BatchFeedScrollListener.PAGE_SIZE + 1));
        }
        callback.onFinishLoadingData();

    }

    @Override
    public void onLoadNextComplete(List<DocumentSnapshot> nextPageData) {

        scrollListener.loadCompleted();
        if (nextPageData.size() < BatchFeedScrollListener.PAGE_SIZE)
            scrollListener.setAllLoadingComplete();

        int oldDatacount = data.size();
        data.addAll(nextPageData);
        notifyItemRangeInserted(oldDatacount, nextPageData.size());


    }

    @Override
    public void onLoadPreviousComplete(List<DocumentSnapshot> previousData) {
        runDiff(previousData);
    }

    @Override
    public void loadMoreData() {
        loadNextPage();
    }

    class BatchVH extends RecyclerView.ViewHolder {

        TextView heading, clientName, cylinderCount, timestamp, batchNumber;
        View colorView;

        BatchVH(@NonNull View itemView) {
            super(itemView);

            heading = itemView.findViewById(R.id.batch_item_title);
            clientName = itemView.findViewById(R.id.batch_item_destination_name);
            cylinderCount = itemView.findViewById(R.id.batch_item_no_of_cylinders);
            timestamp = itemView.findViewById(R.id.batch_item_timestamp);
            colorView = itemView.findViewById(R.id.view);
            batchNumber = itemView.findViewById(R.id.batch_item_batch_number);

            itemView.setOnClickListener(v -> callback.onRecyclerItemClicked(data.get(getAdapterPosition()).toObject(Batch.class), getAdapterPosition()));
        }

        public void bind() {

            Batch batch = data.get(getAdapterPosition()).toObject(Batch.class);
            int roundColor = 1;
            switch (batch.getType()) {

                case Batch.TYPE_INVOICE:
                    heading.setText("Invoice");
                    roundColor = ContextCompat.getColor(context, R.color.invoice_green);
                    break;

                case Batch.TYPE_ECR:
                    heading.setText("Empty cylinder return");
                    roundColor = ContextCompat.getColor(context, R.color.ecr_blue);
                    break;

                case Batch.TYPE_FCI:
                    heading.setText("Full cylinder inward");
                    roundColor = ContextCompat.getColor(context, R.color.fci_green);
                    break;

                case Batch.TYPE_RCI:
                    heading.setText("Repair cylinder inward");
                    roundColor = ContextCompat.getColor(context, R.color.rci_orange);
                    break;

                case Batch.TYPE_REFILL:
                    heading.setText("Sent for refilling");
                    roundColor = ContextCompat.getColor(context, R.color.ref_pink);
                    break;

                case Batch.TYPE_REPAIR:
                    heading.setText("Sent for repair");
                    roundColor = ContextCompat.getColor(context, R.color.rep_red);
                    break;


            }

            colorView.getBackground().setColorFilter(roundColor, PorterDuff.Mode.MULTIPLY);
            clientName.setText(batch.getDestinationName());
            cylinderCount.setText(Integer.toString(batch.getNoOfCylinders()) + " Cylinders");
            timestamp.setText(batch.getStringTimeStamp());
            batchNumber.setText(batch.getBatchNumber());

        }
    }

    public interface FeedAdapterCallBack {

        void onRecyclerItemClicked(Batch item, int position);

        void onRecyclerItemLongClicked(Batch item, int position, View view);

        void onStartLoadingData();

        void onFinishLoadingData();

    }
}
