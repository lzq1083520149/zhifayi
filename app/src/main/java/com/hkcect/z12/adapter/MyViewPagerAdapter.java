package com.hkcect.z12.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;


public class MyViewPagerAdapter extends FragmentStatePagerAdapter {

    private String[] mTitles;
    private List<Fragment> mFragments;

    public MyViewPagerAdapter(FragmentManager fm, List<Fragment> mFragments) {
        super(fm);
//    this.mTitles = mTitles;
        this.mFragments = mFragments;
    }


    public MyViewPagerAdapter(FragmentManager fm, String[] mTitles, List<Fragment> mFragments) {
        super(fm);
        this.mTitles = mTitles;
        this.mFragments = mFragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles != null) {
            return mTitles[position];
        }
        return null;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}