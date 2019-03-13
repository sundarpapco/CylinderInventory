package com.papco.sundar.cylinderinventory.screens.batchDetail;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CylinderListSpacingDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view); // item position
        int spanCount = 6;
        int spacing = 26;//spacing between views in grid
        /*int column=1;

        column=position % spanCount;
        log("column :"+Integer.toString(column));
        if(column==0){

            outRect.left=spacing;
            outRect.right=spacing/2;
            outRect.top=spacing/2;
            outRect.bottom=spacing/2;
            log("first column");
            return;

        }

        if(column==spanCount-1){

            outRect.left=spacing/2;
            outRect.right=spacing;
            outRect.top=spacing/2;
            outRect.bottom=spacing/2;
            log("Last column");
            return;
        }

        outRect.left=spacing/2;
        outRect.right=spacing/2;
        outRect.top=spacing/2;
        outRect.bottom=spacing/2;
        log("middle column");*/
        if (position >= 0) {
            int column = position % spanCount; // item column

            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // item bottom
        } else {
            outRect.left = 0;
            outRect.right = 0;
            outRect.top = 0;
            outRect.bottom = 0;
        }
    }

    private void log(String msg){

        Log.d("SUNDAR", msg);
    }


}
