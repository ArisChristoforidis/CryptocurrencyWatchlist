package com.arisc.cryptocurrencywatchlist;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
    /*Used to see which item is selected on the coinList.Not much creativity here,just following
    the tutorials.*/

    private static final String TAG = "RecyclerTouchListener";

    private CoinListFragment.ClickListener clickListener;
    private GestureDetector gestureDetector;

    public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final CoinListFragment.ClickListener clickListener){
        this.clickListener = clickListener;

        gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View item = rv.findChildViewUnder(e.getX(),e.getY());
        if(item != null && clickListener != null && gestureDetector.onTouchEvent(e)){
            clickListener.onClick(item,rv.getChildAdapterPosition(item));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
}
