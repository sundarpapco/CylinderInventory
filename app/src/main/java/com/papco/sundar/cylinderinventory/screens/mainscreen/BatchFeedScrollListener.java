package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.util.Log;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BatchFeedScrollListener extends RecyclerView.OnScrollListener {

    public static final String TAG="SUNDAR";

    public static final int PAGE_SIZE=25;
    private static final int PREFETCH_DISTANCE=10;

    private boolean isLoading=false;
    private boolean allLoadingComplete=false;
    private LinearLayoutManager layoutManager;
    private Callback callback;


    public BatchFeedScrollListener(@NonNull LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void clearCallback(){
        this.callback=null;
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

    public boolean isLoading(){
        return isLoading;
    }

    public void loadCompleted(){
        log("setting load complete");
        isLoading=false;
    }

    public void setAllLoadingComplete(){

        log("setting all load complete");
        allLoadingComplete=true;
    }

    private void log(String msg){

        Log.d(TAG, msg);
    }

    public interface Callback{

        void loadMoreData();

    }
}
