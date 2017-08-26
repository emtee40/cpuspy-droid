//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
// (C) Scott Warner, 2017 <Tortel1210@gmail.com>
//
//-----------------------------------------------------------------------------

package com.tortel.cpuspy;

// imports
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Application;
import android.util.Log;

/** main application class */
public class CpuSpyApp extends Application {

    private static final String KERNEL_VERSION_PATH = "/proc/version";

    private static final String TAG = "CpuSpyApp";

    private static final String PREF_NAME = "CpuSpyPreferences";
    private static final String PREF_OFFSETS = "offsets";

    /** the long-living object used to monitor the system frequency states */
    private CpuStateMonitor mMonitor = new CpuStateMonitor();

    private String mKernelVersion = "";

    /**
     * On application start, load the saved offsets and stash the
     * current kernel version string
     */
    @Override
    public void onCreate(){
        super.onCreate();
        updateKernelVersion();
    }

    /**
     * @return the kernel version string
     */
    public String getKernelVersion() {
        return mKernelVersion;
    }

    /** @return the internal CpuStateMonitor object */
    public CpuStateMonitor getCpuStateMonitor() {
        return mMonitor;
    }

    /**
     * Try to read the kernel version string from the proc fileystem
     */
    public void updateKernelVersion() {
        try {
            InputStream is = new FileInputStream(KERNEL_VERSION_PATH);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                mKernelVersion = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Problem reading kernel version file");
            mKernelVersion = getString(R.string.unknown);
        }
    }
}
