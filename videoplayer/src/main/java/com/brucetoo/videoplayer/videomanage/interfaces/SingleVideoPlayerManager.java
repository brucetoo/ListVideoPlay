package com.brucetoo.videoplayer.videomanage.interfaces;

import com.brucetoo.videoplayer.Config;
import com.brucetoo.videoplayer.videomanage.MessagesHandlerThread;
import com.brucetoo.videoplayer.videomanage.PlayerMessageState;
import com.brucetoo.videoplayer.videomanage.SetNewViewForPlayback;
import com.brucetoo.videoplayer.videomanage.messages.ClearPlayerInstance;
import com.brucetoo.videoplayer.videomanage.messages.CreateNewPlayerInstance;
import com.brucetoo.videoplayer.videomanage.messages.Prepare;
import com.brucetoo.videoplayer.videomanage.messages.Release;
import com.brucetoo.videoplayer.videomanage.messages.Reset;
import com.brucetoo.videoplayer.videomanage.messages.SetUrlDataSourceMessage;
import com.brucetoo.videoplayer.videomanage.messages.Stop;
import com.brucetoo.videoplayer.videomanage.meta.MetaData;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;
import com.brucetoo.videoplayer.utils.Logger;

import java.util.Arrays;

/**
 * This implementation of {@link VideoPlayerManager} is designed to manage a single video playback.
 * If new video should start playback this implementation previously stops currently playing video
 * and then starts new playback.
 */
public class SingleVideoPlayerManager implements VideoPlayerManager<MetaData>, VideoPlayerManagerCallback, VideoPlayerListener {

    private static final String TAG = SingleVideoPlayerManager.class.getSimpleName();
    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;

    /**
     * This is a handler thread that is used to process Player messages.
     */
    private final MessagesHandlerThread mPlayerHandler = new MessagesHandlerThread();

    /**
     * When {@link SingleVideoPlayerManager} actually switches the player
     * (Switching the player can take a while: we have to stop previous player then start another),
     * then it calls {@link PlayerItemChangeListener#onPlayerItemChanged(MetaData)}}
     * To notify that player was switched.
     */
    private final PlayerItemChangeListener mPlayerItemChangeListener;

    private VideoPlayerView mCurrentPlayer = null;
    private PlayerMessageState mCurrentPlayerState = PlayerMessageState.IDLE;

    public SingleVideoPlayerManager(PlayerItemChangeListener playerItemChangeListener) {
        mPlayerItemChangeListener = playerItemChangeListener;
    }

