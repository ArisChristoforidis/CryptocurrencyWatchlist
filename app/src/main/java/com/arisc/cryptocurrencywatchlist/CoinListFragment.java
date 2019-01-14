package com.arisc.cryptocurrencywatchlist;

import android.content.Intent;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class CoinListFragment extends Fragment implements CoinListReceivedListener, View.OnClickListener {

    private static final String TAG = "CoinListFragment";

    public static final String EXTRA_COINDATA = "com.arisc.cryptocurrencywatchlist.COINDATA";
    public static final int RESULT_REQUEST_CODE = 1;

    private View mView;

    private RecyclerView mRecyclerView;
    private CoinListRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mFirstFetch;

    private TextView mTxtSelectedSort;

    private boolean mShowingSavedList = false;

    private int mSelectedCoinPosition = 0;

    private List<CoinListing> mCoinListings = new ArrayList<>();

    private Comparator<CoinListing> mLastUsedComparator = new RankComparator();
    private boolean mSortReversed = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {


        mView = inflater.inflate(R.layout.coin_list_fragment,container,false);

        setupProgressBar();
        setupRefreshLayout();

        setupRecyclerView();
        setupSortButtons();


        return mView;
    }


    private void setupSortButtons() {
        TextView mTxtSortRank = mView.findViewById(R.id.txtHeaderCoinRank);
        TextView mTxtSortName = mView.findViewById(R.id.txtHeaderCoinName);
        TextView mTxtSortPrice = mView.findViewById(R.id.txtHeaderCoinPrice);
        TextView mTxtSortChange = mView.findViewById(R.id.txtHeaderCoinChange24Hr);

        mTxtSortRank.setOnClickListener(this);
        mTxtSortName.setOnClickListener(this);
        mTxtSortPrice.setOnClickListener(this);
        mTxtSortChange.setOnClickListener(this);

        if(mTxtSelectedSort == null){
            resetSort();
        }

    }

    private void resetSort(){
        unhighlightSortOption();
        mTxtSelectedSort = mView.findViewById(R.id.txtHeaderCoinRank);
        highlightSortOption();
    }

    private void highlightSortOption(){
        if(mTxtSelectedSort == null) return;

        mTxtSelectedSort.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    private void unhighlightSortOption(){
        if(mTxtSelectedSort == null) return;

        mTxtSelectedSort.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
    }


    private void setupProgressBar() {
        mProgressBar = mView.findViewById(R.id.txtFetchProgressBar);
        mFirstFetch = true;
    }

    private void setupRefreshLayout() {
        mSwipeRefreshLayout = mView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                new FetchCoinListings(CoinListFragment.this).execute();
            }
        });
    }

    private void setupRecyclerView() {
        mRecyclerView = mView.findViewById(R.id.coinList);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CoinListRecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                CoinListing cl = mAdapter.getCoinListItem(position);
                Intent intent = new Intent(getContext(),CoinDetailsActivity.class);
                intent.putExtra(EXTRA_COINDATA,cl);
                mSelectedCoinPosition = position;
                startActivityForResult(intent,RESULT_REQUEST_CODE);
                //startActivity(intent);
            }
        }));


        new FetchCoinListings(this).execute();
    }

    public void updateCoinList(List<CoinListing> listings){
        mAdapter.setCoinListData(listings);
    }

    public void initializeProgressBar(int value){
        if(!mFirstFetch) return;

        mProgressBar.setMax(value);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void updateProgress(Integer val){
        if(!mFirstFetch) return;

        mProgressBar.setProgress(val);
    }

    private void finalizeProgressBar(){
        if(mFirstFetch){
            mProgressBar.setVisibility(View.INVISIBLE);
            mFirstFetch = false;
        }else{
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_REQUEST_CODE && resultCode == RESULT_OK){
            //When we start CoinDetailsActivity,we get the historical data info
            //and save it to a coinListing.This updates the coins list with the
            //updated coinListing.
            CoinListing cl = data.getParcelableExtra(CoinDetailsActivity.EXTRA_UPDATEDCOINLISTING);
            mAdapter.setCoinListItem(mSelectedCoinPosition,cl);

            boolean updateSaves = data.getBooleanExtra(CoinDetailsActivity.EXTRA_CHANGEDSAVESTATUS,true);
            if(updateSaves == true){
                UserAccount account = GoogleSignInManager.getSignedAccount();
                account.resetSavedCoins();
                if(mShowingSavedList){
                    List<CoinListing> savedCoins = getSavedCoinListings();
                    Collections.sort(savedCoins,mLastUsedComparator);
                    updateCoinList(savedCoins);
                }

            }
        }

    }

    @Override
    public void onCoinListReceived(List<CoinListing> coinListings) {
        resetSort();
        mCoinListings = coinListings;
        if(mShowingSavedList){
            List<CoinListing> savedCoins = getSavedCoinListings();
            updateCoinList(savedCoins);
        }else{

            if(mFirstFetch){
                final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_animation_slide_left);
                mRecyclerView.setLayoutAnimation(controller);
                mRecyclerView.scheduleLayoutAnimation();
            }

            updateCoinList(mCoinListings);
        }
        finalizeProgressBar();
    }

    @Override
    public void onClick(View v) {

        //Handle reversing the list for ascending and descending sorts.
        if(mTxtSelectedSort.getId() == v.getId()){
            mSortReversed = !mSortReversed;
            mAdapter.reverseData();
            return;
        }else if(v.getId() != R.id.filterSaved){
            mSortReversed = false;
        }

        Comparator<CoinListing> comparator = null;
        switch(v.getId()){
            case R.id.txtHeaderCoinRank:
                comparator = new RankComparator();
                break;
            case R.id.txtHeaderCoinName:
                comparator = new NameComparator();
                break;
            case R.id.txtHeaderCoinPrice:
                comparator = new PriceComparator();
                break;
            case R.id.txtHeaderCoinChange24Hr:
                comparator = new ChangeComparator();
                break;
            case R.id.filterSaved:
                //Check if user pressed the filter saved button.
                ImageView savedButton = (ImageView) v;
                if(mShowingSavedList){
                    Collections.sort(mCoinListings,mLastUsedComparator);
                    if(mSortReversed) Collections.reverse(mCoinListings);
                    updateCoinList(mCoinListings);
                    savedButton.setImageResource(R.drawable.ic_star_border_white_32dp);
                }else{
                    List<CoinListing> savedCoins = getSavedCoinListings();
                    Collections.sort(savedCoins,mLastUsedComparator);
                    if(mSortReversed) Collections.reverse(savedCoins);
                    updateCoinList(savedCoins);
                    savedButton.setImageResource(R.drawable.ic_star_selected_32dp);
                }
                mShowingSavedList = !mShowingSavedList;
                Log.d(TAG,"Showing saved list:" + mShowingSavedList);
                return;
        }

        mLastUsedComparator = comparator;

        unhighlightSortOption();
        mTxtSelectedSort = (TextView) v;
        highlightSortOption();

        mAdapter.sortData(comparator);
    }

    public List<CoinListing> getSavedCoinListings() {
        List<CoinListing> savedCoinListings = new ArrayList<>();

        UserAccount userAccount = GoogleSignInManager.getSignedAccount();
        List<SavedCoin> savedCoins = userAccount.getSavedCoins();

        for(SavedCoin savedCoin : savedCoins){
            for(CoinListing coinlisting : mCoinListings){
                if(savedCoin.getMCoinId().equals(coinlisting.getId())){
                    savedCoinListings.add(coinlisting);
                    break;
                }
            }
        }



        return savedCoinListings;
    }

    public interface ClickListener{
        void onClick(View view,int position);
    }
}
