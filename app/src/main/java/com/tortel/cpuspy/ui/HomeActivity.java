//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
//
//-----------------------------------------------------------------------------

package com.tortel.cpuspy.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.tortel.cpuspy.*;
import com.tortel.cpuspy.CpuStateMonitor.CpuStateMonitorException;
import android.util.Log;

/** main activity class */
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "CpuSpy";
    public static final String DATA_LOADED = "LOADED";

    private CpuSpyApp mApp = null;
    private DataReceiver mReceiver = new DataReceiver();
    private StateFragmentAdapter mAdapter;
    private ViewPager mPager;

    /**
     * Initialize the Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the view, stash the app context, and get all UI elements
        setContentView(R.layout.home_layout);
        mApp = (CpuSpyApp)getApplicationContext();
        mAdapter = new StateFragmentAdapter(mApp, getSupportFragmentManager());
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(DATA_LOADED));

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        SmartTabLayout indicator = findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    /**
     * Update the view when the application regains focus
     */
    @Override
    public void onResume () {
        super.onResume();
        refreshData();
    }

    /**
     * Called when we want to infalte the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // request inflater from activity and inflate into its menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        // made it
        return true;
    }

    /**
     * called to handle a menu event
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
        case R.id.menu_refresh:
            refreshData();
            break;
        }

        // made it
        return true;
    }

    private class DataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
            mPager.setAdapter(mAdapter);
        }
    }

    /**
     * Attempt to update the time-in-state info
     */
    public void refreshData() {
        new RefreshStateDataTask(mApp.getBaseContext(), mApp.getCpuStateMonitor()).execute();
    }

    /**
     * Keep updating the state data off the UI thread for slow devices
     */
    protected static class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private CpuStateMonitor mMonitor;

        public RefreshStateDataTask(Context context, CpuStateMonitor monitor) {
            mMonitor = monitor;
            mContext = context;
        }

        /**
         * Stuff to do on a seperate thread
         */
        @Override
        protected Void doInBackground(Void... v) {
            try {
                mMonitor.updateStates();
            } catch (CpuStateMonitorException e) {
                Log.e(TAG, "Problem getting CPU states", e);
            }
            return null;
        }

        /**
         * Executed on the UI thread right before starting the task
         */
        @Override
        protected void onPreExecute() {
        }

        /**
         * Executed on UI thread after task
         */
        @Override
        protected void onPostExecute(Void v) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(DATA_LOADED));
        }
    }

    /**
     * logging
     */
    private void log(String s) {
        Log.d(TAG, s);
    }
}
