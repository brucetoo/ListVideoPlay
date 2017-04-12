package com.brucetoo.videoplayer.videomanage.messages;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerManagerCallback;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;


/**
 * This PlayerMessage calls {@link MediaPlayer#setDataSource(Context, Uri)} on the instance that is used inside {@link VideoPlayerView}
 */
public class SetUrlDataSourceMessage extends SetDataSourceMessage{

    private final String mVideoUrl;

    public SetUrlDataSourceMessage(VideoPlayerView videoPlayerView, String videoUrl, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
        mVideoUrl = videoUrl;
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.setDataSource(mVideoUrl);
    }
}
