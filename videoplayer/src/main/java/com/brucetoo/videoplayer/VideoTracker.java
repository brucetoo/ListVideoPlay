package com.brucetoo.videoplayer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;

import com.brucetoo.videoplayer.videomanage.controller.VideoControllerView;
import com.brucetoo.videoplayer.videomanage.interfaces.IMediaPlayer;
import com.brucetoo.videoplayer.videomanage.interfaces.SimpleVideoPlayerListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SingleVideoPlayerManager;

import java.io.IOException;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 15:08
 */

public class VideoTracker extends ViewTracker {

    private IMediaPlayer mMediaPlayer;
    private int mCurrentBuffer;

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
    public IViewTracker attach() {
        IViewTracker tracker = super.attach();
        getVideoTopLayer();
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
        if (tag == null) {
            throw new IllegalArgumentException("Tracker view need set tag by id:tag_tracker_view !");
        }

        SingleVideoPlayerManager.getInstance().playNewVideo(this, getFloatLayerView().getVideoPlayerView(), (String) tag);
        SingleVideoPlayerManager.getInstance().addVideoPlayerListener(new SimpleVideoPlayerListener() {
            @Override
            public void onBufferingUpdateMainThread(IViewTracker viewTracker, int percent) {
                mCurrentBuffer = percent;
            }
        });

        return tracker;
    }

    public void setMediaPlayer(IMediaPlayer mediaPlayer) {
        this.mMediaPlayer = mediaPlayer;
    }

    private View getVideoTopLayer() {
        return new VideoControllerView.Builder(mContext, mPlayerControlListener)
            .withVideoTitle("TEST VIDEO")
            .withVideoView(mFollowerView)//to enable toggle display controller view
            .canControlBrightness(true)
            .canControlVolume(true)
            .canSeekVideo(false)
            .exitIcon(R.drawable.video_top_back)
            .pauseIcon(R.drawable.ic_media_pause)
            .playIcon(R.drawable.ic_media_play)
            .shrinkIcon(R.drawable.ic_media_fullscreen_shrink)
            .stretchIcon(R.drawable.ic_media_fullscreen_stretch)
            .build(mVideoTopView);//layout container that hold video play view
    }

    private VideoControllerView.MediaPlayerControlListener mPlayerControlListener = new VideoControllerView.MediaPlayerControlListener() {
        @Override
        public void start() {
            try {
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void pause() {
            try {
                mMediaPlayer.pause();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getDuration() {
            try {
                return mMediaPlayer.getDuration();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public int getCurrentPosition() {
            try {
                return mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public void seekTo(int position) {
            try {
                mMediaPlayer.seekTo(position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }

        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        public int getBufferPercentage() {
            return mCurrentBuffer;
        }

        @Override
        public boolean isFullScreen() {
            return VideoTracker.this.isFullScreen();
        }

        @Override
        public void toggleFullScreen() {
            if (isFullScreen()) {
                toNormalScreen();
            } else {
                toFullScreen();
            }
        }

        @Override
        public void exit() {
            if (isFullScreen()) {
                toNormalScreen();
            }
        }
    };

}
