package com.papco.sundar.cylinderinventory.screens.operations.allotment.pickup;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.firestore.ListenerRegistration;
import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Allotment;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.AllotmentListItem;
import com.papco.sundar.cylinderinventory.screens.operations.allotment.create.AllotmentItemAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PickupRequirementFragment extends DialogFragment {


    private ImageView closeImage;
    private RecyclerView recyclerView;
    private AllotmentItemAdapter adapter;
    private FillCylindersVM viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel= ViewModelProviders.of(getActivity()).get(FillCylindersVM.class);
        viewModel.getCurrentAllotment().observe(this, new Observer<Allotment>() {
            @Override
            public void onChanged(Allotment allotment) {
                if(allotment==null)
                    return;

                //if(adapter.getData().size()>0)
                 //   return;

                HashMap<String,Integer> map=allotment.getRequirement();
                List<AllotmentListItem> requirements=new ArrayList<>();
                AllotmentListItem item;
                for(String key:map.keySet()){
                    item=new AllotmentListItem();
                    item.setCylinderTypeName(key);
                    item.setRequiredQuantity(map.get(key));
                    item.setApprovedQuantity(item.getRequiredQuantity());
                    item.setChecked(true);
                    requirements.add(item);
                }

                adapter.setData(requirements);

            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view=getActivity().getLayoutInflater().inflate(R.layout.pickup_requirement,null);
        linkViews(view);
        initViews(view);

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setView(view);

        return builder.create();
    }

    private void linkViews(View view) {

        closeImage=view.findViewById(R.id.pickup_req_close);
        recyclerView=view.findViewById(R.id.pickup_req_recycler);
    }

    private void initViews(View view) {

        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        adapter=new AllotmentItemAdapter(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

    }

}
