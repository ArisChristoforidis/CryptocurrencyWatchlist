package com.arisc.cryptocurrencywatchlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

    private UserAccount mSignedInAccount;

    private RecyclerView mRecyclerView;
    private CoinListRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mFirstFetch;

    private boolean mShowingSavedList = false;

    private Comparator<CoinListing> mLastUsedComparator = new RankComparator();
    private TextView mTxtSelectedSort;
    private boolean mSortReversed = false;

    private List<CoinListing> mCoinListings = new ArrayList<>();

    private int mSelectedCoinPosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        //We need to store this to call findViewById from other methods.
        mView = inflater.inflate(R.layout.coin_list_fragment,container,false);

        //Get signed account.
        mSignedInAccount = GoogleSignInManager.getSignedAccount();

        //Progress bar(Appears on first fetch only).
        setupProgressBar();
        //Swipe refresh layout(For every refresh after the first).
        setupRefreshLayout();

        //CoinListing list.
        setupCoinRecyclerView();
        new FetchCoinListings(this).execute();

        //Sort buttons.
        setupSortButtons();

        return mView;
    }

    private void setupSortButtons() {
        //These behave as toggle switches, but look better.
        TextView mTxtSortRank = mView.findViewById(R.id.txtHeaderCoinRank);
        TextView mTxtSortName = mView.findViewById(R.id.txtHeaderCoinName);
        TextView mTxtSortPrice = mView.findViewById(R.id.txtHeaderCoinPrice);
        TextView mTxtSortChange = mView.findViewById(R.id.txtHeaderCoinChange24Hr);

        mTxtSortRank.setOnClickListener(this);
        mTxtSortName.setOnClickListener(this);
        mTxtSortPrice.setOnClickListener(this);
        mTxtSortChange.setOnClickListener(this);

        if(mTxtSelectedSort == null)resetSort();

    }

    private void resetSort(){
        //Default sorting option is the rank.
        unhighlightSortOption();
        mTxtSelectedSort = mView.findViewById(R.id.txtHeaderCoinRank);
        highlightSortOption();
    }

    private void highlightSortOption(){
        if(mTxtSelectedSort == null) return;
        mTxtSelectedSort.setTextColor(ContextCompat.getColor(getContext(),R.color.colorAccent));
    }

    private void unhighlightSortOption(){
        if(mTxtSelectedSort == null) return;
        mTxtSelectedSort.setTextColor(ContextCompat.getColor(getContext(),android.R.color.tab_indicator_text));
    }

    private void setupProgressBar() {
        //Will only be shown for the first fetch.
        mProgressBar = mView.findViewById(R.id.txtFetchProgressBar);
        mFirstFetch = true;
    }

    private void setupRefreshLayout() {
        mSwipeRefreshLayout = mView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                //Fetch data.
                new FetchCoinListings(CoinListFragment.this).execute();
            }
        });
    }

    private void setupCoinRecyclerView() {
        mRecyclerView = mView.findViewById(R.id.coinList);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CoinListRecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Start the CoinDetailsActivity,and pass it the coinListing data.
                CoinListing cl = mAdapter.getCoinListItem(position);
                Intent intent = new Intent(getContext(),CoinDetailsActivity.class);
                intent.putExtra(EXTRA_COINDATA,cl);
                mSelectedCoinPosition = position;
                /*We are expecting some info when the user returns here:the historical data for the
                 selected coinListing and possibly its change in status as a saved coin.*/
                startActivityForResult(intent,RESULT_REQUEST_CODE);
            }
        }));
    }

    public void updateCoinList(List<CoinListing> listings){
        mAdapter.setCoinListData(listings);
    }

    public void initializeProgressBar(int value){
        //This is called by the async Task to initialize the progress bar for the first fetch.
        if(!mFirstFetch) return;
        mProgressBar.setMax(value);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void updateProgress(Integer val){
        //Updates the progress bar.
        if(!mFirstFetch) return;
        mProgressBar.setProgress(val);
    }

    private void finalizeProgressBar(){
        /*When the data is received,we disable the correct progress bar.(The refresh layout is not
        really a progress bar but you get the idea).*/
        if(mFirstFetch){
            mProgressBar.setVisibility(View.INVISIBLE);
            mFirstFetch = false;
        }else{
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //When the user returns to this activity from the coinDetailsActivity, this runs.
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_REQUEST_CODE && resultCode == RESULT_OK){
            //When we start CoinDetailsActivity,we get the historical data info
            //and save it to a coinListing.This updates the coins list with the
            //updated coinListing.
            CoinListing cl = data.getParcelableExtra(CoinDetailsActivity.EXTRA_UPDATEDCOINLISTING);
            mAdapter.setCoinListItem(mSelectedCoinPosition,cl);

            //Check if the user changed the selected coin's save status.
            boolean updateSaves = data.getBooleanExtra(CoinDetailsActivity.EXTRA_CHANGEDSAVESTATUS,true);
            if(updateSaves == true){
                mSignedInAccount.resetSavedCoins();
                if(mShowingSavedList){
                    //Enforce the default sort.
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
            //Update the saved coins list(visually).
            List<CoinListing> savedCoins = getSavedCoinListings();
            updateCoinList(savedCoins);
        }else{
            if(mFirstFetch){
                //Play the animation once.
                final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_animation_slide_left);
                mRecyclerView.setLayoutAnimation(controller);
                mRecyclerView.scheduleLayoutAnimation();
            }

            //Update the full list(visually).
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

        //Find the correct way to sort the list.
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
                //This is a special case: check if user pressed the filter saved button.
                ImageView savedButton = (ImageView) v;
                if(mShowingSavedList){
                    Collections.sort(mCoinListings,mLastUsedComparator);
                    if(mSortReversed) Collections.reverse(mCoinListings);
                    //Switch to the full list.
                    updateCoinList(mCoinListings);
                    savedButton.setImageResource(R.drawable.ic_star_border_white_32dp);
                }else{
                    List<CoinListing> savedCoins = getSavedCoinListings();
                    Collections.sort(savedCoins,mLastUsedComparator);
                    if(mSortReversed) Collections.reverse(savedCoins);
                    //Switch to the saved Coins list.
                    updateCoinList(savedCoins);
                    savedButton.setImageResource(R.drawable.ic_star_selected_32dp);
                }
                mShowingSavedList = !mShowingSavedList;
                Log.d(TAG,"Showing saved list:" + mShowingSavedList);
                //We need to exit this method now,everything below is for the sorting.
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

        List<SavedCoin> savedCoins = mSignedInAccount.getSavedCoins();
        //Find saved coins.
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
