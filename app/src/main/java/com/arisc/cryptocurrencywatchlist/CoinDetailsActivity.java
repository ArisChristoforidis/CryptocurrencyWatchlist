package com.arisc.cryptocurrencywatchlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CoinDetailsActivity extends AppCompatActivity implements AlertDialogFragment.OnCreateAlertListener,AlertActionListener, View.OnClickListener {

    private static final String TAG = "CoinDetailsActivity";


    public static final String EXTRA_UPDATEDCOINLISTING = "com.arisc.cryptocurrencywatchlist.UPDATEDCOINLISTING";
    public static final String EXTRA_CHANGEDSAVESTATUS = "com.arisc.cryptocurrencywatchlist.CHANGEDSAVESTATUS";

    enum Interval {HOUR, DAY, WEEK, MONTH}

    private CoinListing mCoinListing;

    private LineChart mLineChart;
    private Interval mSelectedInterval = Interval.MONTH;


    ImageView mSaveCoinButton;
    private boolean mIsCoinSaved,mInitialSaveState;
    private SavedCoinDao mSavedCoinDao;
    private QueryBuilder<SavedCoin> mSavedCoinQueryBuilder;

    private FloatingActionButton mFab;

    private UserAccount mSignedInAccount;
    private List<CoinAlert> mCoinAlerts;
    private RecyclerView mAlertCoinsListView;
    private AlertListViewAdapter mAdapter;

    private TextView mSelectedChartOption;

    private TextView txtDChange1Hr, txtDChange24Hr, txtDChange1Week, txtDChange1Month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_details);
        Toolbar toolbar = findViewById(R.id.coinDetailsToolBar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mCoinListing = intent.getParcelableExtra(CoinListFragment.EXTRA_COINDATA);

        mSignedInAccount = GoogleSignInManager.getSignedAccount();

        setupSaveButton();

        setupInformationTextViews();

        mLineChart = findViewById(R.id.lcHistData);
        setupLineChart();

        setupFloatingActionButton();

        setupRecyclerView();
        setAlertList();

        fetchData();


        setupChartControls();

    }

    private void fetchData() {
        txtDChange1Hr = findViewById(R.id.txtDChange1Hr);
        txtDChange24Hr = findViewById(R.id.txtDChange24Hr);
        txtDChange1Week = findViewById(R.id.txtDChange1Week);
        txtDChange1Month = findViewById(R.id.txtDChange1Month);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean forceLoad = preferences.getBoolean("forceGraphLoad",true);

        if(forceLoad){
            Log.d(TAG,"Graphs will be downloaded.");
        }else{
            Log.d(TAG,"Graphs will be loaded from memory.");
        }

        if (mCoinListing.getLastHourData().size() == 0 || forceLoad) {
            new FetchHistoricalData(this, mCoinListing.getId()).execute(Interval.HOUR);
        }

        if (mCoinListing.getLastDayData().size() == 0 || forceLoad) {
            new FetchHistoricalData(this, mCoinListing.getId()).execute(Interval.DAY);
        }

        if (mCoinListing.getLastDayData().size() == 0 || forceLoad) {
            new FetchHistoricalData(this, mCoinListing.getId()).execute(Interval.WEEK);
        }

        if (mCoinListing.getLastMonthData().size() == 0 || forceLoad) {
            new FetchHistoricalData(this, mCoinListing.getId()).execute(Interval.MONTH);
        } else {
            mSelectedChartOption = findViewById(R.id.txtChartChange1Month);
            highlightChartOption();
            setLineChartData();
        }

        setupChangeTextView(txtDChange1Hr, mCoinListing.getChangeHourly());
        setupChangeTextView(txtDChange24Hr, mCoinListing.getChangeDaily());
        setupChangeTextView(txtDChange1Week, mCoinListing.getChangeWeekly());
        setupChangeTextView(txtDChange1Month, mCoinListing.getChangeMonthly());


    }

    private void setupChartControls() {
        TextView txtHour = findViewById(R.id.txtChartChange1Hour);
        TextView txtDay = findViewById(R.id.txtChartChange1Day);
        TextView txtWeek = findViewById(R.id.txtChartChange1Week);
        TextView txtMonth = findViewById(R.id.txtChartChange1Month);

        txtHour.setOnClickListener(this);
        txtDay.setOnClickListener(this);
        txtWeek.setOnClickListener(this);
        txtMonth.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //Check if user has already selected this option.
        if (v.getId() == mSelectedChartOption.getId()) return;

        unhighlightChartOption();
        switch (v.getId()) {
            case R.id.txtChartChange1Hour:
                mSelectedInterval = Interval.HOUR;
                break;
            case R.id.txtChartChange1Day:
                mSelectedInterval = Interval.DAY;
                break;
            case R.id.txtChartChange1Week:
                mSelectedInterval = Interval.WEEK;
                break;
            case R.id.txtChartChange1Month:
                mSelectedInterval = Interval.MONTH;
                break;
        }

        mSelectedChartOption = (TextView) v;
        highlightChartOption();
        setLineChartData();
    }

    private List<HistoricalDataEntry> getSelectedData(){
        switch (mSelectedInterval){
            case HOUR:
                return  mCoinListing.getLastHourData();
            case DAY:
                return mCoinListing.getLastDayData();
            case WEEK:
                return mCoinListing.getLastWeekData();
            case MONTH:
                default:
                return mCoinListing.getLastMonthData();
        }
    }

    private void highlightChartOption() {
        if (mSelectedChartOption == null) return;

        mSelectedChartOption.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    private void unhighlightChartOption() {
        if (mSelectedChartOption == null) return;

        mSelectedChartOption.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void setupSaveButton() {
        //Check if it is saved.
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        mSavedCoinDao = daoSession.getSavedCoinDao();

        mSavedCoinQueryBuilder = mSavedCoinDao.queryBuilder();
        SavedCoin savedCoin;
        Long userId = mSignedInAccount.getId();
        mSavedCoinQueryBuilder.where(SavedCoinDao.Properties.MCoinId.eq(mCoinListing.getId()), SavedCoinDao.Properties.UserAccountId.eq(userId));
        savedCoin = mSavedCoinQueryBuilder.unique();

        mSaveCoinButton = findViewById(R.id.imgSaveCoin);

        if (savedCoin != null) {
            mIsCoinSaved = true;
        } else {
            mIsCoinSaved = false;
        }
        mInitialSaveState = mIsCoinSaved;
        updateSaveButtonGraphic();


        mSaveCoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mIsCoinSaved) {
                    mSavedCoinQueryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
                } else {
                    SavedCoin newCoin = new SavedCoin();
                    newCoin.setMCoinId(mCoinListing.getId());
                    UserAccount account = GoogleSignInManager.getSignedAccount();
                    newCoin.setUserAccountId(account.getId());
                    mSavedCoinDao.insert(newCoin);

                    account.getSavedCoins().add(newCoin);
                    account.resetSavedCoins();

                }
                mIsCoinSaved = !mIsCoinSaved;
                //TODO:Figure out if this works.
                updateSaveButtonGraphic();
            }
        });

    }

    private void updateSaveButtonGraphic() {
        if (mIsCoinSaved) {
            mSaveCoinButton.setImageResource(R.drawable.ic_star_selected_32dp);
        } else {
            mSaveCoinButton.setImageResource(R.drawable.ic_star_border_black_32dp);
        }
    }

    private void setupInformationTextViews() {

        String coinSymbol = mCoinListing.getSymbol();
        getSupportActionBar().setTitle(coinSymbol);

        String coinName = mCoinListing.getName();
        TextView txtName = findViewById(R.id.txtDCoinName);
        txtName.setText(coinName);

        /*
        TextView txtSymbol = findViewById(R.id.txtDCoinSymbol);
        txtSymbol.setText(coinSymbol);
        */


        //Price.
        Double price = mCoinListing.getPrice();
        if (price != null) {
            String strPrice = "$" + Utils.doubleToString(mCoinListing.getPrice());
            TextView txtPrice = findViewById(R.id.txtDCoinPrice);
            txtPrice.setText(strPrice);
        }


        DecimalFormat intFormat = new DecimalFormat("#");
        //Supply.
        Double supply = mCoinListing.getSupply();
        if (supply != null) {
            String strSupply = intFormat.format(supply);
            TextView txtSupply = findViewById(R.id.txtDCoinSupply);
            txtSupply.setText(strSupply);
        }

        //Max Supply.
        Double maxSupply = mCoinListing.getMaxSupply();
        if (maxSupply != null) {
            String strMaxSupply = intFormat.format(maxSupply);
            TextView txtMaxSupply = findViewById(R.id.txtDCoinMaxSupply);
            txtMaxSupply.setText(strMaxSupply);
        }

        //Market Cap.
        Double marketCapUsd = mCoinListing.getMarketCapUsd();
        if (marketCapUsd != null) {
            String strMarketCapUsd = "$" + Utils.doubleToString(marketCapUsd);
            TextView txtCoinMarketCap = findViewById(R.id.txtDCoinMarketCap);
            txtCoinMarketCap.setText(strMarketCapUsd);
        }

        //Volume 24Hr.
        Double volumeUsd24Hr = mCoinListing.getVolumeUsd24Hr();
        if (volumeUsd24Hr != null) {
            String strVolumeUsd24Hr = "$" + Utils.doubleToString(volumeUsd24Hr);
            TextView txtCoinTradingVolume24Hr = findViewById(R.id.txtDCoinVolume24Hr);
            txtCoinTradingVolume24Hr.setText(strVolumeUsd24Hr);
        }

        //Vwap 24Hr.
        Double vWap24Hr = mCoinListing.getVwap24Hr();
        if (vWap24Hr != null) {
            String coinVwap24Hr = "$" + Utils.doubleToString(mCoinListing.getVwap24Hr());
            TextView txtCoinVwap24Hr = findViewById(R.id.txtDCoinVwap24Hr);
            txtCoinVwap24Hr.setText(coinVwap24Hr);
        }


    }

    private void setupFloatingActionButton() {
        mFab = findViewById(R.id.fabAlertCreate);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogFragment dialogFragment = new AlertDialogFragment();
                Bundle args = new Bundle();
                args.putDouble(AlertDialogFragment.priceKey, mCoinListing.getPrice());
                dialogFragment.setArguments(args);
                dialogFragment.show(getFragmentManager(), "tag");
                //dialogFragment.setupViewsAndEvents();
            }
        });

    }

    private void setupChangeTextView(TextView textView, Double change) {
        if (change == null) return;

        final int colorPositive = ContextCompat.getColor(getApplicationContext(), R.color.valuePositive);
        final int colorNegative = ContextCompat.getColor(getApplicationContext(), R.color.valueNegative);


        DecimalFormat decimalFormat = Utils.getPercentageChangeDecimalFormat();

        String strChange = decimalFormat.format(change);

        textView.setText(strChange + "%");

        if (change > 0) {
            textView.setTextColor(colorPositive);
        } else if (change < 0) {
            textView.setTextColor(colorNegative);
        }

    }

    private Double calculateChange(List<HistoricalDataEntry> entries) {
        Log.d(TAG, "Entries Size:" + entries.size());
        if (entries.size() < 2) return null;


        HistoricalDataEntry start = entries.get(0);
        HistoricalDataEntry end = entries.get(entries.size() - 1);
        Double change = (end.getPrice() - start.getPrice()) / start.getPrice() * 100;
        return change;
    }

    public void onHistoricalDataReceived(FetchHistoricalData.FetchHistoricalDataWrapper wrapper) {
        if (wrapper == null) {
            Log.d(TAG, "FetchHistoricalDataWrapper is null");
            return;
        }

        updateCoinListing(wrapper.getDataEntries(), wrapper.getInterval());

        //TODO:This is temp.
        if (mSelectedChartOption == null && wrapper.getInterval() == Interval.MONTH) {
            mSelectedChartOption = findViewById(R.id.txtChartChange1Month);
            highlightChartOption();
            setLineChartData();
        }

    }

    private void updateCoinListing(List<HistoricalDataEntry> historicalDataEntries, Interval interval) {

        Log.d(TAG, "Received " + historicalDataEntries.size() + " entries for " + interval.name() + " interval");

        switch (interval) {
            case HOUR:
                mCoinListing.setLastHourData(historicalDataEntries);
                mCoinListing.setChangeHourly(calculateChange(historicalDataEntries));

                setupChangeTextView(txtDChange1Hr, mCoinListing.getChangeHourly());
                break;
            case DAY:
                mCoinListing.setLastDayData(historicalDataEntries);
                mCoinListing.setChangeDaily(calculateChange(historicalDataEntries));

                setupChangeTextView(txtDChange24Hr, mCoinListing.getChangeDaily());
                break;
            case WEEK:
                mCoinListing.setLastWeekData(historicalDataEntries);
                mCoinListing.setChangeWeekly(calculateChange(historicalDataEntries));
                setupChangeTextView(txtDChange1Week, mCoinListing.getChangeWeekly());
            case MONTH:
                mCoinListing.setLastMonthData(historicalDataEntries);
                mCoinListing.setChangeMonthly(calculateChange(historicalDataEntries));

                setupChangeTextView(txtDChange1Month, mCoinListing.getChangeMonthly());
                break;
        }

    }

    private void setupLineChart() {
        mLineChart.setNoDataText("Loading historical data...");
        int color = getResources().getColor(R.color.valuePositive);
        mLineChart.setNoDataTextColor(color);

        //Marker.
        /*
        CustomMarker marker = new CustomMarker(getApplicationContext(), R.layout.custom_marker);
        marker.setChartView(mLineChart);
        mLineChart.setMarker(marker);
        */

        mLineChart.setFocusable(false);
        mLineChart.setPinchZoom(false);
        mLineChart.setDoubleTapToZoomEnabled(false);
        mLineChart.setScaleEnabled(false);

        XAxis xAxis = mLineChart.getXAxis();
        //Place the X axis on the bottom.
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        //Disable the right Y Axis.
        YAxis yAxisRight = mLineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        //Format axis values.
        YAxis yAxisLeft = mLineChart.getAxisLeft();
        yAxisLeft.setValueFormatter(new YAxisFormatter());


        //Disable description.
        Description chartDescription = mLineChart.getDescription();
        chartDescription.setEnabled(false);

        //Disable legend.
        Legend chartLegend = mLineChart.getLegend();
        chartLegend.setEnabled(false);

    }

    private Double getChange(){
        Double change = 0.0;
        switch (mSelectedInterval) {
            case HOUR:
                change = mCoinListing.getChangeHourly();
                break;
            case DAY:
                change = mCoinListing.getChangeDaily();
                break;
            case WEEK:
                change = mCoinListing.getChangeWeekly();
                break;
            case MONTH:
                change = mCoinListing.getChangeMonthly();
                break;
        }
        return change;
    }

    public void setLineChartData() {

        List<HistoricalDataEntry> historicalDataEntries = getSelectedData();
        Double change = getChange();


        if (historicalDataEntries.size() == 0) {
            mLineChart.setNoDataText("No data.");
            mLineChart.setData(null);
            mLineChart.invalidate();
            return;
        }

        List<Entry> data = new ArrayList<>();
        for (int i = 0; i < historicalDataEntries.size(); i++) {
            data.add(new Entry(historicalDataEntries.get(i).getTime(), (float) (double) historicalDataEntries.get(i).getPrice()));
        }

        LineDataSet dataSet = new LineDataSet(data, "Line Graph");
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        //Line Styling.
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setLineWidth(2);
        int lineColor;
        Drawable drawable;

        if (change == null) change = 0.0;

        if (change >= 0) {
            lineColor = getResources().getColor(R.color.valuePositive);
            //Gradient below the line.
            drawable = ContextCompat.getDrawable(this, R.drawable.gradient_positive);

        } else {
            lineColor = getResources().getColor(R.color.valueNegative);
            drawable = ContextCompat.getDrawable(this, R.drawable.gradient_negative);
        }


        //Drawable styling.
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(drawable);

        //Don't draw the highlight "cross".
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setDrawVerticalHighlightIndicator(false);

        dataSet.setColor(lineColor);
        LineData lineData = new LineData(dataSet);
        mLineChart.setData(lineData);

        //Setup x-axis scale.
        XAxis axis = mLineChart.getXAxis();
        axis.setValueFormatter(new XAxisFormatter(mSelectedInterval));

        //This refreshes the graph.
        mLineChart.invalidate();
        //Add a simple animation.
        mLineChart.animateX(600, Easing.EaseInOutSine);


    }

    private void setupRecyclerView() {
        mAlertCoinsListView = findViewById(R.id.alertListD);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mAlertCoinsListView.setLayoutManager(linearLayoutManager);

        mAdapter = new AlertListViewAdapter(this);
        mAlertCoinsListView.setAdapter(mAdapter);
    }

    @Override
    public void onAlertCreated(String alertTitle, String lowerLimit, String upperLimit) {
        //Parse values correctly.
        String title;
        if (alertTitle.isEmpty()) {
            title = "New alert";
        } else {
            title = alertTitle;
        }

        String id = mCoinListing.getSymbol();

        Double lLimit = -Double.MAX_VALUE;
        if (lowerLimit.length() > 0) {
            lLimit = Double.valueOf(lowerLimit);
        }


        Double uLimit = Double.MAX_VALUE;
        if (upperLimit.length() > 0) {
            uLimit = Double.valueOf(upperLimit);
        }

        mCoinAlerts = mSignedInAccount.getAlerts();

        CoinAlert newAlert = new CoinAlert();
        newAlert.setAlertTitle(title);
        newAlert.setCoinId(id);
        newAlert.setLowerLimit(lLimit);
        newAlert.setUpperLimit(uLimit);

        newAlert.setUserAccountId(mSignedInAccount.getId());

        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        daoSession.insert(newAlert);

        mCoinAlerts.add(newAlert);
        setAlertList();

    }

    @Override
    public void onAlertDelete(int position) {
        CoinAlert coinAlertToDelete = mCoinAlerts.get(position);
        ((App) getApplication()).getDaoSession().delete(coinAlertToDelete);
        mCoinAlerts = mSignedInAccount.getAlerts();
        mCoinAlerts.remove(coinAlertToDelete);
        setAlertList();
    }

    private void setAlertList() {

        Query<CoinAlert> query = null;
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        String coinId = mCoinListing.getSymbol();
        Long userId = mSignedInAccount.getId();
        query = daoSession.getCoinAlertDao().queryBuilder().where(CoinAlertDao.Properties.CoinId.eq(coinId), CoinAlertDao.Properties.UserAccountId.eq(userId)).build();

        mCoinAlerts = query.list();

        mAdapter.setData(mCoinAlerts);
    }

    class YAxisFormatter implements IAxisValueFormatter {


        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            String result = "$" + Utils.doubleToString(Double.valueOf(value));
            return result;
        }
    }

    static class XAxisFormatter implements IAxisValueFormatter {


        private SimpleDateFormat mFormat;

        public XAxisFormatter(Interval interval) {
            switch (interval) {
                case HOUR:
                    mFormat = new SimpleDateFormat("HH:mm");
                    break;
                case DAY:
                    mFormat = new SimpleDateFormat("HH:mm");
                    break;
                case WEEK:
                    mFormat = new SimpleDateFormat("E dd");
                    break;
                case MONTH:
                    mFormat = new SimpleDateFormat("E dd");
                    break;
            }


        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Date date = new Date((long) value);
            return mFormat.format(date);
        }
    }

    @Override
    public void onBackPressed() {
        //TODO:Implement this as an option?
        /*Update the mirrored coinListing on the CoinListFragment.
        This feature doesn't actually do anything.The idea was that if the data is downloaded
        once for a coin,we don't need to download it again every time we open the activity.Unfortunately,
        this means that the line chart will lag in its animation because it attempts to load the data
        right on the activity start,when the device is under heavy load for a few milliseconds.
        Of course we can add checks on the onCreate method(e.g. if(mCoinListing.getLastHourData().isEmpty()))
        to prevent the async task from running multiple times and save mobile data.*/
        Intent intent = new Intent();
        intent.putExtra(EXTRA_UPDATEDCOINLISTING, mCoinListing);

        if(mIsCoinSaved != mInitialSaveState){
            intent.putExtra(EXTRA_CHANGEDSAVESTATUS,true);
        }else{
            intent.putExtra(EXTRA_CHANGEDSAVESTATUS,false);
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();

    }


}
