package com.brucetoo.videoplayer.videomanage.interfaces;

import com.brucetoo.videoplayer.tracker.IViewTracker;

/**
 * Created by Bruce Too
 * On 13/04/2017.
 * At 18:17
 */

public class SimpleVideoPlayerListener implements VideoPlayerListener {

    @Override
    public void onVideoSizeChanged(IViewTracker viewTracker, int width, int height) {

    }

    @Override
    public void onVideoPrepared(IViewTracker viewTracker) {

    }

    @Override
    public void onVideoCompletion(IViewTracker viewTracker) {

    }

    @Override
    public void onError(IViewTracker viewTracker, int what, int extra) {

    }

    @Override
    public void onBufferingUpdate(IViewTracker viewTracker, int percent) {

    }

    @Override
    public void onVideoStopped(IViewTracker viewTracker) {

    }

    @Override
    public void onVideoReset(IViewTracker viewTracker) {

    }

    @Override
    public void onVideoReleased(IViewTracker viewTracker) {

    }

    @Override
    public void onInfo(IViewTracker viewTracker, int what) {

    }

    @Override
    public void onVideoStarted(IViewTracker viewTracker) {

    }

    @Override
    public void onVideoPaused(IViewTracker viewTracker) {

    }
}
