package com.brucetoo.videoplayer.videomanage.interfaces;

import com.brucetoo.videoplayer.IViewTracker;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 22:06
 */

public interface VideoPlayerListener {

    void onVideoSizeChangedMainThread(IViewTracker viewTracker,int width, int height);

    void onVideoPreparedMainThread(IViewTracker viewTracker);

    void onVideoCompletionMainThread(IViewTracker viewTracker);

    void onErrorMainThread(IViewTracker viewTracker,int what, int extra);

    void onBufferingUpdateMainThread(IViewTracker viewTracker,int percent);

    void onVideoStoppedMainThread(IViewTracker viewTracker);

    void onInfoMainThread(IViewTracker viewTracker,int what);
}
