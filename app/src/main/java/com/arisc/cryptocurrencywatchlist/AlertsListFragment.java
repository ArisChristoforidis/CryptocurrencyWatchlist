package com.arisc.cryptocurrencywatchlist;

import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class AlertsListFragment extends Fragment implements  AlertActionListener{

    private static final String TAG = "AlertsListFragment";


    private View mView;

    private AlertListViewAdapter mAdapter;

    private UserAccount mSignedInAccount;
    List<CoinAlert> mAlerts;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.alert_list_fragment,container,false);

        setupRecyclerView();
        setAlertList();

        //Register the broadcast receiver when the view is created.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,new IntentFilter("update_list"));

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*This is needed cause greenDao caches already queried lists and doesn't update them unless
         told to do so.*/
        mSignedInAccount.resetAlerts();
        setAlertList();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = mView.findViewById(R.id.alertList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new AlertListViewAdapter(this);
        recyclerView.setAdapter(mAdapter);
    }

    private void setAlertList(){
        if(mSignedInAccount == null){
            mSignedInAccount = GoogleSignInManager.getSignedAccount();
        }

        mAlerts = mSignedInAccount.getAlerts();
        mAdapter.setData(mAlerts);
    }


    @Override
    public void onAlertDelete(int position) {
        CoinAlert coinAlertToDelete = mAlerts.get(position);
        ((App)getActivity().getApplication()).getDaoSession().delete(coinAlertToDelete);
        mAlerts.remove(coinAlertToDelete);
        setAlertList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unregister the broadcast receiver when the user is about to leave the app.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*The jobService communicates with this fragment through this broadcastReceiver.
            If an update on the alerts happens while the user has the app open, this onReceive event
            will be triggered.*/
            mSignedInAccount.resetAlerts();
            setAlertList();
        }
    };


}
