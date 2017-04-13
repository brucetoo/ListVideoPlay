package com.brucetoo.videoplayer.videomanage.interfaces;

import com.brucetoo.videoplayer.IViewTracker;

/**
 * Created by Bruce Too
 * On 13/04/2017.
 * At 18:17
 */

public class SimpleVideoPlayerListener implements VideoPlayerListener {

    @Override
    public void onVideoSizeChangedMainThread(IViewTracker viewTracker, int width, int height) {

    }

    @Override
    public void onVideoPreparedMainThread(IViewTracker viewTracker) {

    }

    @Override
    public void onVideoCompletionMainThread(IViewTracker viewTracker) {

    }

    @Override
    public void onErrorMainThread(IViewTracker viewTracker, int what, int extra) {

    }

    @Override
    public void onBufferingUpdateMainThread(IViewTracker viewTracker, int percent) {

    }

    @Override
    public void onVideoStoppedMainThread(IViewTracker viewTracker) {

    }

    @Override
    public void onInfoMainThread(IViewTracker viewTracker, int what) {

    }
}
