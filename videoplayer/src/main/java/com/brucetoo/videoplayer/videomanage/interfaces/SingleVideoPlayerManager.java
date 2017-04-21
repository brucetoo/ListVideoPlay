package com.brucetoo.videoplayer.videomanage.interfaces;

import android.os.Handler;
import android.os.Looper;

import com.brucetoo.videoplayer.tracker.IViewTracker;
import com.brucetoo.videoplayer.utils.Logger;
import com.brucetoo.videoplayer.videomanage.MessagesHandlerThread;
import com.brucetoo.videoplayer.videomanage.PlayerMessageState;
import com.brucetoo.videoplayer.videomanage.SetNewViewForPlayback;
import com.brucetoo.videoplayer.videomanage.messages.ClearPlayerInstance;
import com.brucetoo.videoplayer.videomanage.messages.CreateNewPlayerInstance;
import com.brucetoo.videoplayer.videomanage.messages.Pause;
import com.brucetoo.videoplayer.videomanage.messages.Prepare;
import com.brucetoo.videoplayer.videomanage.messages.Release;
import com.brucetoo.videoplayer.videomanage.messages.Reset;
import com.brucetoo.videoplayer.videomanage.messages.SetUrlDataSourceMessage;
import com.brucetoo.videoplayer.videomanage.messages.Start;
import com.brucetoo.videoplayer.videomanage.messages.Stop;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This implementation of {@link VideoPlayerManager} is designed to manage a single video playback.
 * If new video should start playback this implementation previously stops currently playing video
 * and then starts new playback.
 */
public class SingleVideoPlayerManager implements VideoPlayerManager<IViewTracker>, VideoPlayerManagerCallback, VideoPlayerListener {

    private static final String TAG = SingleVideoPlayerManager.class.getSimpleName();

    /**
     * This is a handler thread that is used to process Player messages.
     */
    private final MessagesHandlerThread mPlayerHandler = new MessagesHandlerThread();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private VideoPlayerView mCurrentPlayer = null;
    private PlayerMessageState mCurrentPlayerState = PlayerMessageState.IDLE;

    private List<PlayerItemChangeListener> mPlayerItemChangeListeners = new ArrayList<>();
    private List<VideoPlayerListener> mPendingAddListeners = new ArrayList<>();

    private static SingleVideoPlayerManager mInstance;

    private SingleVideoPlayerManager() {
    }

    public static SingleVideoPlayerManager getInstance() {
        if (mInstance == null) {
            synchronized (SingleVideoPlayerManager.class) {
                mInstance = new SingleVideoPlayerManager();
                return mInstance;
            }
        }
        return mInstance;
    }

    /**
     * Start play a new video in a new {@link VideoPlayerView}
     * 1. Stop queue processing to have consistent state of queue when posting new messages
     * 2. Remove all listener and message in queue(destroy the current player)
     * 3. Create a new {@link IMediaPlayer} for new {@link VideoPlayerView},add start prepare to play
     * 5. Resume stopped queue
     *
     * @param viewTracker     current item bounded IViewTracker
     * @param videoPlayerView - the actual video player
     */
    @Override
    public void playNewVideo(IViewTracker viewTracker, VideoPlayerView videoPlayerView) {
        Logger.v(TAG, ">> playNewVideo, videoPlayer " + videoPlayerView + ", mCurrentPlayer " + mCurrentPlayer + ", videoPlayerView " + videoPlayerView);

        mPlayerHandler.pauseQueueProcessing(TAG);

        startNewPlayback(viewTracker, videoPlayerView);

        mPlayerHandler.resumeQueueProcessing(TAG);

        Logger.v(TAG, "<< playNewVideo, videoPlayer " + videoPlayerView);
    }

