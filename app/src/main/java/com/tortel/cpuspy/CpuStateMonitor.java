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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * CpuStateMonitor is a class responsible for querying the system and getting
 * the time-in-state information, as well as allowing the user to set/reset
 * offsets to "restart" the state timers
 */
public class CpuStateMonitor {
    private static final String TAG = "CpuSpy";
    private static final String TIME_IN_STATE_PATH =
        "/sys/devices/system/cpu/cpu#/cpufreq/stats/time_in_state";
    private static final String CPU_INFO_PATH =
            "/proc/cpuinfo";

    private int mCpuCount;
    private List<List<CpuState>> mStates = new ArrayList<>();

    /** exception class */
    public class CpuStateMonitorException extends Exception {
        CpuStateMonitorException(String s) {
            super(s);
        }
    }

    /**
     * simple struct for states/time
     */
    public class CpuState implements Comparable<CpuState> {
        /** init with freq and duration */
        CpuState(int a, long b) { freq = a; duration = b; }

        public int freq = 0;
        public long duration = 0;

        /** for sorting, compare the freqs */
        public int compareTo(@NonNull CpuState state) {
            return Integer.compare(freq, state.freq);
        }
    }

    public int getCpuCount() {
        return mCpuCount;
    }

    /**
     * @param cpu the CPU number
     * @return List of CpuState with the offsets applied
     */
    public List<CpuState> getStates(int cpu) {
        return mStates.get(cpu);
    }

    /**
     * @return Sum of all state durations including deep sleep, accounting
     * for offsets
     */
    public long getTotalStateTime(int cpu) {
        long sum = 0;

        for (CpuState state : mStates.get(cpu)) {
            sum += state.duration;
        }

        return sum;
    }

    public void updateCpuCount() {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(CPU_INFO_PATH));
            String line = null;
            int count = 0;
            while((line = reader.readLine()) != null){
                // Log.d(TAG, "CPU info line: "+line);
                if(line.contains("processor")) {
                    count++;
                }
            }
            mCpuCount = count;
            // Log.d(TAG, "CPU count "+ mCpuCount);

            mStates.clear();

            for(int i =0; i < mCpuCount; i++){
                mStates.add(new ArrayList<CpuState>());
            }
        } catch (Exception e){
            Log.e(TAG, "Exception gettting CPU count", e);
        }
    }

    /**
     */
    public void updateStates()
        throws CpuStateMonitorException {
        if(mCpuCount == 0){
            updateCpuCount();
        }

        for(int cpu=0; cpu < mCpuCount; cpu++){
            List<CpuState> states = mStates.get(cpu);

            /* attempt to create a buffered reader to the time in state
             * file and read in the states to the class */
            try {
                String path = TIME_IN_STATE_PATH.replace('#', Character.forDigit(cpu, 10));
                // Log.d(TAG, "CPU state file path: "+ path);
                InputStream is = new FileInputStream(path);
                InputStreamReader ir = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(ir);
                states.clear();
                readInStates(br, cpu, states);
                is.close();
            } catch (IOException e) {
                throw new CpuStateMonitorException(
                        "Problem opening time-in-states file");
            }

            /* deep sleep time determined by difference between elapsed
             * (total) boot time and the system uptime (awake) */
            long sleepTime = (SystemClock.elapsedRealtime()
                    - SystemClock.uptimeMillis()) / 10;
            states.add(new CpuState(0, sleepTime));

            Collections.sort(states, Collections.reverseOrder());
        }
    }

    /**
     * read from a provided BufferedReader the state lines into the
     * States member field
     */
    private void readInStates(BufferedReader br, int cpu, List<CpuState> states)
        throws CpuStateMonitorException {
        try {
            String line;
            while ((line = br.readLine()) != null) {
                // split open line and convert to Integers
                String[] nums = line.split(" ");
                states.add(new CpuState(
                        Integer.parseInt(nums[0]),
                        Long.parseLong(nums[1])));
            }
        } catch (IOException e) {
            throw new CpuStateMonitorException(
                    "Problem processing time-in-states file");
        }
    }
}
