package com.arisc.cryptocurrencywatchlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class NewsFragment extends Fragment {

    private static final String TAG = "NewsFragment";

    private List<NewsEntry> mEntries;

    View mView;
    NewsListAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        mView = inflater.inflate(R.layout.news_list_fragment,container,false);

        setupRecyclerView();
        setupRefreshLayout();

        //Run fetch news.
        new FetchNews(this).execute();

        return mView;
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = mView.findViewById(R.id.newsList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new NewsListAdapter();
        recyclerView.setAdapter(mAdapter);
    }


    private void setupRefreshLayout() {
        mSwipeRefreshLayout = mView.findViewById(R.id.newsRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                new FetchNews(NewsFragment.this).execute();
            }
        });

    }

    public void updateNewsEntries(List<NewsEntry> newsEntries) {

        if(mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);

        mEntries = newsEntries;
        mAdapter.setData(mEntries);

    }
}
