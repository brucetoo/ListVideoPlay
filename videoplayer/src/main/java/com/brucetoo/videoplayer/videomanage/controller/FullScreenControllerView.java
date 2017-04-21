package com.brucetoo.videoplayer.videomanage.controller;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import com.brucetoo.videoplayer.tracker.IViewTracker;
import com.brucetoo.videoplayer.R;
import com.brucetoo.videoplayer.tracker.Tracker;

/**
 * Created by Bruce Too
 * On 18/04/2017.
 * At 14:08
 */

public class FullScreenControllerView extends BaseControllerView {

    private VideoControllerView mControllerView;
    
    public FullScreenControllerView(Context context) {
        super(context);
    }

    public FullScreenControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullScreenControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {

    }

    @Override
    public void setViewTracker(IViewTracker viewTracker) {
        super.setViewTracker(viewTracker);
        viewTracker.muteVideo(false);
        // TODO: 18/04/2017 Separate VideoController View,don't need create VideoControllerView every time ,here just a demo
        if(mControllerView == null) {
            mControllerView = new VideoControllerView.Builder((Activity) viewTracker.getContext(), mPlayerControlListener)
                .withVideoTitle("TEST VIDEO")
                .withVideoView(viewTracker.getFollowerView())//to enable toggle display controller view
                .canControlBrightness(true)
                .canControlVolume(true)
                .canSeekVideo(false)
                .exitIcon(R.drawable.video_top_back)
                .pauseIcon(R.drawable.ic_media_pause)
                .playIcon(R.drawable.ic_media_play)
                .shrinkIcon(R.drawable.ic_media_fullscreen_shrink)
                .stretchIcon(R.drawable.ic_media_fullscreen_stretch)
                .build(this);//layout container that hold video play view
        }else {
            mControllerView.setMediaPlayerControlListener(mPlayerControlListener);
        }
    }

    @Override
    protected void attachWindow(boolean attach) {

    }

    private VideoControllerView.MediaPlayerControlListener mPlayerControlListener = new VideoControllerView.MediaPlayerControlListener() {
        @Override
        public void start() {
           Tracker.startVideo();
        }

        @Override
        public void pause() {
            Tracker.pauseVideo();
        }

        @Override
        public int getDuration() {
            return mVideoPlayerView.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return mVideoPlayerView.getCurrentPosition();
        }

        @Override
        public void seekTo(int position) {
            mVideoPlayerView.seekTo(position);
        }

        @Override
        public boolean isPlaying() {
            return mVideoPlayerView.isPlaying();
        }

        @Override
        public boolean isComplete() {
            return mVideoPlayerView.isComplete();
        }

        @Override
        public int getBufferPercentage() {
            return mVideoPlayerView.getCurrentBuffer();
        }

        @Override
        public boolean isFullScreen() {
            return mViewTracker.isFullScreen();
        }

        @Override
        public void toggleFullScreen() {
            if (isFullScreen()) {
                mViewTracker.toNormalScreen();
            } else {
                mViewTracker.toFullScreen();
            }
        }

        @Override
        public void exit() {
            if (isFullScreen()) {
                mViewTracker.toNormalScreen();
            }
        }
    };

}
