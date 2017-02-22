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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.SparseArray;

/**
 * CpuStateMonitor is a class responsible for querying the system and getting
 * the time-in-state information, as well as allowing the user to set/reset
 * offsets to "restart" the state timers
 */
public class CpuStateMonitor {

    private static final String TIME_IN_STATE_PATH =
        "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";

    private List<CpuState> mStates = new ArrayList<>();
    private SparseArray<Long> mOffsets = new SparseArray<>();

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

    /**
     * @return List of CpuState with the offsets applied
     */
    public List<CpuState> getStates() {
        List<CpuState> states = new ArrayList<>();

        /* check for an existing offset, and if it's not too big, subtract it
         * from the duration, otherwise just add it to the return List */
        for (CpuState state : mStates) {
            long duration = state.duration;
            if(mOffsets.indexOfKey(state.freq) >= 0){
                long offset = mOffsets.get(state.freq);
                if (offset <= duration) {
                    duration -= offset;
                } else {
                    /* offset > duration implies our offsets are now invalid,
                     * so clear and recall this function */
                    mOffsets.clear();
                    return getStates();
                }
            }

            states.add(new CpuState(state.freq, duration));
        }

        return states;
    }

    /**
     * @return Sum of all state durations including deep sleep, accounting
     * for offsets
     */
    public long getTotalStateTime() {
        long sum = 0;
        long offset = 0;

        for (CpuState state : mStates) {
            sum += state.duration;
        }

        for (int i = 0; i < mOffsets.size(); i++) {
            offset += mOffsets.valueAt(i);
        }

        return sum - offset;
    }

    /**
     * @return Map of freq->duration of all the offsets
     */
    SparseArray<Long> getOffsets() {
        return mOffsets;
    }

    /**
     * Sets the offset map (freq->duration offset)
     */
    void setOffsets(SparseArray<Long> offsets) {
        mOffsets = offsets;
    }

    /**
     * Updates the current time in states and then sets the offset map to the
     * current duration, effectively "zeroing out" the timers
     */
    public void setOffsets() throws CpuStateMonitorException {
        mOffsets.clear();
        updateStates();

        for (CpuState state : mStates) {
            mOffsets.put(state.freq, state.duration);
        }
    }

    /**
     * removes state offsets
     */
    public void removeOffsets() {
        mOffsets.clear();
    }

    /**
     * @return a list of all the CPU frequency states, which contains
     * both a frequency and a duration (time spent in that state
     */
    public List<CpuState> updateStates()
        throws CpuStateMonitorException {
        /* attempt to create a buffered reader to the time in state
         * file and read in the states to the class */
        try {
            InputStream is = new FileInputStream(TIME_IN_STATE_PATH);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);
            mStates.clear();
            readInStates(br);
            is.close();
        } catch (IOException e) {
            throw new CpuStateMonitorException(
                    "Problem opening time-in-states file");
        }

        /* deep sleep time determined by difference between elapsed
         * (total) boot time and the system uptime (awake) */
        long sleepTime = (SystemClock.elapsedRealtime()
                - SystemClock.uptimeMillis()) / 10;
        mStates.add(new CpuState(0, sleepTime));

        Collections.sort(mStates, Collections.reverseOrder());

        return mStates;
    }

    /**
     * read from a provided BufferedReader the state lines into the
     * States member field
     */
    private void readInStates(BufferedReader br)
        throws CpuStateMonitorException {
        try {
            String line;
            while ((line = br.readLine()) != null) {
                // split open line and convert to Integers
                String[] nums = line.split(" ");
                mStates.add(new CpuState(
                        Integer.parseInt(nums[0]),
                        Long.parseLong(nums[1])));
            }
        } catch (IOException e) {
            throw new CpuStateMonitorException(
                    "Problem processing time-in-states file");
        }
    }
}
