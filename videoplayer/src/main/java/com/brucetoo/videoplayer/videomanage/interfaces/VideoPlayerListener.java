package com.brucetoo.videoplayer.videomanage.interfaces;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 22:06
 */

public interface VideoPlayerListener {

    void onVideoSizeChangedMainThread(int width, int height);

    void onVideoPreparedMainThread();

    void onVideoCompletionMainThread();

    void onErrorMainThread(int what, int extra);

    void onBufferingUpdateMainThread(int percent);

    void onVideoStoppedMainThread();

    void onInfoMainThread(int what);
}
