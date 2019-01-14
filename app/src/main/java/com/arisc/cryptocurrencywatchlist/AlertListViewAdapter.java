package com.arisc.cryptocurrencywatchlist;

import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.nio.channels.AlreadyBoundException;
import java.util.ArrayList;
import java.util.List;

public class AlertListViewAdapter extends RecyclerView.Adapter<AlertListViewAdapter.AlertListRecyclerViewHolder>{

    private static final String TAG = "AlertListAdapter";

    List<CoinAlert> mData = new ArrayList<>();


    AlertActionListener mAlertActionListener;

    public AlertListViewAdapter(AlertActionListener alertActionListener){
        mAlertActionListener = alertActionListener;
    }

    @NonNull
    @Override
    public AlertListRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.alert_list_item,parent,false);
        AlertListRecyclerViewHolder vh = new AlertListRecyclerViewHolder(itemView,this,mAlertActionListener);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull AlertListRecyclerViewHolder holder, int position) {
        CoinAlert alert = mData.get(position);
        holder.setValues(alert);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void deleteItem(int position){
        try{
            mData.remove(position);
            notifyDataSetChanged();
        }catch(IndexOutOfBoundsException e){
            Log.e(TAG,"Tried to remove an element that does not exist.");
        }

    }

    public void setData(List<CoinAlert> data){
        Log.d(TAG,"setData was entered " + data.size());


        this.mData = data;
        notifyDataSetChanged();
    }

    public static class AlertListRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private AlertListViewAdapter mAdapter;

        private final TextView mTxtTitle;
        private final TextView mTxtCoinSymbol;
        private final TextView mTxtLowerLimit;
        private final TextView mTxtUpperLimit;
        private final ImageButton btnDeleteItem;

        AlertActionListener mAlertActionListener;

        public AlertListRecyclerViewHolder(View view,AlertListViewAdapter adapter,AlertActionListener alertActionListener) {
            super(view);

            mAdapter = adapter;

            mTxtTitle = view.findViewById(R.id.txtAlertTitle);
            mTxtCoinSymbol = view.findViewById(R.id.txtAlertCoinSymbol);
            mTxtLowerLimit = view.findViewById(R.id.txtAlertLowerLimit);
            mTxtUpperLimit = view.findViewById(R.id.txtAlertUpperLimit);

            btnDeleteItem = view.findViewById(R.id.btnAlertDelete);
            btnDeleteItem.setOnClickListener(this);

            mAlertActionListener = alertActionListener;
        }

        public void setValues(CoinAlert alert){

            String title = alert.getAlertTitle();
            mTxtTitle.setText(title);

            String coinSymbol = alert.getCoinId();
            mTxtCoinSymbol.setText(coinSymbol);

            Double lowerLimit = alert.getLowerLimit();
            if(lowerLimit == -Double.MAX_VALUE){
                mTxtLowerLimit.setText("-");
            }else{
                String strLowerLimit = "$" + lowerLimit.toString();
                mTxtLowerLimit.setText(strLowerLimit);
            }

            Double upperLimit = alert.getUpperLimit();
            if(upperLimit == Double.MAX_VALUE){
                mTxtUpperLimit.setText("-");
            }else{
                String strUpperLimit = "$" + upperLimit.toString();
                mTxtUpperLimit.setText(strUpperLimit);
            }

        }

        @Override
        public void onClick(View v) {
            mAlertActionListener.onAlertDelete(getAdapterPosition());
            //mAdapter.deleteItem(getAdapterPosition());
        }
    }

}
