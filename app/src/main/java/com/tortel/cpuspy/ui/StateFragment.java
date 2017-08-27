package com.tortel.cpuspy.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tortel.cpuspy.CpuSpyApp;
import com.tortel.cpuspy.CpuStateMonitor;
import com.tortel.cpuspy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that shows the CPU state info
 */
public class StateFragment extends Fragment {
    public static final String CPU = "cpu";

    // the views
    private LinearLayout mStatesView = null;
    private TextView mAdditionalStates = null;
    private TextView mTotalStateTime = null;
    private TextView mHeaderAdditionalStates = null;
    private TextView mHeaderTotalStateTime = null;
    private TextView mStatesWarning = null;
    private TextView mKernelString = null;

    private int mCpu;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCpu = getArguments().getInt(CPU);
        mApp = (CpuSpyApp) getActivity().getApplicationContext();
    }

    private CpuSpyApp mApp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.state_fragment, container, false);
        mStatesView = view.findViewById(R.id.ui_states_view);
        mKernelString = view.findViewById(R.id.ui_kernel_string);
        mAdditionalStates = view.findViewById(
                R.id.ui_additional_states);
        mHeaderAdditionalStates = view.findViewById(
                R.id.ui_header_additional_states);
        mHeaderTotalStateTime = view.findViewById(
                R.id.ui_header_total_state_time);
        mStatesWarning = view.findViewById(R.id.ui_states_warning);
        mTotalStateTime = view.findViewById(R.id.ui_total_state_time);

        updateView();

        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updateView();
    }

    /**
     * Generate and update all UI elements
     */
    public void updateView() {
        /* Get the CpuStateMonitor from the app, and iterate over all states,
         * creating a row if the duration is > 0 or otherwise marking it in
         * extraStates (missing) */
        CpuStateMonitor monitor = mApp.getCpuStateMonitor();
        mStatesView.removeAllViews();
        List<String> extraStates = new ArrayList<>();
        for (CpuStateMonitor.CpuState state : monitor.getStates(mCpu)) {
            if (state.duration > 0) {
                generateStateRow(state, mStatesView);
            } else {
                if (state.freq == 0) {
                    extraStates.add(getString(R.string.deep_sleep));
                } else {
                    extraStates.add(state.freq/1000 + " MHz");
                }
            }
        }

        // show the red warning label if no states found
        if (monitor.getStates(mCpu).size() == 0) {
            mStatesWarning.setVisibility(View.VISIBLE);
            mHeaderTotalStateTime.setVisibility(View.GONE);
            mTotalStateTime.setVisibility(View.GONE);
            mStatesView.setVisibility(View.GONE);
        }

        // update the total state time
        long totTime = monitor.getTotalStateTime(mCpu) / 100;
        mTotalStateTime.setText(sToString(totTime));

        // for all the 0 duration states, add the the Unused State area
        if (extraStates.size() > 0) {
            int n = 0;
            String str = "";

            for (String s : extraStates) {
                if (n++ > 0)
                    str += ", ";
                str += s;
            }

            mAdditionalStates.setVisibility(View.VISIBLE);
            mHeaderAdditionalStates.setVisibility(View.VISIBLE);
            mAdditionalStates.setText(str);
        } else {
            mAdditionalStates.setVisibility(View.GONE);
            mHeaderAdditionalStates.setVisibility(View.GONE);
        }

        // kernel line
        mKernelString.setText(mApp.getKernelVersion());
    }

    /**
     * Set up the state row
     */
    private void generateStateRow(CpuStateMonitor.CpuState state, ViewGroup parent) {
        // inflate the XML into a view in the parent
        LayoutInflater inf = LayoutInflater.from(mApp);
        LinearLayout theRow = (LinearLayout)inf.inflate(
                R.layout.state_row, parent, false);

        // what percetnage we've got
        CpuStateMonitor monitor = mApp.getCpuStateMonitor();
        int percent = (int) ((float)state.duration * 100 /
                monitor.getTotalStateTime(mCpu));

        // state name
        String sFreq;
        if (state.freq == 0) {
            sFreq = getString(R.string.deep_sleep);
        } else {
            sFreq = state.freq / 1000 + " MHz";
        }

        // duration
        String sDur = sToString(state.duration / 100);

        // map UI elements to objects
        TextView freqText = theRow.findViewById(R.id.ui_freq_text);
        TextView durText = theRow.findViewById(
                R.id.ui_duration_text);
        TextView perText = theRow.findViewById(
                R.id.ui_percentage_text);
        ProgressBar bar = theRow.findViewById(R.id.ui_bar);

        // modify the row
        freqText.setText(sFreq);
        perText.setText(percent + "%");
        durText.setText(sDur);
        bar.setProgress(percent);

        // add it to parent and return
        parent.addView(theRow);
    }

    /**
     * @return A nicely formatted String representing tSec seconds
     */
    private static String sToString(long tSec) {
        long h = (long)Math.floor(tSec / (60*60));
        long m = (long)Math.floor((tSec - h*60*60) / 60);
        long s = tSec % 60;
        String sDur;
        sDur = h + ":";
        if (m < 10)
            sDur += "0";
        sDur += m + ":";
        if (s < 10)
            sDur += "0";
        sDur += s;

        return sDur;
    }

}
