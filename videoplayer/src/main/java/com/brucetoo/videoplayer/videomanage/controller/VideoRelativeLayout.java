package com.brucetoo.videoplayer.videomanage.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.brucetoo.videoplayer.tracker.IViewTracker;
import com.brucetoo.videoplayer.R;
import com.brucetoo.videoplayer.tracker.Tracker;
import com.brucetoo.videoplayer.videomanage.interfaces.PlayerItemChangeListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SimpleVideoPlayerListener;
import com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerListener;

/**
 * Created by Bruce Too
 * On 14/04/2017.
 * At 18:02
 * A RelativeLayout that can observe {@link com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerListener}
 */

public class VideoRelativeLayout extends RelativeLayout implements PlayerItemChangeListener {

    private View mPlayView;
    private View mTrackerView;

    public VideoRelativeLayout(Context context) {
        super(context);
    }

    public VideoRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPlayView = findViewById(R.id.view_play_video);
        mTrackerView = findViewById(R.id.view_tracker);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Tracker.addVideoPlayerListener(mVideoPlayerListener);
        Tracker.addPlayerItemChangeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Tracker.removeVideoPlayerListener(mVideoPlayerListener);
        Tracker.removePlayerItemChangeListener(this);
    }

    private VideoPlayerListener mVideoPlayerListener = new SimpleVideoPlayerListener(){

        @Override
        public void onVideoStarted(IViewTracker viewTracker) {
            if(mPlayView != null && viewTracker.getTrackerView().equals(mTrackerView)){
                mPlayView.setVisibility(GONE);
            }
        }

        @Override
        public void onVideoStopped(IViewTracker viewTracker) {
            if(mPlayView != null){
                mPlayView.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onVideoCompletion(IViewTracker viewTracker) {
            if(mPlayView != null){
                mPlayView.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onVideoPaused(IViewTracker viewTracker) {
            if(mPlayView != null){
                mPlayView.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onError(IViewTracker viewTracker, int what, int extra) {
            if(mPlayView != null){
                mPlayView.setVisibility(VISIBLE);
            }
        }
    };

    @Override
    public void onPlayerItemChanged(IViewTracker viewTracker) {
        if(mPlayView != null){
            if(viewTracker.getTrackerView().equals(mTrackerView)) {
                mPlayView.setVisibility(GONE);
            }else {
                mPlayView.setVisibility(VISIBLE);
            }
        }
    }
}
