package com.weshi.imusic.imusicapp.indicator;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.Button;

import com.viewpagerindicator.IconPagerAdapter;
import com.weshi.imusic.imusicapp.R;

class TestFragmentAdapter extends FragmentStatePagerAdapter {
    //protected static final String[] CONTENT = new String[] { "This", "Is", "A", "Test", };
    /*protected static final int[] ICONS = new int[] {
            R.drawable.perm_group_calendar,
            R.drawable.perm_group_camera,
            R.drawable.perm_group_device_alarms,
            R.drawable.perm_group_location
    };*/

    private Context parent;


    protected static final int[] CONTENT = new int[] {
            R.drawable.guide_01,
            R.drawable.guide_02,
            R.drawable.guide_03
    };
    private int mCount = CONTENT.length;

    public TestFragmentAdapter(FragmentManager fm,Context ctx) {
        super(fm);
        parent = ctx;
    }

    @Override
    public Fragment getItem(int position) {
        return TestFragment.newInstance(CONTENT[position % CONTENT.length],position+1 == CONTENT.length,parent);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return "";//TestFragmentAdapter.CONTENT[position % CONTENT.length];
    }
/*
    @Override
    public int getIconResId(int index) {
      return ICONS[index % ICONS.length];
    }*/

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}