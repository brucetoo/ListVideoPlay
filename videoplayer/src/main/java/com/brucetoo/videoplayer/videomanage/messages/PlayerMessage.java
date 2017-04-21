package com.brucetoo.videoplayer.videomanage.messages;


import com.brucetoo.videoplayer.utils.Logger;
import com.brucetoo.videoplayer.videomanage.PlayerMessageState;
import com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerManagerCallback;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

/**
 * This is generic interface for PlayerMessage
 */
public abstract class PlayerMessage implements Message {

    private static final String TAG = PlayerMessage.class.getSimpleName();
    private final VideoPlayerView mCurrentPlayer;
    private final VideoPlayerManagerCallback mCallback;

    public PlayerMessage(VideoPlayerView currentPlayer, VideoPlayerManagerCallback callback) {
        mCurrentPlayer = currentPlayer;
        mCallback = callback;
    }

    protected final PlayerMessageState getCurrentState() {
        return mCallback.getCurrentPlayerState();
    }

    @Override
    public final void polledFromQueue() {
        mCallback.updateVideoPlayerState(mCurrentPlayer, stateBefore());
    }

    @Override
    public final void messageFinished() {
        mCallback.updateVideoPlayerState(mCurrentPlayer, stateAfter());
    }

    public final void runMessage() {
        Logger.v(TAG, ">> runMessage, " + getClass().getSimpleName());
        performAction(mCurrentPlayer);
        Logger.v(TAG, "<< runMessage, " + getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    protected abstract void performAction(VideoPlayerView currentPlayer);

    protected abstract PlayerMessageState stateBefore();

    protected abstract PlayerMessageState stateAfter();

}
