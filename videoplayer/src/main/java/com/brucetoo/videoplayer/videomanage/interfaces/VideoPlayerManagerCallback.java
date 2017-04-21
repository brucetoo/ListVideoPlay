package com.brucetoo.videoplayer.videomanage.interfaces;


import com.brucetoo.videoplayer.tracker.IViewTracker;
import com.brucetoo.videoplayer.videomanage.PlayerMessageState;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

/**
 * This callback is used by {@link com.brucetoo.videoplayer.videomanage.messages.PlayerMessage}
 * to get and set data it needs
 */
public interface VideoPlayerManagerCallback {

    void setCurrentItem(IViewTracker viewTracker, VideoPlayerView newPlayerView);

    void updateVideoPlayerState(VideoPlayerView videoPlayerView, PlayerMessageState playerMessageState);

    PlayerMessageState getCurrentPlayerState();
}
