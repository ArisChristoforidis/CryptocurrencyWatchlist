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

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,"coins-db");
        Database db = helper.getWritableDb();

        mDaoSession = new DaoMaster(db).newSession();

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
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(false)
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
