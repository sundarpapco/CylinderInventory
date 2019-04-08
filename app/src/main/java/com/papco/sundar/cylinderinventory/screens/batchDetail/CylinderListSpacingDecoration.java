package com.papco.sundar.cylinderinventory.screens.batchDetail;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CylinderListSpacingDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int sixDp;

    public CylinderListSpacingDecoration(Context context,int spanCount) {
        this.spanCount=spanCount;
        sixDp=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,6,context.getResources().getDisplayMetrics());
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view); // item position

        outRect.bottom=0;
        outRect.left=0;
        outRect.right=sixDp;
        outRect.top=sixDp;

        /*if(position%spanCount==0){
            //first row
            outRect.top=0;
        }else
            outRect.top=sixDp;*/

    }

    private void log(String msg){

        Log.d("SUNDAR", msg);
    }


}
