package com.arisc.cryptocurrencywatchlist;

import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.greenrobot.greendao.database.Database;


public class App extends Application {

    private static final String TAG = "App";

    private DaoSession mDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        //Used for the database handling.
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,"coins-db");
        Database db = helper.getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();

        //Creates/Updates the jobService one time when the app opens.
        scheduleNotifications();
    }

    public DaoSession getDaoSession(){
        return mDaoSession;
    }

    private void scheduleNotifications(){

        //Execute every 15 minutes.
        int period = 15 * 60 * 1000;

        ComponentName componentName = new ComponentName(this,NotificationJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1,componentName)
                //Run the service when connected to any type of network(Change to NETWORK_TYPE_UNMETERED for wifi)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                //If the user reboots, the service is lost.(This is done on purpose,as a safety measure)
                .setPersisted(false)
                //Run the jobService every 15 minutes.
                .setPeriodic(period)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);
        if(resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG,"Job scheduled.");
        }else{
            Log.d(TAG,"Could not schedule job.");
        }


    }

}
