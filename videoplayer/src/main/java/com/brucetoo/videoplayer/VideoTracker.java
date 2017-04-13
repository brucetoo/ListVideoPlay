package com.brucetoo.videoplayer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;

import com.brucetoo.videoplayer.videomanage.interfaces.SingleVideoPlayerManager;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 15:08
 */

public class VideoTracker extends ViewTracker {

    public VideoTracker(Activity context) {
        super(context);
    }

    @Override
    public IViewTracker detach() {
        IViewTracker tracker = super.detach();
        SingleVideoPlayerManager.getInstance().stopAnyPlayback();
        return tracker;
    }

    @Override
    public IViewTracker destroy() {
        IViewTracker tracker = super.destroy();
        SingleVideoPlayerManager.getInstance().resetMediaPlayer();
        return tracker;
    }

    @Override
    public IViewTracker trackView(@NonNull View trackView) {
        IViewTracker tracker = super.trackView(trackView);
        Object tag = tracker.getTrackerView().getTag(R.id.tag_tracker_view);
        if(tag == null){
            throw new IllegalArgumentException("Tracker view need set tag by id:tag_tracker_view !");
        }

        SingleVideoPlayerManager.getInstance().playNewVideo(this,getFloatLayerView().getVideoPlayerView(),(String)tag);

        return tracker;
    }
}
