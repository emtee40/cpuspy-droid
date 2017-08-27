package com.tortel.cpuspy.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tortel.cpuspy.CpuSpyApp;
import com.tortel.cpuspy.CpuStateMonitor;

public class StateFragmentAdapter extends FragmentStatePagerAdapter {
    private CpuSpyApp mApp;
    private CpuStateMonitor mMonitor;
    private int mCpuCount = 0;

    StateFragmentAdapter(CpuSpyApp app, FragmentManager fm) {
        super(fm);
        mApp = app;
        mMonitor = mApp.getCpuStateMonitor();
        mCpuCount = mMonitor.getCpuCount();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mCpuCount = mMonitor.getCpuCount();
    }

    @Override
    public Fragment getItem(int position) {
        if (position < mCpuCount && position < mMonitor.getCpuCount()) {
            StateFragment frag = new StateFragment();
            Bundle args = new Bundle();
            args.putInt(StateFragment.CPU, position);
            frag.setArguments(args);
            return frag;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "CPU "+position;
    }

    @Override
    public int getCount() {
        if (mApp != null) {
            return mCpuCount;
        }
        return 0;
    }
}
