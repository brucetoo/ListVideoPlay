package com.brucetoo.videoplayer.videomanage.interfaces;

import android.graphics.SurfaceTexture;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.brucetoo.videoplayer.tracker.IViewTracker;

import java.io.IOException;

/**
 * Created by Bruce Too
 * On 12/04/2017.
 * At 14:16
 */

public interface IMediaPlayer {

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

    //TODO handle IllegalStateException too.
    void setDataSource(@NonNull String url) throws IOException;

    void prepare() throws IOException;

    void prepareAsync() throws IOException;

    void start() throws IOException;

    void pause() throws IOException;

    void stop() throws IOException;

    void reset() throws IOException;

    void release() throws IOException;

    int getDuration() throws IOException;

    void seekTo(int mis) throws IOException;

    void setVolume(@FloatRange(from = 0, to = 1) float left, @FloatRange(from = 0, to = 1) float right) throws IOException;

    void setSurfaceTexture(SurfaceTexture surfaceTexture) throws IOException;

    int getVideoWidth();

    int getVideoHeight();

    int getCurrentPosition() throws IOException;

    boolean isPlaying();

    void clearAll();

    void setViewTracker(IViewTracker viewTracker);

    State getCurrentState();

}
