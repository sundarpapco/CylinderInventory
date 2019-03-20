package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.historyList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.DividerDecoration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Batch;
import com.papco.sundar.cylinderinventory.logic.LoadMoreListener;
import com.papco.sundar.cylinderinventory.logic.RecyclerListener;
import com.papco.sundar.cylinderinventory.screens.batchDetail.BatchDetailActivity;
import com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.DestinationDetailVM;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DestinationHistoryFragment extends Fragment implements RecyclerListener<Batch> {

    public static final String TAG="SUNDAR";
    private static final String KEY_DEST_ID="destination_id";


    public static Bundle getArguments(int destinationId){

        Bundle bundle=new Bundle();
        bundle.putInt(KEY_DEST_ID,destinationId);
        return bundle;
    }

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private DestinationHistoryScrollListener scrollListener;
    private DestinationHistoryAdapter adapter;
    private DestinationDetailVM viewModel;
    private DestinationHistoryData dataSource;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel= ViewModelProviders.of(getActivity()).get(DestinationDetailVM.class);
        dataSource=viewModel.getHistoryData(getDestinationId());
        dataSource.loadMoreData(null);
        dataSource.getBatches().observe(this, new Observer<List<DocumentSnapshot>>() {
            @Override
            public void onChanged(List<DocumentSnapshot> snapshots) {

                hideProgressBar();
                scrollListener.loadCompleted();
                if(snapshots==null){
                    Msg.show(requireContext(),"Error loading history data. Please check internet connection");
                    return;
                }

                if(snapshots.size()<DestinationHistoryData.PAGE_SIZE)
                    scrollListener.setAllLoadingCOmplete();

                adapter.submitData(snapshots);

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.just_recycler,container,false);
        linkViews(view);
        initViews(view);
        return view;

    }

    @Override
    public void onRecyclerItemClicked(Batch item, int position) {

        BatchDetailActivity.start(getActivity(),item);

    }

    @Override
    public void onRecyclerItemLongClicked(Batch item, int position, View view) {

    }

    private void linkViews(View view) {

        recyclerView=view.findViewById(R.id.recycler);
        progressBar=view.findViewById(R.id.progress_bar);
    }

    private void initViews(View view) {

        int dividerColor= ContextCompat.getColor(requireContext(),R.color.borderGrey);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        scrollListener=new DestinationHistoryScrollListener(layoutManager, new LoadMoreListener() {
            @Override
            public void loadMoreData() {
                dataSource.loadMoreData(adapter.getLastLoadedSnapshot());
            }
        });
        adapter=new DestinationHistoryAdapter(this);

        recyclerView.addItemDecoration(new DividerDecoration(getActivity(),dividerColor));
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        showProgressBar();
    }

    private int getDestinationId(){

        if(getArguments()==null)
            return -1;

        return getArguments().getInt(KEY_DEST_ID,-1);
    }

    private void showProgressBar(){

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideProgressBar(){

        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

    }

}
