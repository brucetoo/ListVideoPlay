package com.brucetoo.videoplayer.videomanage.controller;

import android.view.View;

import com.brucetoo.videoplayer.IViewTracker;

/**
 * Created by Bruce Too
 * On 14/04/2017.
 * At 16:58
 */

public class DefaultControllerView implements IControllerView {

    private View mLoadingView;

    @Override
    public View normalScreenController(IViewTracker tracker) {
        return null;
    }

    @Override
    public View fullScreenController(IViewTracker tracker) {
        return null;
    }

    @Override
    public View loadingController(IViewTracker tracker) {
        if (mLoadingView == null){
            mLoadingView = new LoadingControllerView(tracker.getContext());
        }
        return mLoadingView;
    }
}
