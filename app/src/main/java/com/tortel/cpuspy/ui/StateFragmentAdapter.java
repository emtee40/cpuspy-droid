package com.tortel.cpuspy.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tortel.cpuspy.CpuSpyApp;

public class StateFragmentAdapter extends FragmentStatePagerAdapter {
    private CpuSpyApp mApp;

    StateFragmentAdapter(CpuSpyApp app, FragmentManager fm) {
        super(fm);
        mApp = app;
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