    /**
     * Call it if you have direct url or path to video source
     *
     * The logic is following:
     * 1. Stop queue processing to have consistent state of queue when posting new messages
     * 2. Check if current player is active.
     * 3. If it is active and already playing current video we do nothing
     * 4. If not active then start new playback
     * 5. Resume stopped queue
     *
     * @param currentItemMetaData
     * @param videoPlayerView - the actual video player
     * @param videoUrl - the link to the video source
     */
    @Override
    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView videoPlayerView, String videoUrl) {
        if(SHOW_LOGS) Logger.v(TAG, ">> playNewVideo, videoPlayer " + videoPlayerView + ", mCurrentPlayer " + mCurrentPlayer + ", videoPlayerView " + videoPlayerView);

        mPlayerHandler.pauseQueueProcessing(TAG);

        startNewPlayback(currentItemMetaData, videoPlayerView, videoUrl);

        mPlayerHandler.resumeQueueProcessing(TAG);

        if(SHOW_LOGS) Logger.v(TAG, "<< playNewVideo, videoPlayer " + videoPlayerView + ", videoUrl " + videoUrl);
    }

    /**
     * This is copy paste of {@link #startNewPlayback(MetaData, VideoPlayerView, String)}
     * The difference is that this method uses AssetFileDescriptor instead of direct path
     */
    private void startNewPlayback(MetaData currentItemMetaData, VideoPlayerView videoPlayerView, String videoUrl) {

        this.mCurrentPlayer = videoPlayerView;

        // set listener for new player
        videoPlayerView.addMediaPlayerListener(this);
        if (SHOW_LOGS) Logger.v(TAG, "startNewPlayback, mCurrentPlayerState " + mCurrentPlayerState);

        mPlayerHandler.clearAllPendingMessages(TAG);

        stopResetReleaseClearCurrentPlayer();
        setNewViewForPlayback(currentItemMetaData, videoPlayerView);
        startPlayback(videoPlayerView, videoUrl);
    }

    /**
     * This method stops playback if one exists.
     */
    @Override
    public void stopAnyPlayback() {
        if(SHOW_LOGS) Logger.v(TAG, ">> stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);

        mCurrentPlayer.removeAllPlayerListener();

        mPlayerHandler.pauseQueueProcessing(TAG);
        if (SHOW_LOGS) Logger.v(TAG, "stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);

        mPlayerHandler.clearAllPendingMessages(TAG);

        stopResetReleaseClearCurrentPlayer();

        mPlayerHandler.resumeQueueProcessing(TAG);

        if(SHOW_LOGS) Logger.v(TAG, "<< stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);
    }

    /**
     * This method stops current playback and resets MediaPlayer.
     * Call it when you no longer need it.
     */
    @Override
    public void resetMediaPlayer() {
        if(SHOW_LOGS) Logger.v(TAG, ">> resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);

        mCurrentPlayer.removeAllPlayerListener();

        mPlayerHandler.pauseQueueProcessing(TAG);
        if (SHOW_LOGS) Logger.v(TAG, "resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);
        mPlayerHandler.clearAllPendingMessages(TAG);
        resetReleaseClearCurrentPlayer();

        mPlayerHandler.resumeQueueProcessing(TAG);

        if(SHOW_LOGS) Logger.v(TAG, "<< resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);
    }

    @Override
    public IMediaPlayer getMediaPlayer() {
        return mCurrentPlayer.getMediaPlayer();
    }

    @Override
    public void addVideoPlayerListener(VideoPlayerListener videoPlayerListener) {
        if(mCurrentPlayer != null) {
            mCurrentPlayer.addMediaPlayerListener(videoPlayerListener);
        }
    }

    /**
     * This method posts a set of messages to {@link MessagesHandlerThread} in order
     * to start new playback
     *
     * @param videoPlayerView - video player view which should start playing
     * @param videoUrl - a source path
     */
    private void startPlayback(VideoPlayerView videoPlayerView, String videoUrl) {
        if(SHOW_LOGS) Logger.v(TAG, "startPlayback");

        mPlayerHandler.addMessages(Arrays.asList(
                new CreateNewPlayerInstance(videoPlayerView, this),
                new SetUrlDataSourceMessage(videoPlayerView, videoUrl, this),
                new Prepare(videoPlayerView, this)
        ));
    }


    /**
     * This method posts a message that will eventually call {@link PlayerItemChangeListener#onPlayerItemChanged(MetaData)}
     * When current player is stopped and new player is about to be active this message sets new player
     */
    private void setNewViewForPlayback(MetaData currentItemMetaData, VideoPlayerView videoPlayerView) {
        if(SHOW_LOGS) Logger.v(TAG, "setNewViewForPlayback, currentItemMetaData " + currentItemMetaData + ", videoPlayer " + videoPlayerView);
        mPlayerHandler.addMessage(new SetNewViewForPlayback(currentItemMetaData, videoPlayerView, this));
    }

    /**
     * This method posts a set of messages to {@link MessagesHandlerThread}
     * in order to stop current playback
     */
    private void stopResetReleaseClearCurrentPlayer() {
        if(SHOW_LOGS) Logger.v(TAG, "stopResetReleaseClearCurrentPlayer, mCurrentPlayerState " + mCurrentPlayerState +", mCurrentPlayer " + mCurrentPlayer);

        switch (mCurrentPlayerState){
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
        if(SHOW_LOGS) Logger.v(TAG, "resetReleaseClearCurrentPlayer, mCurrentPlayerState " + mCurrentPlayerState +", mCurrentPlayer " + mCurrentPlayer);

        switch (mCurrentPlayerState){
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
     * Then it passes that knowledge to the {@link #mPlayerItemChangeListener}
     *
     */
    @Override
    public void setCurrentItem(MetaData currentItemMetaData, VideoPlayerView videoPlayerView) {
        if(SHOW_LOGS) Logger.v(TAG, ">> onPlayerItemChanged");

        mCurrentPlayer = videoPlayerView;
        if(mPlayerItemChangeListener != null)
          mPlayerItemChangeListener.onPlayerItemChanged(currentItemMetaData);

        if(SHOW_LOGS) Logger.v(TAG, "<< onPlayerItemChanged");
    }

    @Override
    public void setVideoPlayerState(VideoPlayerView videoPlayerView, PlayerMessageState playerMessageState) {
        if(SHOW_LOGS) Logger.v(TAG, ">> setVideoPlayerState, playerMessageState " + playerMessageState + ", videoPlayer " + videoPlayerView);

        mCurrentPlayerState = playerMessageState;

        if(SHOW_LOGS) Logger.v(TAG, "<< setVideoPlayerState, playerMessageState " + playerMessageState + ", videoPlayer " + videoPlayerView);
    }

    @Override
    public PlayerMessageState getCurrentPlayerState() {
        if(SHOW_LOGS) Logger.v(TAG, "getCurrentPlayerState, mCurrentPlayerState " + mCurrentPlayerState);
        return mCurrentPlayerState;
    }

    @Override
    public void onVideoSizeChangedMainThread(int width, int height) {
    }

    @Override
    public void onVideoPreparedMainThread() {
    }

    @Override
    public void onVideoCompletionMainThread() {
        mCurrentPlayerState = PlayerMessageState.PLAYBACK_COMPLETED;
    }

    @Override
    public void onErrorMainThread(int what, int extra) {
        if(SHOW_LOGS) Logger.v(TAG, "onErrorMainThread, what " + what + ", extra " + extra);

        /** if error happen during playback, we need to set error state.
         * Because we cannot run some messages in Error state
        for example {@link Stop}*/
        mCurrentPlayerState = PlayerMessageState.ERROR;
    }

    @Override
    public void onBufferingUpdateMainThread(int percent) {
    }

    @Override
    public void onVideoStoppedMainThread() {

    }

    @Override
    public void onInfoMainThread(int what) {

    }
}
