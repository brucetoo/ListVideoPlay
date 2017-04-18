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
    private BaseControllerView mNormalScreenView;
    private BaseControllerView mFullScreenView;

    @Override
    public View normalScreenController(IViewTracker tracker) {
        tracker.muteVideo(true);
        if(mNormalScreenView == null){
            mNormalScreenView = new NormalScreenControllerView(tracker.getContext());
            (mNormalScreenView).setViewTracker(tracker);
        }
        return mNormalScreenView;
    }

    @Override
    public View detailScreenController(IViewTracker tracker) {
        return fullScreenController(tracker);
    }

    @Override
    public View fullScreenController(IViewTracker tracker) {
        tracker.muteVideo(false);
        if(mFullScreenView == null){
            mFullScreenView = new FullScreenControllerView(tracker.getContext());
            mFullScreenView.setViewTracker(tracker);
        }
        return mFullScreenView;
    }

    @Override
    public View loadingController(IViewTracker tracker) {
        if (mLoadingView == null){
            mLoadingView = new LoadingControllerView(tracker.getContext());
        }
        return mLoadingView;
    }

    @Override
    public boolean muteVideo() {
        return true;
    }

    @Override
    public boolean enableAutoRotation() {
        return true;
    }
}
