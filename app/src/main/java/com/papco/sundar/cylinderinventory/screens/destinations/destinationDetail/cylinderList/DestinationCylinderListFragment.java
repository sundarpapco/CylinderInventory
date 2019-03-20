package com.papco.sundar.cylinderinventory.screens.destinations.destinationDetail.cylinderList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.common.DividerDecoration;
import com.papco.sundar.cylinderinventory.common.Msg;
import com.papco.sundar.cylinderinventory.data.Cylinder;
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

public class DestinationCylinderListFragment extends Fragment {

    private static final String KEY_DEST_ID="destination_id";


    public static Bundle getArguments(int destinationId){

        Bundle bundle=new Bundle();
        bundle.putInt(KEY_DEST_ID,destinationId);
        return bundle;
    }


    public static final String TAG="SUNDAR";

    private DestinationDetailVM viewmodel;
    private DestinationCylindersData dataSource;
    private DestinationCylindersAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewmodel= ViewModelProviders.of(getActivity()).get(DestinationDetailVM.class);
        dataSource=viewmodel.getCylindersData(getDestinationId());

        adapter=new DestinationCylindersAdapter();
        dataSource.getCylinders().observe(this, new Observer<List<Cylinder>>() {
            @Override
            public void onChanged(List<Cylinder> cylinders) {

                hideProgressBar();
                if(cylinders==null){
                    Msg.show(getActivity(),"Cannot get cylinder details. Please check internet connection");
                    return;
                }

                adapter.setData(cylinders);

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.just_recycler,container,false);
        linkView(view);
        initView(view);
        return view;

    }

    private void linkView(View view) {

        recyclerView=view.findViewById(R.id.recycler);
        progressBar=view.findViewById(R.id.progress_bar);
    }

    private void initView(View view) {

        int dividerColor= ContextCompat.getColor(requireContext(),R.color.borderGrey);
        recyclerView.addItemDecoration(new DividerDecoration(getActivity(),dividerColor));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        showProgressBar();
    }

    private void showProgressBar(){

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideProgressBar(){

        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

    }

    private int getDestinationId(){

        if(getArguments()==null)
            return -1;

        return getArguments().getInt(KEY_DEST_ID,-1);
    }


}
