package com.brucetoo.listvideoplay.videomanage.ui;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import com.brucetoo.listvideoplay.videomanage.Config;
import com.brucetoo.listvideoplay.videomanage.utils.Logger;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class encapsulates {@link MediaPlayer}
 * and follows this use-case diagram:
 * <p>
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 */
public abstract class MediaPlayerWrapper
        implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener {

    private String TAG;
    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    public static final String VIDEO_TAG = "VIDEO_TAG";

    public static final int POSITION_UPDATE_NOTIFYING_PERIOD = 1000;         // milliseconds
    private Surface mSurface;

    public enum State {
        IDLE,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        END,
        ERROR
    }

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final MediaPlayer mMediaPlayer;
    private final AtomicReference<State> mState = new AtomicReference<>();

    private MainThreadMediaPlayerListener mListener;
    private Context mContext;

    protected MediaPlayerWrapper(MediaPlayer mediaPlayer, Context context) {
        TAG = "" + this;
        this.mContext = context;
        if (SHOW_LOGS) Logger.v(TAG, "constructor of MediaPlayerWrapper");
        if (SHOW_LOGS)
            Logger.v(TAG, "constructor of MediaPlayerWrapper, main Looper " + Looper.getMainLooper());
        if (SHOW_LOGS)
            Logger.v(TAG, "constructor of MediaPlayerWrapper, my Looper " + Looper.myLooper());

        if (Looper.myLooper() != null) {
            throw new RuntimeException("myLooper not null, a bug in some MediaPlayer implementation cause that listeners are not called at all. Please use a thread without Looper");
        }
        mMediaPlayer = mediaPlayer;

        mState.set(State.IDLE);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setScreenOnWhilePlaying(true);
    }

    private final Runnable mOnVideoPreparedMessage = new Runnable() {
        @Override
        public void run() {
            if (SHOW_LOGS) Logger.v(TAG, ">> run, onVideoPreparedMainThread");
            mListener.onVideoPreparedMainThread();
            if (SHOW_LOGS) Logger.v(TAG, "<< run, onVideoPreparedMainThread");
        }
    };

    public void prepare() {
        if (SHOW_LOGS) Logger.v(TAG, ">> execute prepare, mState " + mState);

        synchronized (mState) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    //must use prepareAsync() or may cause ANR
                    mMediaPlayer.prepareAsync();
                }
            });
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        if (SHOW_LOGS) Logger.v(TAG, ">> onPrepared");
        mState.set(State.PREPARED);

        if (mListener != null) {
            mMainThreadHandler.post(mOnVideoPreparedMessage);
        }

        //when prepare complete start playing video
        start();
    }

    /**
     * @see MediaPlayer#setDataSource(Context, Uri)
     */
    public void setDataSource(String filePath) throws IOException {
        synchronized (mState) {
            if (SHOW_LOGS)
                Logger.v(TAG, "setDataSource, filePath " + filePath + ", mState " + mState);

            //setOnBufferingUpdateListener only be called in internet streams
            mMediaPlayer.setDataSource(mContext, Uri.parse(filePath));
            mState.set(State.INITIALIZED);
        }
    }

    /**
     * @see MediaPlayer#setDataSource(FileDescriptor fd, long offset, long length)
     */
    public void setDataSource(AssetFileDescriptor assetFileDescriptor) throws IOException {
        synchronized (mState) {
            mMediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
            mState.set(State.INITIALIZED);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (SHOW_LOGS) Logger.v(TAG, "onVideoSizeChanged, width " + width + ", height " + height);
        if (mListener != null) {
            mListener.onVideoSizeChangedMainThread(width, height);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (SHOW_LOGS) Logger.v(TAG, "onVideoCompletion, mState " + mState);

        synchronized (mState) {
            mState.set(State.PLAYBACK_COMPLETED);
        }

        if (mListener != null) {
            mListener.onVideoCompletionMainThread();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (SHOW_LOGS) Logger.v(TAG, "onErrorMainThread, what " + what + ", extra " + extra);

        synchronized (mState) {
            mState.set(State.ERROR);
        }

        //weird error code what = -38 ? What the hell?
        if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN || what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            if (mListener != null) {
                mListener.onErrorMainThread(what, extra);
            }
        }
        // We always return true, because after Error player stays in this state.
        // See here http://developer.android.com/reference/android/media/MediaPlayer.html
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (SHOW_LOGS) Logger.v(VIDEO_TAG, "onBufferingUpdate percent : " + percent);
        if (mListener != null) {
            mListener.onBufferingUpdateMainThread(percent);
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (SHOW_LOGS) Logger.v(TAG, "onInfoMainThread");
        printInfo(what);
        if (mListener != null) {
            mListener.onInfoMainThread(what);
        }
        return false;
    }

    private void printInfo(int what) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_UNKNOWN");
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_VIDEO_TRACK_LAGGING");
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                if (SHOW_LOGS)
                    Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_VIDEO_RENDERING_START");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_BUFFERING_START");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_BUFFERING_END");
                break;
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_BAD_INTERLEAVING");
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_NOT_SEEKABLE");
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_METADATA_UPDATE");
                break;
            case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_UNSUPPORTED_SUBTITLE");
                break;
            case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                if (SHOW_LOGS) Logger.inf(TAG, "onInfoMainThread, MEDIA_INFO_SUBTITLE_TIMED_OUT");
                break;
        }
    }

    /**
     * Listener trigger 'onVideoPreparedMainThread' and `onVideoCompletionMainThread` events
     */
    public void setMainThreadMediaPlayerListener(MainThreadMediaPlayerListener listener) {
        mListener = listener;
    }

    /**
     * Play or resume video. Video will be played as soon as view is available and media player is
     * prepared.
     * <p/>
     * If video is stopped or ended and play() method was called, video will start over.
     */
    public void start() {
        if (SHOW_LOGS) Logger.v(TAG, ">> start");

        synchronized (mState) {
            if (SHOW_LOGS) Logger.v(TAG, "start, mState " + mState);
            mMediaPlayer.start();
            mState.set(State.STARTED);
        }
        if (SHOW_LOGS) Logger.v(TAG, "<< start");
    }

    /**
     * Pause video. If video is already paused, stopped or ended nothing will happen.
     */
    public void pause() {
        if (SHOW_LOGS) Logger.v(TAG, ">> pause");

        synchronized (mState) {
            if (SHOW_LOGS)
                Logger.v(TAG, "pause, mState " + mState);
            mMediaPlayer.pause();
            mState.set(State.PAUSED);
        }
        if (SHOW_LOGS) Logger.v(TAG, "<< pause");
    }

    private final Runnable mOnVideoStopMessage = new Runnable() {
        @Override
        public void run() {
            if (SHOW_LOGS) Logger.v(TAG, ">> run, onVideoStoppedMainThread");
            mListener.onVideoStoppedMainThread();
            if (SHOW_LOGS) Logger.v(TAG, "<< run, onVideoStoppedMainThread");
        }
    };

    public void stop() {
        if (SHOW_LOGS) Logger.v(TAG, ">> stop");

        synchronized (mState) {
            if (SHOW_LOGS) Logger.v(TAG, "stop, mState " + mState);

            mMediaPlayer.stop();
            mState.set(State.STOPPED);

            if (mListener != null) {
                mMainThreadHandler.post(mOnVideoStopMessage);
            }
        }
        if (SHOW_LOGS) Logger.v(TAG, "<< stop");
    }

    public void reset() {
        if (SHOW_LOGS) Logger.v(TAG, ">> reset , mState " + mState);

        synchronized (mState) {
            mMediaPlayer.reset();
            mState.set(State.IDLE);
        }
        if (SHOW_LOGS) Logger.v(TAG, "<< reset , mState " + mState);
    }

    public void release() {
        if (SHOW_LOGS) Logger.v(TAG, ">> release, mState " + mState);
        synchronized (mState) {
            mMediaPlayer.release();
            mState.set(State.END);
        }
        if (SHOW_LOGS) Logger.v(TAG, "<< release, mState " + mState);
    }

    public void clearAll() {
        if (SHOW_LOGS) Logger.v(TAG, ">> clearAll, mState " + mState);
        synchronized (mState) {
            mMediaPlayer.setOnVideoSizeChangedListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnBufferingUpdateListener(null);
            mMediaPlayer.setOnInfoListener(null);
            mMediaPlayer.setOnPreparedListener(null);
        }
        if (SHOW_LOGS) Logger.v(TAG, "<< clearAll, mState " + mState);
    }

    public void setLooping(boolean looping) {
        if (SHOW_LOGS) Logger.v(TAG, "setLooping " + looping);
        mMediaPlayer.setLooping(looping);
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        if (SHOW_LOGS) Logger.v(TAG, ">> setSurfaceTexture " + surfaceTexture);
        if (SHOW_LOGS) Logger.v(TAG, "setSurfaceTexture mSurface " + mSurface);


        if (surfaceTexture != null) {
            mSurface = new Surface(surfaceTexture);
            mMediaPlayer.setSurface(mSurface); // TODO fix illegal state exception
        } else {
            mMediaPlayer.setSurface(null);
        }
        if (SHOW_LOGS) Logger.v(TAG, "<< setSurfaceTexture " + surfaceTexture);

    }

    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public int getVideoWidth() {
        return mMediaPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return mMediaPlayer.getVideoHeight();
    }

    public int getCurrentPosition() {
        int currentPos;
        try {
            currentPos = mMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            return 0;
        }
        return currentPos;
    }

    public boolean isPlaying() {
        boolean isPlaying;
        try {
            isPlaying = mMediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
        return isPlaying;
    }

    public boolean isReadyForPlayback() {
        boolean isReadyForPlayback = false;
        synchronized (mState) {
            if (SHOW_LOGS) Logger.v(TAG, "isReadyForPlayback, mState " + mState);
            State state = mState.get();

            switch (state) {
                case IDLE:
                case INITIALIZED:
                case ERROR:
                case PREPARING:
                case STOPPED:
                case END:
                    isReadyForPlayback = false;
                    break;
                case PREPARED:
                case STARTED:
                case PAUSED:
                case PLAYBACK_COMPLETED:
                    isReadyForPlayback = true;
                    break;
            }

        }
        return isReadyForPlayback;
    }

    public int getDuration() {
        int duration = 0;
        synchronized (mState) {
            duration = mMediaPlayer.getDuration();
        }
        return duration;
    }

    public void seekToPosition(int position) {
        synchronized (mState) {
            State state = mState.get();
            if (SHOW_LOGS)
                Logger.v(TAG, "seekToPosition, position " + position + ", mState " + state);
            mMediaPlayer.seekTo(position);
        }
    }

    public State getCurrentState() {
        synchronized (mState) {
            return mState.get();
        }
    }

    public static int positionToPercent(int progressMillis, int durationMillis) {
        float percentPrecise = (float) progressMillis / (float) durationMillis * 100f;
        return Math.round(percentPrecise);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode();
    }

    public interface MainThreadMediaPlayerListener {
        void onVideoSizeChangedMainThread(int width, int height);

        void onVideoPreparedMainThread();

        void onVideoCompletionMainThread();

        void onErrorMainThread(int what, int extra);

        void onBufferingUpdateMainThread(int percent);

        void onVideoStoppedMainThread();

        void onInfoMainThread(int what);
    }

    private boolean inUiThread() {
        return Thread.currentThread().getId() == 1;
    }
}
