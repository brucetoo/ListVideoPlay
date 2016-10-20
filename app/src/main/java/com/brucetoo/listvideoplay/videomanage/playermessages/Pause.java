package com.brucetoo.listvideoplay.videomanage.playermessages;

import android.media.MediaPlayer;

import com.brucetoo.listvideoplay.videomanage.PlayerMessageState;
import com.brucetoo.listvideoplay.videomanage.manager.VideoPlayerManagerCallback;
import com.brucetoo.listvideoplay.videomanage.ui.VideoPlayerView;


/**
 * This PlayerMessage calls {@link MediaPlayer#pause()} ()} on the instance that is used inside {@link VideoPlayerView}
 */
public class Pause extends PlayerMessage {
    public Pause(VideoPlayerView videoView, VideoPlayerManagerCallback callback) {
        super(videoView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.pause();
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.PAUSING;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.PAUSED;
    }
}
