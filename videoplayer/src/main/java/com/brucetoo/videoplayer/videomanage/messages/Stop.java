package com.brucetoo.videoplayer.videomanage.messages;

import android.media.MediaPlayer;

import com.brucetoo.videoplayer.videomanage.PlayerMessageState;
import com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerManagerCallback;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;


/**
 * This PlayerMessage calls {@link MediaPlayer#stop()} on the instance that is used inside {@link VideoPlayerView}
 */
public class Stop extends PlayerMessage {
    public Stop(VideoPlayerView videoView, VideoPlayerManagerCallback callback) {
        super(videoView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.stop();
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.STOPPING;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.STOPPED;
    }
}
