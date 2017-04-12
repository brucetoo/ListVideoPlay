package com.brucetoo.videoplayer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;

import com.brucetoo.videoplayer.videomanage.interfaces.SingleVideoPlayerManager;
import com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerListener;
import com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerManager;
import com.brucetoo.videoplayer.videomanage.meta.MetaData;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 15:08
 */

public class VideoTracker extends ViewTracker implements VideoPlayerListener {

    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(null);
    public VideoTracker(Activity context) {
        super(context);
    }

    @Override
    public IViewTracker detach() {
        IViewTracker tracker = super.detach();
        mVideoPlayerManager.stopAnyPlayback();
        return tracker;
    }

    @Override
    public IViewTracker destroy() {
        IViewTracker tracker = super.destroy();
        mVideoPlayerManager.resetMediaPlayer();
        return tracker;
    }

    @Override
    public IViewTracker trackView(@NonNull View trackView) {
        IViewTracker tracker = super.trackView(trackView);
        Object tag = tracker.getTrackerView().getTag(R.id.tag_tracker_view);
        if(tag == null){
            throw new IllegalArgumentException("Tracker view need set tag by id:tag_tracker_view !");
        }

        mVideoPlayerManager.playNewVideo(null,getFloatLayerView().getVideoPlayerView(),(String)tag);
        mVideoPlayerManager.addVideoPlayerListener(this);
        return tracker;
    }

    @Override
    public void onVideoSizeChangedMainThread(int width, int height) {

    }

    @Override
    public void onVideoPreparedMainThread() {

    }

    @Override
    public void onVideoCompletionMainThread() {

    }

    @Override
    public void onErrorMainThread(int what, int extra) {

    }

    @Override
    public void onBufferingUpdateMainThread(int percent) {

    }

    @Override
    public void onVideoStoppedMainThread() {

    }

    @Override
    public void onInfoMainThread(int what) {

    }
}
