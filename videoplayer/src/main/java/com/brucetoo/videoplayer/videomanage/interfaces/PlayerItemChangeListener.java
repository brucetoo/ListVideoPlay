package com.brucetoo.videoplayer.videomanage.interfaces;


import com.brucetoo.videoplayer.IViewTracker;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

public interface PlayerItemChangeListener {
    /**
     * Observer video playing item or tracker view changed
     * Note: This callback is post from sub thread to main thread {@link VideoPlayerManagerCallback#setCurrentItem(IViewTracker, VideoPlayerView)},
     * if we do something need be associated with {@link IViewTracker} in {@link com.brucetoo.videoplayer.VideoTracker},
     * we'd better use new {@link IViewTracker} in params instead, Specially indicate {@link IViewTracker#getTrackerView()}
     * @param viewTracker new bound {@link IViewTracker}
     */
    void onPlayerItemChanged(IViewTracker viewTracker);
}
