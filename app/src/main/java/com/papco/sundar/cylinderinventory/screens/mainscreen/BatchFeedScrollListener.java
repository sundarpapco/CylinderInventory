package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BatchFeedScrollListener extends RecyclerView.OnScrollListener {

    public static final String TAG="SUNDAR";

    public static final int PAGE_SIZE=10;
    private static final int PREFETCH_DISTANCE=3;

    private boolean isLoading=false;
    private boolean allLoadingComplete=false;
    private LinearLayoutManager layoutManager;
    private LoadMoreListener callback;


    public BatchFeedScrollListener(@NonNull LinearLayoutManager layoutManager, @NonNull LoadMoreListener callback) {
        this.layoutManager = layoutManager;
        this.callback = callback;
    }


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

        if(dy<=0)
            return;


        if(!allLoadingComplete &&
                !isLoading &&
                layoutManager.getItemCount()-layoutManager.findLastVisibleItemPosition()<=PREFETCH_DISTANCE){

            //trigger the load here
            log("Triggering the load");
            isLoading=true;
            if(callback!=null)
                callback.loadMoreData();

        }


    }

    public void loadCompleted(){
        log("setting load complete");
        isLoading=false;
    }

    public void setAllLoadingCOmplete(){

        log("setting all load complete");
        allLoadingComplete=true;
    }

    public interface LoadMoreListener{

        public void loadMoreData();

    }

    private void log(String msg){

        Log.d(TAG, msg);
    }
}
