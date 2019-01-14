package com.arisc.cryptocurrencywatchlist;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CoinListRecyclerViewAdapter extends RecyclerView.Adapter<CoinListRecyclerViewAdapter.CoinListRecyclerViewHolder>{

    private static final String TAG = "CoinListAdapter";


    private List<CoinListing> mData = new ArrayList<>();

    @NonNull
    @Override
    public CoinListRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.coin_list_item,parent,false);
        CoinListRecyclerViewHolder vh = new CoinListRecyclerViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull CoinListRecyclerViewHolder holder, int position) {
        holder.setValues(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setCoinListData(List<CoinListing> data){
        this.mData = data;
        notifyDataSetChanged();
    }

    public CoinListing getCoinListItem(int position){
        try{
            return mData.get(position);
        }catch(IndexOutOfBoundsException e){
            Log.d(TAG,"Requested item outside of index range,returning null");
            return null;
        }
    }

    public void setCoinListItem(int position,CoinListing cl){
        try{
            mData.set(position,cl);
        }catch(IndexOutOfBoundsException e){
            Log.d(TAG,"Attempted to set item outside of index range,returning null");
        }
    }

    public void sortData(Comparator<CoinListing> comparator) {
        Collections.sort(mData,comparator);
        notifyDataSetChanged();
    }

    public void reverseData() {
        Collections.reverse(mData);
        notifyDataSetChanged();
    }

    public static class CoinListRecyclerViewHolder extends RecyclerView.ViewHolder{

        private final TextView mCoinRank;
        private final TextView mTxtCoinName;
        private final TextView mTxtCoinSymbol;
        private final TextView mTxtCoinPrice;
        private final TextView mTxtChangePercent24Hr;



        private final int valuePositive, valueNegative, valueNeutral;

        public CoinListRecyclerViewHolder(View view){
            super(view);
            mCoinRank = view.findViewById(R.id.txtCoinRank);
            mTxtCoinName = view.findViewById(R.id.txtCoinName);
            mTxtCoinSymbol = view.findViewById(R.id.txtCoinSymbol);
            mTxtCoinPrice= view.findViewById(R.id.txtCoinPrice);
            mTxtChangePercent24Hr = view.findViewById(R.id.txtChangePercent24Hr);

            valuePositive = ContextCompat.getColor(view.getContext(),R.color.valuePositive);
            valueNegative = ContextCompat.getColor(view.getContext(),R.color.valueNegative);
            valueNeutral = ContextCompat.getColor(view.getContext(),R.color.colorBlack);
        }

        public void setValues(CoinListing cl){

            String rank = String.valueOf(cl.getRank());
            mCoinRank.setText(rank);

            String name = cl.getName();
            mTxtCoinName.setText(name);

            String symbol = cl.getSymbol();
            mTxtCoinSymbol.setText(symbol);

            double coinPrice = cl.getPrice();
            String coinPriceString = "$" + Utils.doubleToString(coinPrice);
            mTxtCoinPrice.setText(coinPriceString);

            DecimalFormat decimalFormat = Utils.getPercentageChangeDecimalFormat();

            Double changePercent24Hr = cl.getChangePercent24Hr();
            //Log.d(TAG,"Reached setValues.");
            if(cl.getSymbol().equals("DEW")){
                Log.d(TAG,"DEW:" + cl.getChangePercent24Hr());
            }
            if(changePercent24Hr != null){
                String strChangePercent24Hr;
                strChangePercent24Hr = decimalFormat.format(changePercent24Hr);
                mTxtChangePercent24Hr.setText(strChangePercent24Hr + "%");
                if(changePercent24Hr > 0){
                    mTxtChangePercent24Hr.setTextColor(valuePositive);
                }else if(changePercent24Hr < 0){
                    mTxtChangePercent24Hr.setTextColor(valueNegative);
                }

            }else{
                mTxtChangePercent24Hr.setTextColor(valueNeutral);
                mTxtChangePercent24Hr.setText("N/A");
            }

        }


    }


}
