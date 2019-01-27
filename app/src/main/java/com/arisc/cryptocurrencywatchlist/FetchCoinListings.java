package com.arisc.cryptocurrencywatchlist;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearSmoothScroller;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

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
import java.util.ArrayList;
import java.util.List;

public class FetchCoinListings extends AsyncTask<String,Integer,List<CoinListing>> {

    private static final String TAG = "FetchCoinListings";
    private CoinListFragment mFragment;

    private CoinListReceivedListener mCoinListReceivedListener;

    public FetchCoinListings(CoinListFragment fragment){
        this.mFragment = fragment;
        this.mCoinListReceivedListener = fragment;
    }

    public FetchCoinListings(CoinListReceivedListener listener){
        mCoinListReceivedListener = listener;
    }

    @Override
    protected List<CoinListing> doInBackground(String... strings) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        //No api key needed for this.
        String baseUrl = "https://api.coincap.io/v2/assets?limit=2000";
        try {
            //Standard procedure.
            Uri builtURI = Uri.parse(baseUrl);

            URL url = new URL(builtURI.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if(inputStream == null){
                return new ArrayList<>();
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line).append("\n");
            }

            if(buffer.length() == 0){
                return new ArrayList<>();
            }

            String JSONString = buffer.toString();
            return getCoinListingsFromJSON(JSONString);

        } catch (MalformedURLException e) {
            Log.e(TAG,"Malformed URL Exception in doInBackground.",e);
            return  new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG,"IOException in doInBackground.",e);
            return new ArrayList<>();
        } catch (JSONException e) {
            Log.e(TAG,"JSON Exception in doInBackground(while running getCoinListingsFromJSON).",e);
            return new ArrayList<>();
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

    private List<CoinListing> getCoinListingsFromJSON(String jsonString) throws JSONException {

        final String dataKey = "data";
        final String idKey = "id";
        final String rankKey = "rank";
        final String symbolKey = "symbol";
        final String nameKey = "name";
        final String supplyKey = "supply";
        final String maxSupplyKey = "maxSupply";
        final String marketCapUsdKey = "marketCapUsd";
        final String volumeUsd24HrKey = "volumeUsd24Hr";
        final String priceKey = "priceUsd";
        final String changePercent24HrKey = "changePercent24Hr";
        final String vwap24HrKey = "vwap24Hr";

        List<CoinListing> coinListings = new ArrayList<>();

        JSONObject data = new JSONObject(jsonString);
        JSONArray topCoinsList = data.getJSONArray(dataKey);

        int listSize = topCoinsList.length();
        if(mFragment != null){
            //Inform the progressbar of the list size(on the first fetch).
            mFragment.initializeProgressBar(listSize);
        }

        for (int i = 0; i< listSize; i++){
            CoinListing cl = new CoinListing();

            JSONObject currentListing = topCoinsList.getJSONObject(i);

            String id = currentListing.getString(idKey);
            cl.setId(id);

            int rank = currentListing.getInt(rankKey);
            cl.setRank(rank);

            String symbol = currentListing.getString(symbolKey);
            cl.setSymbol(symbol);

            String name = currentListing.getString(nameKey);
            cl.setName(name);

            Double supply = Utils.readJSONDouble(currentListing,supplyKey);
            cl.setSupply(supply);

            Double maxSupply = Utils.readJSONDouble(currentListing,maxSupplyKey);
            cl.setMaxSupply(maxSupply);

            Double marketCapUsd = Utils.readJSONDouble(currentListing,marketCapUsdKey);
            cl.setMarketCapUsd(marketCapUsd);

            Double volumeUsd24Hr = Utils.readJSONDouble(currentListing,volumeUsd24HrKey);
            cl.setVolumeUsd24Hr(volumeUsd24Hr);

            Double price = Utils.readJSONDouble(currentListing,priceKey);
            cl.setPrice(price);

            Double changePercent24Hr = Utils.readJSONDouble(currentListing,changePercent24HrKey);
            cl.setChangePercent24Hr(changePercent24Hr);

            Double vwap24Hr = Utils.readJSONDouble(currentListing,vwap24HrKey);
            cl.setVwap24Hr(vwap24Hr);

            coinListings.add(cl);
            publishProgress(i);

        }
        return coinListings;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(mFragment == null) return;
        //Update progress bar.
        mFragment.updateProgress(values[0]);
    }
    
    @Override
    protected void onPostExecute(List<CoinListing> coinListings) {
        mCoinListReceivedListener.onCoinListReceived(coinListings);
    }




}