    private void startNewPlayback(IViewTracker viewTracker, VideoPlayerView videoPlayerView) {

        // set listener for new player
        videoPlayerView.addMediaPlayerListener(this);

        //clear all pre pending message
        mPlayerHandler.clearAllPendingMessages(TAG);

        //clear current player
        stopResetReleaseClearCurrentPlayer();

        //start play in new player
        setNewViewForPlaybackAndPlay(viewTracker, videoPlayerView);
    }

    /**
     * This method stops playback if one exists.
     */
    @Override
    public void stopAnyPlayback() {
        Logger.v(TAG, ">> stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);
        mPlayerHandler.pauseQueueProcessing(TAG);
        Logger.v(TAG, "stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);

        mPlayerHandler.clearAllPendingMessages(TAG);

        stopResetReleaseClearCurrentPlayer();

        mPlayerHandler.resumeQueueProcessing(TAG);

        Logger.v(TAG, "<< stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);
    }

    /**
     * This method stops current playback and resets MediaPlayer.
     * Call it when you no longer need it.
     */
    @Override
    public void resetMediaPlayer() {
        Logger.v(TAG, ">> resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);

        mPlayerHandler.pauseQueueProcessing(TAG);
        Logger.v(TAG, "resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);
        mPlayerHandler.clearAllPendingMessages(TAG);
        resetReleaseClearCurrentPlayer();

        mPlayerHandler.resumeQueueProcessing(TAG);

        Logger.v(TAG, "<< resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);
    }

    @Override
    public void startVideo() {
        Logger.v(TAG, ">> startVideo, mCurrentPlayerState " + mCurrentPlayerState);
        mPlayerHandler.pauseQueueProcessing(TAG);
        Logger.v(TAG, "startVideo, mCurrentPlayerState " + mCurrentPlayerState);

        switch (mCurrentPlayerState) {
            case STARTING:
            case STARTED:
            case PAUSING:
            case PAUSED:
            case STOPPING:
            case STOPPED:
                mPlayerHandler.addMessage(new Start(mCurrentPlayer, this));
                break;
        }

        mPlayerHandler.resumeQueueProcessing(TAG);

        Logger.v(TAG, "<< startVideo, mCurrentPlayerState " + mCurrentPlayerState);
    }

    @Override
    public void pauseVideo() {
        Logger.v(TAG, ">> pauseVideo, mCurrentPlayerState " + mCurrentPlayerState);

        mPlayerHandler.pauseQueueProcessing(TAG);
        Logger.v(TAG, "pauseVideo, mCurrentPlayerState " + mCurrentPlayerState);

        switch (mCurrentPlayerState) {
            case STARTING:
            case STARTED:
            case PAUSING:
            case PAUSED:
                mPlayerHandler.addMessage(new Pause(mCurrentPlayer, this));
                break;
        }

        mPlayerHandler.resumeQueueProcessing(TAG);

        Logger.v(TAG, "<< pauseVideo, mCurrentPlayerState " + mCurrentPlayerState);
    }

    /**
     * This method posts a message that will eventually call {@link PlayerItemChangeListener#onPlayerItemChanged(IViewTracker)}
     * When current player is stopped and new player is about to be active this message sets new player
     */
    private void setNewViewForPlaybackAndPlay(IViewTracker viewTracker, VideoPlayerView videoPlayerView) {
        Logger.v(TAG, "setNewViewForPlaybackAndPlay, viewTracker " + viewTracker + ", videoPlayer " + videoPlayerView);
        mPlayerHandler.addMessages(Arrays.asList(
            //trigger {@link PlayerItemChangeListener#onPlayerItemChanged(MetaData)}
            new SetNewViewForPlayback(viewTracker, videoPlayerView, this),
            //create new one and start prepare video play
            new CreateNewPlayerInstance(videoPlayerView, this),
            new SetUrlDataSourceMessage(videoPlayerView, viewTracker.getMetaData(), this),
            new Prepare(videoPlayerView, this)
        ));
    }

    /**
     * This method posts a set of messages to {@link MessagesHandlerThread}
     * in order to stop current playback
     */
    private void stopResetReleaseClearCurrentPlayer() {
        Logger.v(TAG, "stopResetReleaseClearCurrentPlayer, mCurrentPlayerState " + mCurrentPlayerState + ", mCurrentPlayer " + mCurrentPlayer);

        switch (mCurrentPlayerState) {
            case SETTING_NEW_PLAYER:
            case IDLE:

            case CREATING_PLAYER_INSTANCE:
            case PLAYER_INSTANCE_CREATED:

            case CLEARING_PLAYER_INSTANCE:
            case PLAYER_INSTANCE_CLEARED:
                // in these states player is stopped
                break;
            case INITIALIZED:
            case PREPARING:
            case PREPARED:
            case STARTING:
            case STARTED:
            case PAUSING:
            case PAUSED:
                mPlayerHandler.addMessage(new Stop(mCurrentPlayer, this));
                //FALL-THROUGH

            case SETTING_DATA_SOURCE:
            case DATA_SOURCE_SET:
                /** if we don't reset player in this state, will will get 0;0 from {@link android.media.MediaPlayer.OnVideoSizeChangedListener}.
                 *  And this TextureView will never recover */
            case STOPPING:
            case STOPPED:
            case ERROR: // reset if error
            case PLAYBACK_COMPLETED:
                mPlayerHandler.addMessage(new Reset(mCurrentPlayer, this));
                //FALL-THROUGH
            case RESETTING:
            case RESET:
                mPlayerHandler.addMessage(new Release(mCurrentPlayer, this));
                //FALL-THROUGH
            case RELEASING:
            case RELEASED:
                mPlayerHandler.addMessage(new ClearPlayerInstance(mCurrentPlayer, this));

                break;
            case END:
                throw new RuntimeException("unhandled " + mCurrentPlayerState);
        }
    }

    private void resetReleaseClearCurrentPlayer() {
        Logger.v(TAG, "resetReleaseClearCurrentPlayer, mCurrentPlayerState " + mCurrentPlayerState + ", mCurrentPlayer " + mCurrentPlayer);

        switch (mCurrentPlayerState) {
            case SETTING_NEW_PLAYER:
            case IDLE:

            case CREATING_PLAYER_INSTANCE:
            case PLAYER_INSTANCE_CREATED:

            case SETTING_DATA_SOURCE:
            case DATA_SOURCE_SET:

            case CLEARING_PLAYER_INSTANCE:
            case PLAYER_INSTANCE_CLEARED:
                break;
            case INITIALIZED:
            case PREPARING:
            case PREPARED:
            case STARTING:
            case STARTED:
            case PAUSING:
            case PAUSED:
            case STOPPING:
            case STOPPED:
            case ERROR: // reset if error
            case PLAYBACK_COMPLETED:
                mPlayerHandler.addMessage(new Reset(mCurrentPlayer, this));
                //FALL-THROUGH
            case RESETTING:
            case RESET:
                mPlayerHandler.addMessage(new Release(mCurrentPlayer, this));
                //FALL-THROUGH
            case RELEASING:
            case RELEASED:
                mPlayerHandler.addMessage(new ClearPlayerInstance(mCurrentPlayer, this));

                break;
            case END:
                throw new RuntimeException("unhandled " + mCurrentPlayerState);
        }
    }

    /**
     * This method is called by {@link SetNewViewForPlayback} message when new player becomes active.
     * Then it passes that knowledge to the {@link #mPlayerItemChangeListeners}
     */
    @Override
    public void setCurrentItem(final IViewTracker viewTracker, VideoPlayerView videoPlayerView) {
        Logger.v(TAG, ">> onPlayerItemChanged");

        mCurrentPlayer = videoPlayerView;
        mCurrentPlayer.setViewTracker(viewTracker);

        for (VideoPlayerListener listener : mPendingAddListeners) {
            mCurrentPlayer.addMediaPlayerListener(listener);
        }

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (PlayerItemChangeListener listener : mPlayerItemChangeListeners) {
                    listener.onPlayerItemChanged(viewTracker);
                }
            }
        });

        Logger.v(TAG, "<< onPlayerItemChanged");
    }

    @Override
    public void updateVideoPlayerState(VideoPlayerView videoPlayerView, PlayerMessageState playerMessageState) {
        Logger.v(TAG, ">> updateVideoPlayerState, playerMessageState " + playerMessageState + ", videoPlayer " + videoPlayerView);

        mCurrentPlayerState = playerMessageState;

        //clear listener when player instance cleared
        if (playerMessageState == PlayerMessageState.PLAYER_INSTANCE_CLEARED) {
            mCurrentPlayer.removeAllPlayerListener();
        }

        Logger.v(TAG, "<< updateVideoPlayerState, playerMessageState " + playerMessageState + ", videoPlayer " + videoPlayerView);
    }

    @Override
    public PlayerMessageState getCurrentPlayerState() {
        Logger.v(TAG, "getCurrentPlayerState, mCurrentPlayerState " + mCurrentPlayerState);
        return mCurrentPlayerState;
    }

    @Override
    public void onVideoSizeChanged(IViewTracker viewTracker, int width, int height) {
    }

    @Override
    public void onVideoPrepared(IViewTracker viewTracker) {
        Logger.v(TAG, "onVideoPrepared tracker" + viewTracker);
    }

    @Override
    public void onVideoCompletion(IViewTracker viewTracker) {
        mCurrentPlayerState = PlayerMessageState.PLAYBACK_COMPLETED;
    }

    @Override
    public void onError(IViewTracker viewTracker, int what, int extra) {
        Logger.v(TAG, "onError, what " + what + ", extra " + extra);

        /** if error happen during playback, we need to set error state.
         * Because we cannot run some messages in Error state
         for example {@link Stop}*/
        mCurrentPlayerState = PlayerMessageState.ERROR;
    }

    @Override
    public void onBufferingUpdate(IViewTracker viewTracker, int percent) {
    }

    @Override
    public void onVideoStopped(IViewTracker viewTracker) {
        Logger.v(TAG, "onVideoStopped tracker " + viewTracker);
    }

    @Override
    public void onVideoReset(IViewTracker viewTracker) {
        Logger.v(TAG, "onVideoReset tracker " + viewTracker);
    }

    @Override
    public void onVideoReleased(IViewTracker viewTracker) {
        Logger.v(TAG, "onVideoReleased tracker " + viewTracker);
    }

    @Override
    public void onInfo(IViewTracker viewTracker, int what) {
        Logger.v(TAG, "onInfo tracker " + viewTracker);
    }

    @Override
    public void onVideoStarted(IViewTracker viewTracker) {
        Logger.v(TAG, "onVideoStarted tracker " + viewTracker);
    }

    @Override
    public void onVideoPaused(IViewTracker viewTracker) {
        Logger.v(TAG, "onVideoPaused tracker " + viewTracker);
    }

    public void addPlayerItemChangeListener(PlayerItemChangeListener playerItemChangeListener) {
        mPlayerItemChangeListeners.add(playerItemChangeListener);
    }

    public void removePlayerItemChangeListener(PlayerItemChangeListener playerItemChangeListener) {
        mPlayerItemChangeListeners.remove(playerItemChangeListener);
    }

    public void removePlayerItemChangeListeners() {
        mPlayerItemChangeListeners.clear();
    }

    public void addVideoPlayerListener(VideoPlayerListener videoPlayerListener) {
        mPendingAddListeners.add(videoPlayerListener);
    }

    public void removeVideoPlayerListener(VideoPlayerListener videoPlayerListener) {
        mPendingAddListeners.remove(videoPlayerListener);
    }

    public void removeAllVideoPlayerListeners() {
        mPendingAddListeners.clear();
    }
}
