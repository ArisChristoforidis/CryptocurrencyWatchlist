package com.arisc.cryptocurrencywatchlist;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Utils {

    private static final String TAG = "Utils";

    private static final DecimalFormat percentageChangeDecimalFormatPattern = new DecimalFormat("+0.00;-0.00");
    private static DecimalFormat decimalFormatPattern = new DecimalFormat();


    public static Double readJSONDouble(JSONObject listing, String key){
        try {
            return listing.getDouble(key);
        } catch (JSONException e) {
            //e.printStackTrace();
            //Log.d(TAG,"Null value found on key:" + key);
            return null;
        }
    }

    public static String doubleToString(Double number){
        if(number == null) return "N/A";

        /*Some numbers are way to small so they appear as 0 which is not desirable.Other numbers
        are way too big so decimals don't matter that much.I didn't find any better way to do
        this so I am basically setting the fraction digits of a number depending on its value.Possibly
        could have been done better if I had the time.*/
        if(number > 10){
            decimalFormatPattern.setMinimumFractionDigits(2);
            decimalFormatPattern.setMaximumFractionDigits(2);
        }else if(number > 0.001){
            decimalFormatPattern.setMinimumFractionDigits(3);
            decimalFormatPattern.setMaximumFractionDigits(3);
        }else if(number > 0.00001){

            decimalFormatPattern.setMinimumFractionDigits(5);
            decimalFormatPattern.setMaximumFractionDigits(5);
        }else if(number > 0.000001){
            decimalFormatPattern.setMinimumFractionDigits(6);
            decimalFormatPattern.setMaximumFractionDigits(6);
        }else{
            decimalFormatPattern.setMinimumFractionDigits(10);
            decimalFormatPattern.setMaximumFractionDigits(10);
        }
        return decimalFormatPattern.format(number);
    }


    public static DecimalFormat getPercentageChangeDecimalFormat() {
        return percentageChangeDecimalFormatPattern;
    }

}
