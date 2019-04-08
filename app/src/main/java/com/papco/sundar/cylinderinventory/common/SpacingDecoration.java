package com.papco.sundar.cylinderinventory.common;

import android.content.Context;
import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class SpacingDecoration extends RecyclerView.ItemDecoration {


    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;

    Context context;
    int topSpacing, bottomSpacing, itemSpacing;
    int orientation;
    int adapterPositionOfView,totalItemsInAdapter;

    public SpacingDecoration(Context context, int orientation, float topSpacingDp, float itemSpacingDp, float bottomSpacingDp) {
        this.context = context;

        //converting the given dp value to pixels
        topSpacing = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topSpacingDp, context.getResources().getDisplayMetrics());
        bottomSpacing = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomSpacingDp, context.getResources().getDisplayMetrics());
        itemSpacing = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, itemSpacingDp, context.getResources().getDisplayMetrics());
        this.orientation = orientation;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        adapterPositionOfView=parent.getChildAdapterPosition(view);
        totalItemsInAdapter=parent.getAdapter().getItemCount();


        //checking for first item in list
        if (adapterPositionOfView == 0) {

            if (orientation == HORIZONTAL) {
                outRect.left = (int) topSpacing;
            }else {
                outRect.top = (int) topSpacing;
            }

            return;
        }

        //checking for last time
        if(adapterPositionOfView==totalItemsInAdapter-1){

            if(orientation==HORIZONTAL) {
                outRect.right = (int) bottomSpacing;
                outRect.left=(int)itemSpacing;

            }else {
                outRect.top=(int)itemSpacing;
                outRect.bottom = (int)bottomSpacing;
            }

            return;
        }

        //intermediate items
        if(orientation==HORIZONTAL) {
            outRect.right = (int) itemSpacing;

        }else {
            outRect.top=(int)itemSpacing;
        }

    }

}
