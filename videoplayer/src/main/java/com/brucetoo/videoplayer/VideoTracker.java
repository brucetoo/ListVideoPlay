package com.brucetoo.videoplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.View;

import com.brucetoo.videoplayer.videomanage.controller.VideoControllerView;
import com.brucetoo.videoplayer.videomanage.interfaces.IMediaPlayer;
import com.brucetoo.videoplayer.videomanage.interfaces.PlayerItemChangeListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SimpleVideoPlayerListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SingleVideoPlayerManager;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

import java.io.IOException;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 15:08
 */

public class VideoTracker extends ViewTracker {

    private VideoPlayerView mVideoPlayView;
    private IMediaPlayer mMediaPlayer;
    private int mCurrentBuffer;
    private boolean mIsComplete;

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
        mVideoPlayView = mFloatLayerView.getVideoPlayerView();
//        View view = new View(getContext());
//        view.setBackgroundColor(Color.parseColor("#dd000000"));
//        //TODO Add immerse view here
//        tracker.getFloatLayerView().addView(view,0);
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

        SingleVideoPlayerManager.getInstance().playNewVideo(this, mVideoPlayView, (String) tag);
        SingleVideoPlayerManager.getInstance().addPlayerItemChangeListener(new PlayerItemChangeListener() {
            @Override
            public void onPlayerItemChanged(IViewTracker viewTracker) {
                addOrRemoveLoadingView(true);
                mVideoPlayView.setVisibility(View.INVISIBLE);
            }
        });

        SingleVideoPlayerManager.getInstance().addVideoPlayerListener(new SimpleVideoPlayerListener() {
            @Override
            public void onBufferingUpdate(IViewTracker viewTracker, int percent) {
                mCurrentBuffer = percent;
            }

            @Override
            public void onVideoCompletion(IViewTracker viewTracker) {
                mIsComplete = true;
            }

            @Override
            public void onVideoPrepared(IViewTracker viewTracker) {
                mVideoPlayView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onInfo(IViewTracker viewTracker, int what) {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    //hide loading view
                    mVideoPlayView.setVisibility(View.VISIBLE);
                    addOrRemoveLoadingView(false);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    //show loading view
                    addOrRemoveLoadingView(true);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    //hide loading view
                    addOrRemoveLoadingView(false);
                }
            }
        });

        return tracker;
    }

    private void addOrRemoveLoadingView(boolean add) {
        if (mControllerView != null) {
            View loading = mControllerView.loadingController(this);
            if (add) {
                if (loading.getParent() == null) {
                    mVideoTopView.addView(loading);
                }
            } else {
                mVideoTopView.removeView(loading);
            }
        }
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
            return mIsComplete;
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
