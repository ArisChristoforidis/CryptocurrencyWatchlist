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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class FetchNews extends AsyncTask<String,Void,List<NewsEntry>> {

    private static final String TAG = "FetchNews";

    private NewsFragment mNewsfragment;

    public FetchNews(NewsFragment fragment) {
        this.mNewsfragment = fragment;
    }

    @Override
    protected List<NewsEntry> doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String newsJSON;

        final String keyword = "cryptocurrency";

        //TODO:Check if this works correctly.
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        final String from = format.format(calendar.getTime());

        final String pageSize = "100";

        //If the app doesn't get news,its probably this.
        final String apiKey = BuildConfig.NewsApiKey;

        final String baseUrl = "https://newsapi.org/v2/everything";
        final String keywordParam = "q";
        final String fromParam = "from";
        final String pageSizeParam = "pagesize";
        final String apiKeyParam = "apiKey";



        try {
            Uri builtUri = Uri.parse(baseUrl).buildUpon()
                    .appendQueryParameter(keywordParam,keyword)
                    .appendQueryParameter(fromParam,from)
                    .appendQueryParameter(pageSizeParam,pageSize)
                    .appendQueryParameter(apiKeyParam,apiKey)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                return new ArrayList<>();
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line=reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0){
                return new ArrayList<>();
            }

            newsJSON = buffer.toString();
            return getNewsFromJSON(newsJSON);
        } catch (MalformedURLException e) {
            Log.e(TAG,"MalformedURLException. ", e);
            return new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG,"IOException. ", e);
            return new ArrayList<>();
        } catch (JSONException e) {
            Log.e(TAG,"JSONException(while running getNewsFromJSON).", e);
            return new ArrayList<>();
        } finally{
            if(urlConnection != null){
                urlConnection.disconnect();
            }

            if(reader != null){
                try{
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG,"IOException when closing stream. ", e);
                }
            }
        }
    }

    private List<NewsEntry> getNewsFromJSON(String newsJSON) throws JSONException {
        final String articleListKey = "articles";
        final String sourceKey = "source";
        final String sourceNameKey = "name";
        final String authorKey = "author";
        final String titleKey = "title";
        final String descriptionKey = "description";
        final String urlKey = "url";
        final String imageUrlKey = "urlToImage";
        final String publishedDateKey = "publishedAt";


        JSONObject newsString = new JSONObject(newsJSON);
        JSONArray newsArray = newsString.getJSONArray(articleListKey);

        List<NewsEntry> newsEntries = new ArrayList<>();
        for(int i=0;i<newsArray.length();i++){
            NewsEntry newsEntry = new NewsEntry();
            JSONObject jsonEntry = newsArray.getJSONObject(i);

            //Source.
            JSONObject jsonSource = jsonEntry.getJSONObject(sourceKey);
            String source = jsonSource.getString(sourceNameKey);
            newsEntry.setSource(source);

            //Author.
            String author = jsonEntry.getString(authorKey);
            if(author == "null"){
                author = "Unknown";
            }
            newsEntry.setAuthor(author);

            //Title.
            String title = jsonEntry.getString(titleKey);
            newsEntry.setTitle(title);

            //Description.
            String description = jsonEntry.getString(descriptionKey);
            newsEntry.setDescription(description);

            //Url.
            String url = jsonEntry.getString(urlKey);
            newsEntry.setUrl(url);

            //Image url.
            String imageUrl = jsonEntry.getString(imageUrlKey);
            newsEntry.setImageUrl(imageUrl);

            newsEntries.add(newsEntry);
        }
        return newsEntries;
    }

    @Override
    protected void onPostExecute(List<NewsEntry> newsEntries) {
        mNewsfragment.updateNewsEntries(newsEntries);
    }
}
