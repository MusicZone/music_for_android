package com.weshi.imusic.imusicapp.indicator;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Button;

import com.viewpagerindicator.CirclePageIndicator;
import com.weshi.imusic.imusicapp.R;

public class SampleCirclesDefault extends BaseSampleActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_circles);

        mAdapter = new TestFragmentAdapter(getSupportFragmentManager(),this);



        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }
}