package com.brucetoo.listvideoplay.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brucetoo.listvideoplay.Backable;
import com.brucetoo.listvideoplay.R;
import com.brucetoo.videoplayer.tracker.Tracker;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 14:37
 */

public class PagerSupportFragment extends Fragment implements Backable{

    ViewPager mViewPager;
    private int mCurrentIndex;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_pager_support,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return new ListSupportFragment();
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        mCurrentIndex = mViewPager.getCurrentItem();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                 if(mCurrentIndex != position){
                     Tracker.destroy(getActivity());
                 }
                mCurrentIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onBackPressed() {
        boolean attach = Tracker.isAttach(getActivity());
        if(attach){
            Tracker.destroy(getActivity());
            return true;
        }else {
            return false;
        }
    }
}
