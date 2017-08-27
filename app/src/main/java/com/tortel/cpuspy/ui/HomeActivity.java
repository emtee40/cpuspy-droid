//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
//
//-----------------------------------------------------------------------------

package com.tortel.cpuspy.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tortel.cpuspy.*;
import com.tortel.cpuspy.CpuStateMonitor.CpuStateMonitorException;
import android.util.Log;

/** main activity class */
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "CpuSpy";

    private CpuSpyApp mApp = null;

    /**
     * Initialize the Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the view, stash the app context, and get all UI elements
        setContentView(R.layout.home_layout);
        mApp = (CpuSpyApp)getApplicationContext();


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

    /**
     * Attempt to update the time-in-state info
     */
    public void refreshData() {
        new RefreshStateDataTask().execute((Void)null);
    }

    /**
     * Keep updating the state data off the UI thread for slow devices
     */
    protected class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {

        /**
         * Stuff to do on a seperate thread
         */
        @Override
        protected Void doInBackground(Void... v) {
            CpuStateMonitor monitor = mApp.getCpuStateMonitor();
            try {
                monitor.updateStates();
            } catch (CpuStateMonitorException e) {
                Log.e(TAG, "Problem getting CPU states");
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
            log("finished data update");
            // TODO
        }
    }

    /**
     * logging
     */
    private void log(String s) {
        Log.d(TAG, s);
    }
}
