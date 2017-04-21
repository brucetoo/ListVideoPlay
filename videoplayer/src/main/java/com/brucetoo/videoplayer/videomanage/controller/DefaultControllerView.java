package com.brucetoo.videoplayer.videomanage.controller;

import android.view.View;

import com.brucetoo.videoplayer.tracker.IViewTracker;

/**
 * Created by Bruce Too
 * On 14/04/2017.
 * At 16:58
 */

public class DefaultControllerView implements IControllerView {

    @Override
    public View normalScreenController(IViewTracker tracker) {
//        tracker.muteVideo(true);
        return new NormalScreenControllerView(tracker.getContext());
    }

    @Override
    public View detailScreenController(IViewTracker tracker) {
        return fullScreenController(tracker);
    }

    @Override
    public View fullScreenController(IViewTracker tracker) {
//        tracker.muteVideo(false);
        return new FullScreenControllerView(tracker.getContext());
    }

    @Override
    public View loadingController(IViewTracker tracker) {
        return new LoadingControllerView(tracker.getContext());
    }

    @Override
    public View anotherController(IViewTracker tracker) {
        return null;
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
