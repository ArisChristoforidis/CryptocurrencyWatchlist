package com.arisc.cryptocurrencywatchlist;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private AlertsListFragment mAlertListFragment = new AlertsListFragment();
    private CoinListFragment mCoinListFragment = new CoinListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        final ImageView savedCoin = toolbar.findViewById(R.id.filterSaved);
        savedCoin.setOnClickListener(mCoinListFragment);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position != 1){
                    savedCoin.setVisibility(View.INVISIBLE);
                }else{
                    savedCoin.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        //Start on the middle tab;
        mViewPager.setCurrentItem(1);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                        Intent settingsIntent = new Intent(this,SettingsActivity.class);
                        startActivity(settingsIntent);
                        return true;
            case R.id.action_signOut:
                        GoogleSignInClient client = GoogleSignInManager.getGoogleSignInClient();
                        client.signOut();
                        Intent loginIntent = new Intent(this,LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                        //I think this is redundant.
                        return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(mAlertListFragment);
        adapter.addFragment(mCoinListFragment);
        adapter.addFragment(new NewsFragment());
        viewPager.setAdapter(adapter);
    }

    public CoinListFragment getCoinListFragment() {
        return mCoinListFragment;
    }

    public AlertsListFragment getAlertListFragment() {
        return mAlertListFragment;
    }
}
