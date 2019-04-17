package com.papco.sundar.cylinderinventory.screens.mainscreen;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.papco.sundar.cylinderinventory.data.Batch;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class DiffUtilRunner extends AsyncTask<List<DocumentSnapshot>,Void, DiffUtil.DiffResult> {

    private CallBack callBack;

    DiffUtilRunner(@NonNull CallBack callBack){

        this.callBack=callBack;
    }


    @Override
    protected DiffUtil.DiffResult doInBackground(List<DocumentSnapshot>... lists) {

        DiffUtil.DiffResult result=DiffUtil.calculateDiff(new FeedDiffCallback(lists[0],lists[1]));
        return result;
    }

    @Override
    protected void onPostExecute(DiffUtil.DiffResult diffResult) {
        if(callBack!=null)
            callBack.onDiffResult(diffResult);
    }

    class FeedDiffCallback extends DiffUtil.Callback{

        private List<DocumentSnapshot> oldList,newList;

        FeedDiffCallback(List<DocumentSnapshot> oldList,List<DocumentSnapshot> newList){

            this.oldList=oldList;
            this.newList=newList;

        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPosition, int newPosition) {

            Batch oldBatch,newBatch;
            oldBatch=oldList.get(oldPosition).toObject(Batch.class);
            newBatch=newList.get(newPosition).toObject(Batch.class);

            if(oldBatch==null || newBatch==null)
                return false;

            return oldBatch.getBatchNumber().equals(newBatch.getBatchNumber());
        }

        @Override
        public boolean areContentsTheSame(int i, int i1) {
            return areItemsTheSame(i,i1);
        }
    }

    public interface CallBack{
        void onDiffResult(DiffUtil.DiffResult result);
    }
}
