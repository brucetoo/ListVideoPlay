package com.brucetoo.videoplayer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.View;

import com.brucetoo.videoplayer.utils.DrawableTask;
import com.brucetoo.videoplayer.utils.OrientationDetector;
import com.brucetoo.videoplayer.utils.Utils;
import com.brucetoo.videoplayer.videomanage.controller.BaseControllerView;
import com.brucetoo.videoplayer.videomanage.controller.IControllerView;
import com.brucetoo.videoplayer.videomanage.interfaces.PlayerItemChangeListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SimpleVideoPlayerListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SingleVideoPlayerManager;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 15:08
 */

public class VideoTracker extends ViewTracker implements PlayerItemChangeListener, DrawableTask.Callback, OrientationDetector.OnOrientationChangedListener {

    private static final String TAG = VideoTracker.class.getSimpleName();
    private VideoPlayerView mVideoPlayView;
    private DrawableTask mDrawableTask = new DrawableTask(this);
    private SimpleArrayMap<Object, BitmapDrawable> mCachedDrawables = new SimpleArrayMap<>();
    private OrientationDetector mOrientationDetector;
    private View mLoadingControllerView;
    private BaseControllerView mNormalScreenControllerView;
    private BaseControllerView mFullScreenControllerView;

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
    public IViewTracker show() {
        startVideo();
        return super.show();
    }

    @Override
    public IViewTracker attach() {
        IViewTracker tracker = super.attach();
        keepScreenOn(true);
        mVideoPlayView = mFloatLayerView.getVideoPlayerView();
        mVideoPlayView.refreshSurfaceTexture(0, 0);
        mVideoPlayView.setAlpha(0f);
//        View view = new View(getContext());
//        view.setBackgroundColor(Color.parseColor("#dd000000"));
//        //TODO Add immerse view when enable mask
//        tracker.getFloatLayerView().addView(view,0);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isFullScreen()) {
            addFullScreenView();
        } else {
            addNormalScreenView();
        }
    }

    @Override
    public IViewTracker controller(IControllerView controllerView) {
        super.controller(controllerView);
        if (mControllerView.enableAutoRotation()) {// auto rotation
            if (mOrientationDetector == null) {
                mOrientationDetector = new OrientationDetector(mContext, this);
                mOrientationDetector.enable(true);
            }
        } else {
            if (mOrientationDetector != null) {
                mOrientationDetector.enable(false);
            }
            mOrientationDetector = null;
        }
        mLoadingControllerView = controllerView.loadingController(this);
        mNormalScreenControllerView = (BaseControllerView) controllerView.normalScreenController(this);
        mFullScreenControllerView = (BaseControllerView) controllerView.fullScreenController(this);
        return this;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (Utils.isSystemRotationEnabled(mContext) && mIsAttach) {
            switch (orientation) {
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    toNormalScreen();
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    toFullScreen();
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    break;
            }
        }
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
            public void onInfo(IViewTracker viewTracker, int what) {
                //This callback may not be called
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    mVideoPlayView.setVisibility(View.VISIBLE);
                    //clear back ground
                    mVideoBottomView.setBackground(null);
                    //hide loading view
                    addOrRemoveLoadingView(false);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    //show loading view
                    addOrRemoveLoadingView(true);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    //hide loading view
                    addOrRemoveLoadingView(false);
                }
            }

            @Override
            public void onBufferingUpdate(IViewTracker viewTracker, int percent) {
                if(percent == 100){
                    mVideoPlayView.setVisibility(View.VISIBLE);
                    addOrRemoveLoadingView(false);
                    mVideoBottomView.setBackground(null);
                }
            }

            @Override
            public void onVideoPrepared(IViewTracker viewTracker) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    mVideoPlayView.setVisibility(View.VISIBLE);
                    addOrRemoveLoadingView(false);
                    mVideoBottomView.setBackground(null);
                }
                addNormalScreenView();
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

    /**
     * Prevent the abrupt black screen in {@link #mFloatLayerView},and set tracker view's
     * background into {@link #mFloatLayerView} to make a better visual connection
     *
     * @param trackView the view be tracked
     */
    private void addTrackerImageToVideoBottomView(View trackView) {
        boolean containsKey = mCachedDrawables.containsKey(mBoundObject);
        Log.i(TAG, "addTrackerImageToVideoBottomView, containsKey : " + containsKey);
        if (containsKey) {
            mVideoBottomView.setBackground(mCachedDrawables.get(mBoundObject));
        } else {
            mDrawableTask.execute(mBoundObject, trackView);
        }
    }

    public void addNormalScreenView() {
        mVideoTopView.removeView(mFullScreenControllerView);
        if(mNormalScreenControllerView.getParent() == null) {
            mVideoTopView.addView(mNormalScreenControllerView);
        }
        mNormalScreenControllerView.setViewTracker(this);
    }

    public void addFullScreenView() {
        mVideoTopView.removeView(mNormalScreenControllerView);
        if(mFullScreenControllerView.getParent() == null) {
            mVideoTopView.addView(mFullScreenControllerView);
        }
        mFullScreenControllerView.setViewTracker(this);
    }

    private void addOrRemoveLoadingView(boolean add) {
        if (mControllerView != null) {
            if (add) {
                if (mLoadingControllerView.getParent() == null) {
                    mVideoTopView.addView(mLoadingControllerView);
                }
            } else {
                mVideoTopView.removeView(mLoadingControllerView);
            }
        }
    }
}
