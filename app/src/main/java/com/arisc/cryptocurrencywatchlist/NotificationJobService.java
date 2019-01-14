package com.arisc.cryptocurrencywatchlist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NotificationJobService extends JobService implements CoinListReceivedListener{

    private static final String TAG = "NotificationJobService";
    private boolean mJobCancelled = false;
    private static String NOTIFICATION_CHANNEL_ID = "cryptocurrencyWatchlist_default";

    private FetchCoinListings mFetchCoinListings;

    private int mNotificationId = 0;

    DaoSession mDaoSession;
    CoinAlertDao mCoinAlertDao;
    List<CoinAlert> mAlerts;

    private ServiceCallback mServiceCallback;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG,"Job started.");
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(JobParameters params){
        mDaoSession = ((App)getApplication()).getDaoSession();
        mCoinAlertDao = mDaoSession.getCoinAlertDao();
        mAlerts = mCoinAlertDao.loadAll();

        if(mAlerts.size() > 0){
            mFetchCoinListings = new FetchCoinListings(this);
            mFetchCoinListings.execute();
        }

        jobFinished(params,false);

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG,"Job cancelled before completion");
        mJobCancelled = true;
        mFetchCoinListings.cancel(true);
        return true;
    }

    @Override
    public void onCoinListReceived(List<CoinListing> coinListings) {
        List<String> messages = new ArrayList<>();

        for(CoinAlert alert : mAlerts){
            for(CoinListing coinListing : coinListings){
                if(alert.getCoinId().equals(coinListing.getSymbol())){
                    Log.d(TAG,"CoinListing Price:" +coinListing.getPrice());
                    Log.d(TAG,"Upper:" + alert.getUpperLimit());
                    Log.d(TAG,"Lower:" + alert.getLowerLimit());
                    boolean exceedsLower = coinListing.getPrice() < alert.getLowerLimit();
                    boolean exceedsUpper = coinListing.getPrice() > alert.getUpperLimit();
                    if(exceedsLower || exceedsUpper){
                        //Make notification etc.
                        //message = alert.getAlertTitle() + ":" + alert.getCoinId() + " value is $" + coinListing.getPrice();
                        messages.add(alert.getAlertTitle());

                        mCoinAlertDao.delete(alert);
                        GoogleSignInManager.getSignedAccount().getAlerts().remove(alert);
                    }
                    break;
                }
            }
        }

        String finalMessage = "";
        if(messages.size() == 0){
            return;
        }else{
            for(int i = 0;i<messages.size();i++){
                finalMessage += messages.get(i);
                if(i == messages.size() -1) continue;
                finalMessage += ",";
            }
        }

        Intent intent = new Intent(this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_coin_unselected)
                .setContentTitle("Coin Updates")
                .setContentText(finalMessage)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        createNotificationChannel();

        NotificationManagerCompat notificationManager=  NotificationManagerCompat.from(this);
        notificationManager.notify(getNotificationId(),builder.build());

        if(mServiceCallback != null){
            mServiceCallback.onAlertActivated();
        }


        Intent alertListIntent = new Intent("update_list");
        LocalBroadcastManager.getInstance(this).sendBroadcast(alertListIntent);

    }


    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"CryptocurrencyWatchlistChannel", importance);
            channel.setDescription("The default channel for the app \"cryptocurrency watchlist\".Handles the alert notifications.");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
    }

    int getNotificationId(){
        mNotificationId += 1;
        return mNotificationId;
    }

    public class LocalBinder extends Binder{
        NotificationJobService getService(){
            return NotificationJobService.this;
        }
    }

    public interface ServiceCallback{
        void onAlertActivated();
    }
}
