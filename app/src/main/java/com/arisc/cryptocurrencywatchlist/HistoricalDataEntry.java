package com.arisc.cryptocurrencywatchlist;

import android.os.Parcel;
import android.os.Parcelable;

public class HistoricalDataEntry implements Parcelable {

    private Double mPrice;
    private long mTime;
    private String mDate;

    public HistoricalDataEntry(){}

    public Double getPrice() {
        return mPrice;
    }

    public void setPrice(Double price) {
        this.mPrice = price;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        this.mTime = time;
    }

    /*We only use mTime(which is in milliseconds),this is why this is not used.I fetch the date just
    in case I need it somewhere down the line.*/
    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        /*A coinListing has historical data entries,and because that object is parcelable, this
        object needs to implement Parcelable as well.*/
        dest.writeSerializable(this.mPrice);
        dest.writeLong(this.mTime);
        dest.writeString(this.mDate);
    }

    public HistoricalDataEntry(Parcel in){
        this.mPrice = (Double)in.readSerializable();
        this.mTime = in.readLong();
        this.mDate = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public HistoricalDataEntry createFromParcel(Parcel in) {
            return new HistoricalDataEntry(in);
        }

        public HistoricalDataEntry[] newArray(int size) {
            return new HistoricalDataEntry[size];
        }
    };
}
