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

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG,"Job started.");
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(JobParameters params){
        /*We are checking alerts for all users.We change it here to show alerts only for the user
        with the most recent sign in.*/
        mDaoSession = ((App)getApplication()).getDaoSession();
        mCoinAlertDao = mDaoSession.getCoinAlertDao();
        mAlerts = mCoinAlertDao.loadAll();

        //Only run if there are alerts.
        if(mAlerts.size() > 0){
            //Reusing FetchCoinListings because we can.
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

        /*Find the coinListings that correspond to our alerts and check if the price is is out of the
        alert limits.If it is,add it to the messages list.*/
        for(CoinAlert alert : mAlerts){
            for(CoinListing coinListing : coinListings){
                if(alert.getCoinId().equals(coinListing.getSymbol())){
                    boolean exceedsLower = coinListing.getPrice() < alert.getLowerLimit();
                    boolean exceedsUpper = coinListing.getPrice() > alert.getUpperLimit();
                    //Alert is activated here.
                    if(exceedsLower || exceedsUpper){
                        messages.add(alert.getAlertTitle());
                        //If the alert is triggered,delete it.
                        mCoinAlertDao.delete(alert);
                    }
                    break;
                }
            }
        }

        /*We need to stich the alerts together,because google doesn't want us sending multiple notifications
        on a short time.So we send 1 notification.On newer android versions there are more notification
        layouts that allow us to add more information but since I want to support older versions I am using
        a very simple notification layout.*/
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

        /*If the user has the app open and the jobService runs,we need to update the AlertListFragment.
        We do this using a localBroadCastManager.*/
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
        //This may be wrong.
        mNotificationId += 1;
        return mNotificationId;
    }
}
