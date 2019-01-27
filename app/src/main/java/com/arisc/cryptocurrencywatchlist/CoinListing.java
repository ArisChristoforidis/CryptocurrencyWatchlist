package com.arisc.cryptocurrencywatchlist;

import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CoinListing implements Parcelable {

    //A bunch of getters/setters.Nothing to see here,move along.

    private final String TAG = "CoinListing";

    private String mId;
    private int mRank;
    private String mSymbol;
    private String mName;
    //Using Double instead of double to handle null values.
    //JSON Data.
    private Double mSupply;
    private Double mMaxSupply;
    private Double mMarketCapUsd;
    private Double mVolumeUsd24Hr;
    private Double mPrice;
    private Double mChangePercent24Hr;
    private Double mVwap24Hr;

    private Double mChangeHourly;
    private Double mChangeDaily;
    private Double mChangeWeekly;
    private Double mChangeMonthly;

    private List<HistoricalDataEntry> mLastHourData;
    private List<HistoricalDataEntry> mLastDayData;
    private List<HistoricalDataEntry> mLastWeekData;
    private List<HistoricalDataEntry> mLastMonthData;

    //This ended up not being used,I might implement it when I have time.
    private Double mMostRecentChange;

    public CoinListing(){}

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public int getRank() {
        return mRank;
    }

    public void setRank(int rank) {
        this.mRank = rank;
    }

    public String getSymbol() {
        return mSymbol;
    }

    public void setSymbol(String symbol) {
        this.mSymbol = symbol;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Double getSupply() {
        return mSupply;
    }

    public void setSupply(Double supply) {
        this.mSupply = supply;
    }

    public Double getMaxSupply() {
        return mMaxSupply;
    }

    public void setMaxSupply(Double maxSupply) {
        this.mMaxSupply = maxSupply;
    }

    public Double getMarketCapUsd() {
        return mMarketCapUsd;
    }

    public void setMarketCapUsd(Double marketCapUsd) {
        this.mMarketCapUsd = marketCapUsd;
    }

    public Double getVolumeUsd24Hr() {
        return mVolumeUsd24Hr;
    }

    public void setVolumeUsd24Hr(Double volumeUsd24Hr) {
        this.mVolumeUsd24Hr = volumeUsd24Hr;
    }

    public Double getPrice() {
        return mPrice;
    }

    public void setPrice(Double price) {
        this.mPrice = price;
    }

    public Double getChangePercent24Hr() {
        return mChangePercent24Hr;
    }

    public void setChangePercent24Hr(Double changePercent24Hr) {
        if(changePercent24Hr == null){
            Log.d(TAG,getSymbol() + ":change null");
            return;
        }
        this.mChangePercent24Hr = changePercent24Hr;
    }

    public Double getVwap24Hr() {
        return mVwap24Hr;
    }

    public void setVwap24Hr(Double vwap24Hr) {
        this.mVwap24Hr = vwap24Hr;
    }

    public Double getChangeHourly() {
        return mChangeHourly;
    }

    public void setChangeHourly(Double changeHourly) {
        this.mChangeHourly = changeHourly;
    }

    public Double getChangeDaily() {
        return mChangeDaily;
    }

    public void setChangeDaily(Double changeDaily) {
        this.mChangeDaily = changeDaily;
    }

    public Double getChangeWeekly() {
        return mChangeWeekly;
    }

    public void setChangeWeekly(Double changeWeekly) {
        this.mChangeWeekly = changeWeekly;
    }

    public Double getChangeMonthly() {
        return mChangeMonthly;
    }

    public void setChangeMonthly(Double changeMonthly) {
        this.mChangeMonthly = changeMonthly;
    }

    public List<HistoricalDataEntry> getLastHourData() {
        return mLastHourData;
    }

    public void setLastHourData(List<HistoricalDataEntry> lastHourData) {
        this.mLastHourData = lastHourData;
    }

    public List<HistoricalDataEntry> getLastDayData() {
        return mLastDayData;
    }

    public void setLastDayData(List<HistoricalDataEntry> lastDayData) {
        this.mLastDayData = lastDayData;
    }

    public List<HistoricalDataEntry> getLastWeekData() {
        return mLastWeekData;
    }

    public void setLastWeekData(List<HistoricalDataEntry> lastWeekData) {
        this.mLastWeekData = lastWeekData;
    }

    public List<HistoricalDataEntry> getLastMonthData() {
        return mLastMonthData;
    }

    public void setLastMonthData(List<HistoricalDataEntry> lastMonthData) {
        this.mLastMonthData = lastMonthData;
    }

    //TODO:Implement when you can.
    public Double getMostRecentChange() {
        return mMostRecentChange;
    }

    //TODO:Implement when you can.
    public void setMostRecentChange(double mostRecentChange) {
        this.mMostRecentChange = mostRecentChange;
    }



    private CoinListing(Parcel in){
       //This object needs to implement parcelable behaviour.
       mId = in.readString();
       mRank = in.readInt();
       mSymbol = in.readString();
       mName = in.readString();
       mSupply = (Double)in.readSerializable();
       mMaxSupply = (Double)in.readSerializable();
       mMarketCapUsd = (Double)in.readSerializable();
       mVolumeUsd24Hr = (Double)in.readSerializable();
       mPrice = (Double)in.readSerializable();
       mChangePercent24Hr = (Double)in.readSerializable();
       mVwap24Hr = (Double)in.readSerializable();
       mChangeHourly = (Double)in.readSerializable();
       mChangeDaily = (Double)in.readSerializable();
       mChangeWeekly = (Double)in.readSerializable();
       mChangeMonthly = (Double) in.readSerializable();
       if(mLastHourData == null) mLastHourData = new ArrayList<>();
       if(mLastDayData == null) mLastDayData = new ArrayList<>();
       if(mLastWeekData == null) mLastWeekData = new ArrayList<>();
       if(mLastMonthData == null) mLastMonthData = new ArrayList<>();
       in.readList(mLastHourData,HistoricalDataEntry.class.getClassLoader());
       in.readList(mLastDayData,HistoricalDataEntry.class.getClassLoader());
       in.readList(mLastWeekData,HistoricalDataEntry.class.getClassLoader());
       in.readList(mLastMonthData,HistoricalDataEntry.class.getClassLoader());

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //This object needs to implement parcelable behaviour.
        dest.writeString(mId);
        dest.writeInt(mRank);
        dest.writeString(mSymbol);
        dest.writeString(mName);
        dest.writeSerializable(mSupply);
        dest.writeSerializable(mMaxSupply);
        dest.writeSerializable(mMarketCapUsd);
        dest.writeSerializable(mVolumeUsd24Hr);
        dest.writeSerializable(mPrice);
        dest.writeSerializable(mChangePercent24Hr);
        dest.writeSerializable(mVwap24Hr);
        dest.writeSerializable(mChangeHourly);
        dest.writeSerializable(mChangeDaily);
        dest.writeSerializable(mChangeWeekly);
        dest.writeSerializable(mChangeMonthly);
        dest.writeList(mLastHourData);
        dest.writeList(mLastDayData);
        dest.writeList(mLastWeekData);
        dest.writeList(mLastMonthData);

    }

    public static final Parcelable.Creator<CoinListing> CREATOR = new Parcelable.Creator<CoinListing>(){

        @Override
        public CoinListing createFromParcel(Parcel source) {
            return new CoinListing(source);
        }

        @Override
        public CoinListing[] newArray(int size) {
            return new CoinListing[size];
        }
    };
}

//Comparators for the different sort options.
class RankComparator implements Comparator<CoinListing>{
    @Override
    public int compare(CoinListing o1, CoinListing o2) {
        return Integer.compare(o1.getRank(),o2.getRank());
    }
}

class NameComparator implements Comparator<CoinListing>{
    @Override
    public int compare(CoinListing o1, CoinListing o2) {
        String n1 = o1.getName().toUpperCase();
        String n2 = o2.getName().toUpperCase();
        return n1.compareTo(n2);
    }
}

class PriceComparator implements Comparator<CoinListing>{
    @Override
    public int compare(CoinListing o1, CoinListing o2) {
        if(o1.getPrice() == null && o2.getPrice() != null){
            return -1;
        }else if(o2.getPrice() == null && o1.getPrice() != null){
            return 1;
        }else if(o1.getPrice() == null && o2.getPrice() == null){
            return 0;
        }
        return Double.compare(o1.getPrice(),o2.getPrice());
    }
}

class ChangeComparator implements Comparator<CoinListing>{
    @Override
    public int compare(CoinListing o1, CoinListing o2) {
        if(o1.getChangePercent24Hr() == null && o2.getChangePercent24Hr() != null){
            return -1;
        }else if(o2.getChangePercent24Hr() == null && o1.getChangePercent24Hr() != null){
            return 1;
        }else if(o1.getChangePercent24Hr() == null && o2.getChangePercent24Hr() == null){
            return 0;
        }
        BigDecimal a = new BigDecimal(o1.getChangePercent24Hr());
        BigDecimal b = new BigDecimal(o2.getChangePercent24Hr());
        return a.compareTo(b);
    }
}