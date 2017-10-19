package com.smd.remotecamera.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;


public class RemoteFileListPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mData;

    public RemoteFileListPagerAdapter(FragmentManager fm, List<Fragment> data) {
        super(fm);
        mData = data;
    }

    @Override
    public Fragment getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }
}
