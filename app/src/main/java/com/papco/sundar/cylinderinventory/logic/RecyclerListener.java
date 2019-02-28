package com.papco.sundar.cylinderinventory.logic;

import android.view.View;

public interface RecyclerListener<T> {

    void onRecyclerItemClicked(T item,int position);
    void onRecyclerItemLongClicked(T item, int position, View view);
}
