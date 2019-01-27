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

    private UserAccount mSignedInAccount;
    private List<CoinAlert> mCoinAlerts;
    private AlertListViewAdapter mAdapter;

    private TextView mSelectedChartOption;
    private TextView txtDChange1Hr, txtDChange24Hr, txtDChange1Week, txtDChange1Month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_details);
        Toolbar toolbar = findViewById(R.id.coinDetailsToolBar);
        setSupportActionBar(toolbar);

        //Get coinListing data from previous activity.
        Intent intent = getIntent();
        mCoinListing = intent.getParcelableExtra(CoinListFragment.EXTRA_COINDATA);

        //Get the signed account.This is used quite a lot so we get it once here.
        mSignedInAccount = GoogleSignInManager.getSignedAccount();

        //Save button.
        setupSaveButton();

        //"General information" textViews.
        setupInformationTextViews();

        //Line chart.
        mLineChart = findViewById(R.id.lcHistData);
        setupLineChart();
        setupChartControls();

        //Fetch historical data.
        fetchData();

        //"Create alert" FAB.
        setupFloatingActionButton();

        //Alerts list.
        setupAlertRecyclerView();
        setAlertList();
    }

    private void fetchData() {
        //Download the historical data.
        txtDChange1Hr = findViewById(R.id.txtDChange1Hr);
        txtDChange24Hr = findViewById(R.id.txtDChange24Hr);
        txtDChange1Week = findViewById(R.id.txtDChange1Week);
        txtDChange1Month = findViewById(R.id.txtDChange1Month);

        //Should the historical data be downloaded?
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
            //This is for the case where we return to the same coin after having saved historical data.
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
        /*Initialize the "Hour/Day/Week/Month" chart controls.When I started implementing this part
        I wanted to use radio buttons but their styling is quite hard so I resorted to textViews and
        it turned out really nice.Their behaviour is basically the same to that of a radio button in
        a radio button group.*/
        TextView txtHour = findViewById(R.id.txtChartChange1Hour);
        TextView txtDay = findViewById(R.id.txtChartChange1Day);
        TextView txtWeek = findViewById(R.id.txtChartChange1Week);
        TextView txtMonth = findViewById(R.id.txtChartChange1Month);

        txtHour.setOnClickListener(this);
        txtDay.setOnClickListener(this);
        txtWeek.setOnClickListener(this);
        txtMonth.setOnClickListener(this);
    }

    private void highlightChartOption() {
        if (mSelectedChartOption == null) return;
        mSelectedChartOption.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    private void unhighlightChartOption() {
        if (mSelectedChartOption == null) return;
        mSelectedChartOption.setTextColor(getResources().getColor(android.R.color.black));
    }

    @Override
    public void onClick(View v) {
        //Check if user has already selected this option.
        if (v.getId() == mSelectedChartOption.getId()) return;

        //Stop highlighting the old chart option.
        unhighlightChartOption();

        //Find the correct interval.
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
        //Highlight the new option.
        highlightChartOption();
        //Set the correct data to the chart.
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

    private void setupSaveButton() {
        //Check if the coin is saved.
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        mSavedCoinDao = daoSession.getSavedCoinDao();

        mSavedCoinQueryBuilder = mSavedCoinDao.queryBuilder();
        Long userId = mSignedInAccount.getId();
        mSavedCoinQueryBuilder.where(SavedCoinDao.Properties.MCoinId.eq(mCoinListing.getId()), SavedCoinDao.Properties.UserAccountId.eq(userId));
        /*We are expecting only one coin.If more coins come, we have a bug somewhere(Potentially
        getting saved coins of other users.)*/
        SavedCoin savedCoin = mSavedCoinQueryBuilder.unique();
        mIsCoinSaved = (savedCoin != null) ? true : false;
        mInitialSaveState = mIsCoinSaved;

        mSaveCoinButton = findViewById(R.id.imgSaveCoin);
        updateSaveButtonGraphic();
        mSaveCoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mIsCoinSaved) {
                    //Delete the saved coin.
                    mSavedCoinQueryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
                } else {
                    //Insert a new coin.
                    SavedCoin newCoin = new SavedCoin();
                    newCoin.setMCoinId(mCoinListing.getId());
                    newCoin.setUserAccountId(mSignedInAccount.getId());
                    mSavedCoinDao.insert(newCoin);
                    mSignedInAccount.getSavedCoins().add(newCoin);
                    mSignedInAccount.resetSavedCoins();
                }
                //Toggle boolean.
                mIsCoinSaved = !mIsCoinSaved;
                updateSaveButtonGraphic();
            }
        });
    }

    private void updateSaveButtonGraphic() {
        //A simple toggle for the graphic.
        if (mIsCoinSaved) {
            mSaveCoinButton.setImageResource(R.drawable.ic_star_selected_32dp);
        } else {
            mSaveCoinButton.setImageResource(R.drawable.ic_star_border_black_32dp);
        }
    }

    private void setupInformationTextViews() {
        //For the "General Information" tab.
        String coinSymbol = mCoinListing.getSymbol();
        getSupportActionBar().setTitle(coinSymbol);

        String coinName = mCoinListing.getName();
        TextView txtName = findViewById(R.id.txtDCoinName);
        txtName.setText(coinName);

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
        FloatingActionButton fab = findViewById(R.id.fabAlertCreate);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create the dialog fragment.
                AlertDialogFragment dialogFragment = new AlertDialogFragment();
                Bundle args = new Bundle();
                /*Passing the current price of the coin to the dialog fragment.This is purely a quality
                of life addition.Before, I used to forget the current price of the coin and had to
                close the dialog fragment to look at the price.*/
                args.putDouble(AlertDialogFragment.priceKey, mCoinListing.getPrice());
                dialogFragment.setArguments(args);
                dialogFragment.show(getFragmentManager(), "tag");
            }
        });
    }

    private void setupChangeTextView(TextView textView, Double change) {
        if (change == null) return;

        final int colorPositive = ContextCompat.getColor(getApplicationContext(), R.color.valuePositive);
        final int colorNegative = ContextCompat.getColor(getApplicationContext(), R.color.valueNegative);

        //Set textView text.
        DecimalFormat decimalFormat = Utils.getPercentageChangeDecimalFormat();
        String strChange = decimalFormat.format(change) + "%";
        textView.setText(strChange);

        //Set textView color.
        if (change > 0) {
            textView.setTextColor(colorPositive);
        } else if (change < 0) {
            textView.setTextColor(colorNegative);
        }
    }

    private Double calculateChange(List<HistoricalDataEntry> entries) {
        if (entries.size() < 2) return null;

        //This is just one way of calculating change.
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

        //Update the coinListing.
        updateCoinListing(wrapper.getDataEntries(), wrapper.getInterval());

        /*If it is the first fetch for this coinListing,the default selected interval is a month
        so we set the chart to display monthly data.*/
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
        //This method initializes data-independent properties of the chart.

        mLineChart.setNoDataText("Loading historical data...");
        int color = ContextCompat.getColor(getApplicationContext(),R.color.valuePositive);
        mLineChart.setNoDataTextColor(color);

        /*Marker.Uncomment if you want the chart to display the price of a touched
        point.I wrote the code but I didn't like the styling of the marker so I turned it off.*/
        /*
        CustomMarker marker = new CustomMarker(getApplicationContext(), R.layout.custom_marker);
        marker.setChartView(mLineChart);
        mLineChart.setMarker(marker);
        */

        //We just need a simple display.
        mLineChart.setFocusable(false);
        mLineChart.setPinchZoom(false);
        mLineChart.setDoubleTapToZoomEnabled(false);
        mLineChart.setScaleEnabled(false);

        //Place the X axis on the bottom.
        XAxis xAxis = mLineChart.getXAxis();
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

        /*If the selected coinListing has no data available,display an appropriate message where the
        chart should be.*/
        if (historicalDataEntries.size() == 0) {
            //This is kind of a hack but it works.
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

        /*Keep the diagram minimal.(For some reason these are LineDataSet options when it would be more
        logical to be chart options.*/
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        //Line and gradient styling.
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setLineWidth(2);
        int lineColor;
        Drawable drawable;

        /*The line/gradient color is chosen based on whether the change for the selected interval is
        positive or negative.*/
        Double change = getChange();
        if (change == null) change = 0.0;

        if (change >= 0) {
            lineColor = ContextCompat.getColor(getApplicationContext(),R.color.valuePositive);
            //Gradient below the line.
            drawable = ContextCompat.getDrawable(this, R.drawable.gradient_positive);
        } else {
            lineColor = ContextCompat.getColor(getApplicationContext(),R.color.valueNegative);
            drawable = ContextCompat.getDrawable(this, R.drawable.gradient_negative);
        }

        //Drawable styling.This gives us the green/red fade effect below the line.
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

    private void setupAlertRecyclerView() {
        RecyclerView alertsRecyclerView = findViewById(R.id.alertListD);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        alertsRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new AlertListViewAdapter(this);
        alertsRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onAlertCreated(String alertTitle, String lowerLimit, String upperLimit) {
        //Parse values of the newly created alert,create the object and handle its relations appropriately.

        //Title.
        String title;
        title = (alertTitle.isEmpty()) ? "New alert" : alertTitle;

        //Lower limit.
        Double lLimit = -Double.MAX_VALUE;
        if (lowerLimit.length() > 0) {
            lLimit = Double.valueOf(lowerLimit);
        }

        //Upper limit.
        Double uLimit = Double.MAX_VALUE;
        if (upperLimit.length() > 0) {
            uLimit = Double.valueOf(upperLimit);
        }

        //Id.
        String id = mCoinListing.getSymbol();

        //Create the object.
        CoinAlert newAlert = new CoinAlert();
        newAlert.setAlertTitle(title);
        newAlert.setCoinId(id);
        newAlert.setLowerLimit(lLimit);
        newAlert.setUpperLimit(uLimit);
        newAlert.setUserAccountId(mSignedInAccount.getId());

        //Insert into database.
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        daoSession.insert(newAlert);

        //Insert to the list.(The list is not updated automatically).
        mCoinAlerts.add(newAlert);
        setAlertList();
    }

    @Override
    public void onAlertDelete(int position) {
        CoinAlert coinAlertToDelete = mCoinAlerts.get(position);
        /*We need to delete the alert twice,once from the dao and once from the list.This is due to
        greenDao caching the table when we call alertDao.getAlerts() and actually returning us the initial
        cached version on consecutive calls(unless we reset the cached list).That means that deleting
         it from the dao won't be enough.More here: http://greenrobot.org/greendao/documentation/relations/*/
        ((App) getApplication()).getDaoSession().delete(coinAlertToDelete);
        mCoinAlerts = mSignedInAccount.getAlerts();
        mCoinAlerts.remove(coinAlertToDelete);
        setAlertList();
    }

    private void setAlertList() {
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        String coinId = mCoinListing.getSymbol();
        Long userId = mSignedInAccount.getId();

        //Get all alerts that the signed user configured(for this coin only).
        Query<CoinAlert> query = daoSession.getCoinAlertDao().queryBuilder().where(CoinAlertDao.Properties.CoinId.eq(coinId), CoinAlertDao.Properties.UserAccountId.eq(userId)).build();
        mCoinAlerts = query.list();
        mAdapter.setData(mCoinAlerts);
    }

    class YAxisFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            //This ensures that the values on the Y axis are displayed properly.
            String result = "$" + Utils.doubleToString(Double.valueOf(value));
            return result;
        }
    }

    static class XAxisFormatter implements IAxisValueFormatter {

        private SimpleDateFormat mFormat;

        public XAxisFormatter(Interval interval) {
            //Handling the possible patterns of the x Axis.
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
        /*When we return from the coinDetailsActivity to the MainActivity(specifically on the coinListFragment) we
        save the historical data on the appropriate coinListing so that when the user clicks the same
        coin again we load it from the saved state,instead of re-downloading it.This is done to
        potentially save mobile data and it can be turned off in the settings menu.*/
        Intent intent = new Intent();
        intent.putExtra(EXTRA_UPDATEDCOINLISTING, mCoinListing);
        //We also inform the coinListing of a potential change in its saved status.
        if(mIsCoinSaved != mInitialSaveState){
            intent.putExtra(EXTRA_CHANGEDSAVESTATUS,true);
        }else{
            intent.putExtra(EXTRA_CHANGEDSAVESTATUS,false);
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();

    }

}
