package com.brucetoo.videoplayer.videomanage.player;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.TextureView;

import com.brucetoo.videoplayer.tracker.IViewTracker;
import com.brucetoo.videoplayer.utils.Logger;
import com.brucetoo.videoplayer.videomanage.interfaces.IMediaPlayer;
import com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is player implementation based on {@link TextureView}
 * It encapsulates {@link MediaPlayer}.
 * <p>
 * It ensures that MediaPlayer methods are called from not main thread.
 * MediaPlayer methods are directly connected with hardware. That's why they should not be called from UI thread
 */
public class VideoPlayerView extends ScalableTextureView
    implements TextureView.SurfaceTextureListener,
    VideoPlayerListener {

    private String TAG = VideoPlayerView.class.getSimpleName();

    private IMediaPlayer mMediaPlayer;

    private IViewTracker mViewTracker;

    private TextureView.SurfaceTextureListener mLocalSurfaceTextureListener;

    private String mVideoPath;

    private int mCurrentBuffer;

    private boolean mIsComplete;

    private final List<VideoPlayerListener> mVideoPlayerListeners = new ArrayList<>();

    public VideoPlayerView(Context context) {
        super(context);
        initView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void checkThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("cannot be in main thread");
        }
    }

    public void reset() {
        checkThread();
        if (mMediaPlayer != null)
            try {
                mMediaPlayer.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void release() {
        checkThread();
        if (mMediaPlayer != null)
            try {
                mMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void clearPlayerInstance() {
        Logger.v(TAG, ">> clearPlayerInstance mMediaPlayer " + mMediaPlayer);

        checkThread();

        if (mMediaPlayer != null) {
            mMediaPlayer.clearAll();
        }
        mMediaPlayer = null;
        Logger.v(TAG, "<< clearPlayerInstance ");
    }

    public void createNewPlayerInstance() {
        Logger.v(TAG, ">> createNewPlayerInstance");

        checkThread();

        Logger.v(TAG, "createNewPlayerInstance mMediaPlayer " + mMediaPlayer);
        if (mMediaPlayer == null) {
            mMediaPlayer = new DefaultMediaPlayer(getContext(), this);
            mMediaPlayer.setViewTracker(mViewTracker);
        }

        SurfaceTexture texture = getSurfaceTexture();
        Logger.v(TAG, "texture " + texture);
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.setSurfaceTexture(texture);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.v(TAG, "<< createNewPlayerInstance");
    }

    public void prepare() {
        checkThread();
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.prepare();
            }
        } catch (Exception e) {
            //TODO handle different error here
            onError(mViewTracker, 0, 0);
            e.printStackTrace();
        }
    }

    public void stop() {
        checkThread();
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void start() {
        checkThread();
        Logger.v(TAG, ">> start");
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Logger.v(TAG, "<< start");
    }

    public void pause() {
        checkThread();
        Logger.d(TAG, ">> pause ");
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.d(TAG, "<< pause");
    }


    public void muteVideo(boolean mute) {
        try {
            if (mMediaPlayer != null) {
                if (mute) {
                    mMediaPlayer.setVolume(0, 0);
                } else {
                    mMediaPlayer.setVolume(1, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //when video is preparing,will throw IllegalStateException
        }
    }

    public int getDuration() {
        try {
            if (mMediaPlayer != null) {
                return mMediaPlayer.getDuration();
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //when video is preparing,will throw IllegalStateException
        }
        return 0;
    }

    public int getCurrentPosition() {
        try {
            if (mMediaPlayer != null) {
                return mMediaPlayer.getCurrentPosition();
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //when video is preparing,will throw IllegalStateException
        }
        return 0;
    }

    public void seekTo(int mis) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(mis);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //when video is preparing,will throw IllegalStateException
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    private void initView() {
        if (!isInEditMode()) {
            setScaleType(ScaleType.FILL);
            super.setSurfaceTextureListener(this);
        }
    }

    @Override
    public final void setSurfaceTextureListener(TextureView.SurfaceTextureListener listener) {
        mLocalSurfaceTextureListener = listener;
    }

    public void setDataSource(String path) {
        checkThread();
        Logger.v(TAG, "setDataSource, path " + path + ", this " + this);

        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.setDataSource(path);
            }
        } catch (Exception e) {
            Logger.d(TAG, e.getMessage());
            throw new RuntimeException(e);
        }
        mVideoPath = path;
    }

    @Override
    public void onVideoSizeChanged(IViewTracker viewTracker, int width, int height) {

        Logger.v(TAG, ">> onVideoSizeChanged, width " + width + ", height " + height);

        if (width != 0 && height != 0) {
            refreshSurfaceTexture(width, height);
        }

        //dispatch VideoPlayerListener
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onVideoSizeChanged(viewTracker, width, height);
        }

        Logger.v(TAG, "<< onVideoSizeChanged, width " + width + ", height " + height);
    }

    @Override
    public void onVideoCompletion(IViewTracker viewTracker) {

        Logger.v(TAG, "notifyVideoCompletion");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onVideoCompletion(viewTracker);
        }

        mIsComplete = true;
    }

    @Override
    public void onVideoPrepared(IViewTracker viewTracker) {

        Logger.v(TAG, "notifyOnVideoPrepared");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onVideoPrepared(viewTracker);
        }

        mIsComplete = false;
        muteVideo(viewTracker.getControllerView().muteVideo());
        /*
        Prevent VideoPlayerView flash the screen when render next video
        and setAlpha(0) when detach to screen first.
         */
        setAlpha(1f);
    }

    @Override
    public void onError(IViewTracker viewTracker, final int what, final int extra) {
        Logger.v(TAG, "onError, this " + VideoPlayerView.this);
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                printErrorExtra(extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                printErrorExtra(extra);
                break;
        }
        Logger.v(TAG, "notifyOnError");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onError(viewTracker, what, extra);
        }
    }

    @Override
    public void onBufferingUpdate(IViewTracker viewTracker, int percent) {

        Logger.v(TAG, "notifyBufferingUpdate");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onBufferingUpdate(viewTracker, percent);
        }

        mCurrentBuffer = percent;
    }

    @Override
    public void onVideoStopped(IViewTracker viewTracker) {
        Logger.v(TAG, "notifyOnVideoStopped");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onVideoStopped(viewTracker);
        }
    }

    @Override
    public void onVideoReset(IViewTracker viewTracker) {
        Logger.v(TAG, "notifyOnVideoRest");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onVideoReset(viewTracker);
        }
    }

    @Override
    public void onVideoReleased(IViewTracker viewTracker) {
        Logger.v(TAG, "notifyOnVideoReleased");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onVideoReleased(viewTracker);
        }
    }

    @Override
    public void onInfo(IViewTracker viewTracker, int what) {
        Logger.v(TAG, "notifyOnInfo");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onInfo(viewTracker, what);
        }
    }

    @Override
    public void onVideoStarted(IViewTracker viewTracker) {
        Logger.v(TAG, "onVideoStarted");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onVideoStarted(viewTracker);
        }
    }

    @Override
    public void onVideoPaused(IViewTracker viewTracker) {
        Logger.v(TAG, "onVideoPaused");
        List<VideoPlayerListener> listCopy;
        synchronized (mVideoPlayerListeners) {
            listCopy = new ArrayList<>(mVideoPlayerListeners);
        }
        for (VideoPlayerListener listener : listCopy) {
            listener.onVideoPaused(viewTracker);
        }
    }

    private void printErrorExtra(int extra) {
        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                Logger.v(TAG, "error extra MEDIA_ERROR_IO");
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                Logger.v(TAG, "error extra MEDIA_ERROR_MALFORMED");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Logger.v(TAG, "error extra MEDIA_ERROR_UNSUPPORTED");
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Logger.v(TAG, "error extra MEDIA_ERROR_TIMED_OUT");
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Logger.v(TAG, "onSurfaceTextureAvailable, width " + width + ", height " + height + ", this " + this);
        if (mLocalSurfaceTextureListener != null) {
            mLocalSurfaceTextureListener.onSurfaceTextureAvailable(surfaceTexture, width, height);
        }

        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.setSurfaceTexture(getSurfaceTexture());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mLocalSurfaceTextureListener != null) {
            mLocalSurfaceTextureListener.onSurfaceTextureSizeChanged(surface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Logger.v(TAG, "onSurfaceTextureDestroyed, surface " + surface);

        if (mLocalSurfaceTextureListener != null) {
            mLocalSurfaceTextureListener.onSurfaceTextureDestroyed(surface);
        }

        // We have to release this surface manually for better control.
        // Also we do this because we return false from this method
        surface.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (mLocalSurfaceTextureListener != null) {
            mLocalSurfaceTextureListener.onSurfaceTextureUpdated(surface);
        }
    }

    public void setViewTracker(IViewTracker viewTracker) {
        this.mViewTracker = viewTracker;
    }

    public void addMediaPlayerListener(VideoPlayerListener listener) {
        synchronized (mVideoPlayerListeners) {
            mVideoPlayerListeners.add(listener);
        }
    }

    public void removeMediaPlayerListener(VideoPlayerListener listener) {
        synchronized (mVideoPlayerListeners) {
            mVideoPlayerListeners.remove(this);
        }
    }

    public void removeAllPlayerListener() {
        synchronized (mVideoPlayerListeners) {
            mVideoPlayerListeners.clear();
        }
    }

    public boolean isComplete() {
        return mIsComplete;
    }

    public int getCurrentBuffer() {
        return mCurrentBuffer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode();
    }
}
