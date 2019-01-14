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

    private RecyclerView mRecyclerView;
    private AlertListViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private UserAccount mSignedInAccount;
    List<CoinAlert> mAlerts;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.alert_list_fragment,container,false);

        setupRecyclerView();
        setAlertList();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,new IntentFilter("update_list"));

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"AlertListFragment onResume");
        mSignedInAccount.resetAlerts();
        setAlertList();
    }

    private void setupRecyclerView() {
        mRecyclerView = mView.findViewById(R.id.alertList);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new AlertListViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
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

    private BroadcastReceiver receiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Received a broadcast.");
            mSignedInAccount.resetAlerts();
            setAlertList();
        }
    };


}
