package com.brucetoo.listvideoplay.videomanage.controller;

import android.view.View;
import android.widget.AbsListView;

/**
 * Created by Bruce Too
 * On 10/12/16.
 * At 18:06
 * Calculate the distance of ListView scroll
 * But sometimes has weird bug when {@link AbsListView.OnScrollListener#onScroll(AbsListView, int, int, int)} happened
 * We need record the last {@link ListScrollDistanceCalculator#mTotalScrollDistance} and check the current value is
 * valid or not
 * Example: (We need log the scroll value to find the discipline)
 *
 *  lastScrollValue * currentScrollValue < 0  indicate currentScrollValue is invalid,so give it up
 */

public class ListScrollDistanceCalculator implements AbsListView.OnScrollListener {

    private ScrollDistanceListener mScrollDistanceListener;

    private boolean mListScrollStarted;
    private int mFirstVisibleItem;
    private int mFirstVisibleHeight;
    private int mFirstVisibleTop, mFirstVisibleBottom;
    private int mTotalScrollDistance;
    private int mDeltaScrollDistance;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getCount() == 0) return;
        switch (scrollState) {
            case SCROLL_STATE_IDLE: {
                mListScrollStarted = false;
                break;
            }
            case SCROLL_STATE_TOUCH_SCROLL: {
                final View firstChild = view.getChildAt(0);
                mFirstVisibleItem = view.getFirstVisiblePosition();
                mFirstVisibleTop = firstChild.getTop();
                mFirstVisibleBottom = firstChild.getBottom();
                mFirstVisibleHeight = firstChild.getHeight();
                mListScrollStarted = true;
                mTotalScrollDistance = 0;
                break;
            }
        }
    }

    public int getTotalScrollDistance() {
        return mTotalScrollDistance;
    }

    public int getDeltaScrollDistance(){
        return mDeltaScrollDistance;
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount == 0 || !mListScrollStarted) return;

        final View firstChild = view.getChildAt(0);
        final int firstVisibleTop = firstChild.getTop(), firstVisibleBottom = firstChild.getBottom();
        final int firstVisibleHeight = firstChild.getHeight();
        final int delta;
        if (firstVisibleItem > mFirstVisibleItem) {
            mFirstVisibleTop += mFirstVisibleHeight;
            delta = firstVisibleTop - mFirstVisibleTop;
        } else if (firstVisibleItem < mFirstVisibleItem) {
            mFirstVisibleBottom -= mFirstVisibleHeight;
            delta = firstVisibleBottom - mFirstVisibleBottom;
        } else {
            delta = firstVisibleBottom - mFirstVisibleBottom;
        }
        mDeltaScrollDistance = delta;
        mTotalScrollDistance += delta;
        if (mScrollDistanceListener != null) {
            mScrollDistanceListener.onScrollDistanceChanged(delta, mTotalScrollDistance);
        }
        mFirstVisibleTop = firstVisibleTop;
        mFirstVisibleBottom = firstVisibleBottom;
        mFirstVisibleHeight = firstVisibleHeight;
        mFirstVisibleItem = firstVisibleItem;
    }

    public void setScrollDistanceListener(ScrollDistanceListener listener) {
        mScrollDistanceListener = listener;
    }

    public static interface ScrollDistanceListener {
        void onScrollDistanceChanged(int delta, int total);
    }
}