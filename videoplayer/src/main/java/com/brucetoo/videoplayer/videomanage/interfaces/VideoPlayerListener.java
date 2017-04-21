package com.brucetoo.videoplayer.videomanage.interfaces;

import com.brucetoo.videoplayer.tracker.IViewTracker;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 22:06
 */

public interface VideoPlayerListener {

    void onVideoSizeChanged(IViewTracker viewTracker, int width, int height);

    void onVideoPrepared(IViewTracker viewTracker);

    void onVideoCompletion(IViewTracker viewTracker);

    void onError(IViewTracker viewTracker, int what, int extra);

    void onBufferingUpdate(IViewTracker viewTracker, int percent);

    void onInfo(IViewTracker viewTracker, int what);

    void onVideoStarted(IViewTracker viewTracker);

    void onVideoPaused(IViewTracker viewTracker);

    void onVideoStopped(IViewTracker viewTracker);

    void onVideoReset(IViewTracker viewTracker);

    void onVideoReleased(IViewTracker viewTracker);
}
