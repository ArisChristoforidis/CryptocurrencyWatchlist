package com.arisc.cryptocurrencywatchlist;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FetchHistoricalData extends AsyncTask <CoinDetailsActivity.Interval,Void,FetchHistoricalData.FetchHistoricalDataWrapper>{

    private static final String TAG = "FetchHistoricalData";

    private String mCoinId;
    private CoinDetailsActivity mCoinDetailsActivity;

    public FetchHistoricalData(CoinDetailsActivity coinDetailsActivity,String coinId){
        this.mCoinId = coinId;
        this.mCoinDetailsActivity = coinDetailsActivity;
    }

    @Override
    protected FetchHistoricalDataWrapper doInBackground(CoinDetailsActivity.Interval... interval) {
        //This fetches the historical data for a specific coin for a specific interval(e.g. Week)
        FetchHistoricalDataWrapper wrapper = new FetchHistoricalDataWrapper(interval[0]);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

        Calendar calendar = Calendar.getInstance();
        //By default, pick one value per day.
        String intervalParameter = "d1";

        switch(interval[0]){
            case HOUR:
                calendar.add(Calendar.HOUR,-1);
                intervalParameter = "m1";
                break;
            case DAY:
                calendar.add(Calendar.DAY_OF_WEEK,-1);
                intervalParameter = "h1";
                break;
            case WEEK:
                calendar.add(Calendar.WEEK_OF_MONTH,-1);
                intervalParameter = "h6";
                break;
            case MONTH:
                //We will be formatting this mData to display weekly mData,this is why
                //there is no case WEEK here.
                calendar.add(Calendar.MONTH,-1);
                intervalParameter = "d1";
                break;
        }

        Timestamp periodStartTimestamp = new Timestamp(calendar.getTimeInMillis());

        String strPeriodEndTimestamp = String.valueOf(currentTimestamp.getTime());
        String strPeriodStartTimestamp = String.valueOf(periodStartTimestamp.getTime());

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String baseUrl = "https://api.coincap.io/v2/assets/" + mCoinId + "/history";

        try {

            Uri builtURI = Uri.parse(baseUrl).buildUpon().
                    appendQueryParameter("interval",intervalParameter).
                    appendQueryParameter("start",strPeriodStartTimestamp).
                    appendQueryParameter("end",strPeriodEndTimestamp).build();

            URL url = new URL(builtURI.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if(inputStream == null){
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line).append("\n");
            }

            if(buffer.length() == 0){
                return null;
            }


            String JSONString = buffer.toString();

            /*We create this wrapper so that whoever uses this class knows the interval of the returned
            data.*/
            wrapper.setDataEntries(getHistoricalDataFromJSON(JSONString));
            return wrapper;

        } catch (MalformedURLException e) {
            Log.e(TAG,"Malformed URL Exception in doInBackground.",e);
            return null;
        } catch (IOException e) {
            Log.e(TAG,"IOException in doInBackground.",e);
            return null;
        } catch (JSONException e) {
            Log.e(TAG,"JSON Exception in doInBackground(while running getHistoricalDataFromJSON).",e);
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }

            if(reader != null){
                try{
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG,"IOException when closing stream.",e);
                }
            }
        }
    }

    private List<HistoricalDataEntry> getHistoricalDataFromJSON(String jsonString) throws JSONException {
        final String dataKey = "data";
        final String priceKey = "priceUsd";
        final String timeKey = "time";
        final String dateKey = "date";

        List<HistoricalDataEntry> data = new ArrayList<>();

        JSONObject jsonData = new JSONObject(jsonString);
        JSONArray jsonHistoricalDataArray = jsonData.getJSONArray(dataKey);

        for (int i=0;i<jsonHistoricalDataArray.length();i++){
            HistoricalDataEntry hde = new HistoricalDataEntry();

            JSONObject currentHistoricalDataEntry = jsonHistoricalDataArray.getJSONObject(i);

            Double price = Utils.readJSONDouble(currentHistoricalDataEntry,priceKey);
            hde.setPrice(price);

            long time = currentHistoricalDataEntry.getLong(timeKey);
            hde.setTime(time);

            String date = currentHistoricalDataEntry.getString(dateKey);
            hde.setDate(date);

            data.add(hde);
        }

        return data;
    }


    @Override
    protected void onPostExecute(FetchHistoricalDataWrapper wrapper) {
       mCoinDetailsActivity.onHistoricalDataReceived(wrapper);
    }


    public class FetchHistoricalDataWrapper{
        //In addition to the entries,this wrapper holds the corresponding interval.
        private List<HistoricalDataEntry> mDataEntries;
        private CoinDetailsActivity.Interval mInterval;


        public FetchHistoricalDataWrapper(CoinDetailsActivity.Interval interval) {
            this.mInterval = interval;
        }


        public List<HistoricalDataEntry> getDataEntries() {
            return mDataEntries;
        }

        public void setDataEntries(List<HistoricalDataEntry> dataEntries) {
            this.mDataEntries = dataEntries;
        }

        public CoinDetailsActivity.Interval getInterval() {
            return mInterval;
        }

    }
}
