package com.brucetoo.videoplayer.scrolldetector;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.brucetoo.videoplayer.tracker.IViewTracker;

/**
 * Created by Bruce Too
 * On 06/04/2017.
 * At 09:55
 */

public class RecyclerScrollDetector extends RecyclerView.OnScrollListener implements IScrollDetector {

    private RecyclerView mRecyclerView;
    private int mScrollState = IScrollDetector.SCROLL_STATE_IDLE;
    private IViewTracker mViewTracker;

    public RecyclerScrollDetector(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        recyclerView.addOnScrollListener(this);
    }

    @Override
    public View getView() {
        return mRecyclerView;
    }


    @Override
    public void setTracker(IViewTracker tracker) {
        mViewTracker = tracker;
    }

    @Override
    public void onScrollStateChanged(int scrollState) {

    }

    @Override
    public void detach() {

    }

    @Override
    public boolean isIdle() {
        return false;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                mScrollState = IScrollDetector.SCROLL_STATE_TOUCH_SCROLL;
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                mScrollState = IScrollDetector.SCROLL_STATE_FLING;
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
                mScrollState = IScrollDetector.SCROLL_STATE_IDLE;
                break;
        }

        onScrollStateChanged(mScrollState);
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    }
}
