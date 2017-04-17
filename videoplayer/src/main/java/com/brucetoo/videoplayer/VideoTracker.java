package com.brucetoo.videoplayer;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.View;

import com.brucetoo.videoplayer.utils.DrawableTask;
import com.brucetoo.videoplayer.videomanage.controller.VideoControllerView;
import com.brucetoo.videoplayer.videomanage.interfaces.PlayerItemChangeListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SimpleVideoPlayerListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SingleVideoPlayerManager;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 15:08
 */

public class VideoTracker extends ViewTracker implements PlayerItemChangeListener, DrawableTask.Callback {

    private static final String TAG = VideoTracker.class.getSimpleName();
    private VideoPlayerView mVideoPlayView;
    private int mCurrentBuffer;
    private boolean mIsComplete;
    private DrawableTask mDrawableTask = new DrawableTask(this);
    private SimpleArrayMap<Object, BitmapDrawable> mCachedDrawables = new SimpleArrayMap<>();

    public VideoTracker(Activity context) {
        super(context);
    }

    @Override
    public IViewTracker detach() {
        IViewTracker tracker = super.detach();
        keepScreenOn(false);
        SingleVideoPlayerManager.getInstance().stopAnyPlayback();
        return tracker;
    }

    @Override
    public IViewTracker hide() {
        pauseVideo();
        return super.hide();
    }

    @Override
    public IViewTracker attach() {
        IViewTracker tracker = super.attach();
        keepScreenOn(true);
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
    public void muteVideo(boolean mute) {
        mVideoPlayView.muteVideo(mute);
    }

    @Override
    public void pauseVideo() {
        mVideoPlayView.pause();
    }

    @Override
    public void startVideo() {
        mVideoPlayView.start();
    }

    @Override
    public IViewTracker trackView(@NonNull View trackView) {
        IViewTracker tracker = super.trackView(trackView);
        mBoundObject = tracker.getTrackerView().getTag(R.id.tag_tracker_view);
        if (mBoundObject == null) {
            throw new IllegalArgumentException("Tracker view need set tag by id:tag_tracker_view !");
        }

        addTrackerImageToVideoBottomView(trackView);
        //TODO mBoundObject typedef
        SingleVideoPlayerManager.getInstance().playNewVideo(this, mVideoPlayView, (String) mBoundObject);
        SingleVideoPlayerManager.getInstance().addPlayerItemChangeListener(this);

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

    @Override
    public Object getBoundObject() {
        return mBoundObject;
    }

    @Override
    public void onPlayerItemChanged(IViewTracker viewTracker) {
        addOrRemoveLoadingView(true);
        mVideoPlayView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void done(Object key, BitmapDrawable drawable) {
        Log.i(TAG, "mDrawableTask done, addKey : " + key);
        mCachedDrawables.put(key, drawable);
        mVideoBottomView.setBackground(drawable);
    }

    private void addTrackerImageToVideoBottomView(View trackView) {
        boolean containsKey = mCachedDrawables.containsKey(mBoundObject);
        Log.i(TAG, "addTrackerImageToVideoBottomView, containsKey : " + containsKey);
        if (containsKey) {
            mVideoBottomView.setBackground(mCachedDrawables.get(mBoundObject));
        } else {
            mDrawableTask.execute(mBoundObject, trackView);
        }
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
            mVideoPlayView.start();
        }

        @Override
        public void pause() {
            mVideoPlayView.pause();
        }

        @Override
        public int getDuration() {
            return mVideoPlayView.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return mVideoPlayView.getCurrentPosition();
        }

        @Override
        public void seekTo(int position) {
            mVideoPlayView.seekTo(position);
        }

        @Override
        public boolean isPlaying() {
            return mVideoPlayView.isPlaying();
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
