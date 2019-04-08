package com.papco.sundar.cylinderinventory.screens.common.batchDetailView;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.papco.sundar.cylinderinventory.R;
import com.papco.sundar.cylinderinventory.data.Cylinder;
import com.papco.sundar.cylinderinventory.screens.batchDetail.CylinderListSpacingDecoration;

import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CylinderTypeDetailedView {

    private Context context;
    private View view;
    private int batchType;
    private int cylinderItemsPerRow;
    private TextView cylinderTypeName,cylinderQuantity;
    private RecyclerView recyclerView;
    private List<Cylinder> monoList;
    private BatchDetailViewAdapter adapter;


    public CylinderTypeDetailedView(Context context, List<Cylinder> monoList,int batchType) throws Exception{

        this.monoList=monoList;
        this.context=context;
        this.batchType=batchType;

        if(context.getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
            cylinderItemsPerRow=6;
        else
            cylinderItemsPerRow=10;

        initializeView();

    }

    public View getView(){
        return view;
    }

    private void initializeView() throws Exception{

        LayoutInflater layoutInflater=LayoutInflater.from(context);
        view=layoutInflater.inflate(R.layout.batch_type_detail_view,null);
        linkViews(view);
        initViews(view);
    }

    private void linkViews(View view) {

        cylinderTypeName=view.findViewById(R.id.batch_detail_view_cylinder_type_name);
        cylinderQuantity=view.findViewById(R.id.batch_detail_view_cylinder_quantity);
        recyclerView=view.findViewById(R.id.batch_detail_view_recycler);

    }

    private void initViews(View view) throws Exception{

        if(monoList==null || monoList.size()==0)
            throw new Exception("Invalid monolist");

        cylinderTypeName.setText(monoList.get(0).getCylinderTypeName());
        cylinderQuantity.setText(Integer.toString(monoList.size())+" Cylinders");
        adapter=new BatchDetailViewAdapter(context,batchType);
        adapter.setData(monoList);

        int heightDpInPixel= (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,60,context.getResources().getDisplayMetrics());
        int sixDp=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,6,context.getResources().getDisplayMetrics());
        if(monoList.size()<=cylinderItemsPerRow){
            recyclerView.getLayoutParams().height=heightDpInPixel;
            recyclerView.setLayoutManager(new GridLayoutManager(context,1, GridLayoutManager.HORIZONTAL,false));
            recyclerView.addItemDecoration(new CylinderListSpacingDecoration(context,1));
        }else{
            recyclerView.getLayoutParams().height=heightDpInPixel*2;
            recyclerView.setLayoutManager(new GridLayoutManager(context,2, GridLayoutManager.HORIZONTAL,false));
            recyclerView.addItemDecoration(new CylinderListSpacingDecoration(context,2));
        }


        recyclerView.setAdapter(adapter);

    }

}
