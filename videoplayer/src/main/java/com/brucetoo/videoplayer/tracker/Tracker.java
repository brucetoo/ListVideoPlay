package com.brucetoo.videoplayer.tracker;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.util.ArrayMap;
import android.view.View;

import com.brucetoo.videoplayer.videomanage.interfaces.PlayerItemChangeListener;
import com.brucetoo.videoplayer.videomanage.interfaces.SingleVideoPlayerManager;
import com.brucetoo.videoplayer.videomanage.interfaces.VideoPlayerListener;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

/**
 * Created by Bruce Too
 * On 01/04/2017.
 * At 17:38
 * TODO Offer more api to control video and tracker view
 */

public class Tracker{

    private static ArrayMap<Activity, IViewTracker> mViewTrackers = new ArrayMap<>();

    /**
     * Attach a activity with single {@link IViewTracker},and bind
     * follower view into DecorView, so we can handle it
     * @param context activity
     * @return IViewTracker
     */
    public static IViewTracker attach(Activity context) {
        IViewTracker iViewTracker = mViewTrackers.get(context);
        if (iViewTracker != null) {
            return iViewTracker.attach();
        }
        IViewTracker tracker = new VideoTracker(context).attach();
        mViewTrackers.put(context, tracker);
        return tracker;
    }

    /**
     * Detach the follower view from DecorView,but we don't remove {@link IViewTracker}
     * from {@link #mViewTrackers} list, in case we need re-attach tracker view
     * @param context activity
     * @return IViewTracker
     */
    public static IViewTracker detach(Activity context) {
        IViewTracker iViewTracker = mViewTrackers.get(context);
        if (iViewTracker != null) {
            return iViewTracker.detach();
        }
        return null;
    }

    /**
     * Detach the follower view and release all instance in {@link IViewTracker}
     * and remove it, indicate we don't need {@link IViewTracker} again.
     * @param context activity
     * @return IViewTracker
     */
    public static IViewTracker destroy(Activity context){
        IViewTracker iViewTracker = mViewTrackers.remove(context);
        if (iViewTracker != null) {
            return iViewTracker.destroy();
        }
        return null;
    }

    /**
     * Check if the follower view still attach to DecorView 
     * @param context activity
     * @return IViewTracker
     */
    public static boolean isAttach(Activity context){
        IViewTracker iViewTracker = mViewTrackers.get(context);
        if (iViewTracker != null) {
            return iViewTracker.isAttach();
        }
        return false;
    }

    /**
     * Get current {@link IViewTracker} attach to Activity
     * NOTE: use this need check NPE
     * @param context current activity
     */
    public static IViewTracker getViewTracker(Activity context){
        return mViewTrackers.get(context);
    }

    /**
     * Check if current tracker view is the same as newTracker
     * @param context current activity
     * @param newTracker new tracker view
     */
    public static boolean isSameTrackerView(Activity context, View newTracker){
        IViewTracker iViewTracker = mViewTrackers.get(context);
        if(iViewTracker != null && iViewTracker.getTrackerView() != null){
            return iViewTracker.getTrackerView().equals(newTracker);
        }
        return false;
    }

    /**
     * Single change track view,re-bound {@link FloatLayerView} to it
     * @param context activity
     * @param trackView new track view
     */
    public static void changeTrackView(Activity context,View trackView){
        if(getViewTracker(context) != null){
            getViewTracker(context).changeTrackView(trackView);
        }
    }


    /**
     * If need auto rotation player view,we should call this in {@link Activity#onConfigurationChanged(Configuration)}
     * @param context activity
     * @param newConfig new Configuration
     */
    public static void onConfigurationChanged(Activity context,Configuration newConfig){
        if(getViewTracker(context) != null){
            getViewTracker(context).onConfigurationChanged(newConfig);
        }
    }

    /**
     * Add global item change listener
     * @param playerItemChangeListener play item changed
     */
    public static void addPlayerItemChangeListener(PlayerItemChangeListener playerItemChangeListener) {
        SingleVideoPlayerManager.getInstance().addPlayerItemChangeListener(playerItemChangeListener);
    }

    public static void removePlayerItemChangeListener(PlayerItemChangeListener playerItemChangeListener) {
        SingleVideoPlayerManager.getInstance().removePlayerItemChangeListener(playerItemChangeListener);
    }

    public static void removePlayerItemChangeListeners() {
        SingleVideoPlayerManager.getInstance().removePlayerItemChangeListeners();
    }

    /**
     * Add global {@link VideoPlayerListener} call this before {@link #playNewVideo(IViewTracker, VideoPlayerView, String)}
     * if {@link PlayerItemChangeListener} happened, it's useless to call this,instead can use new {@link VideoPlayerView}
     * to control video player.
     * @param videoPlayerListener VideoPlayerListener
     */
    public static void addVideoPlayerListener(VideoPlayerListener videoPlayerListener) {
        SingleVideoPlayerManager.getInstance().addVideoPlayerListener(videoPlayerListener);
    }

    public static void removeVideoPlayerListener(VideoPlayerListener videoPlayerListener) {
        SingleVideoPlayerManager.getInstance().removeVideoPlayerListener(videoPlayerListener);
    }

    public static void removeAllVideoPlayerListeners() {
        SingleVideoPlayerManager.getInstance().removeAllVideoPlayerListeners();
    }

    /**
     * Start a pause/stop video
     */
    public static void startVideo(){
        SingleVideoPlayerManager.getInstance().startVideo();
    }

    /**
     * Pause a prepared/stared video
     */
    public static void pauseVideo(){
        SingleVideoPlayerManager.getInstance().pauseVideo();
    }

    public static void playNewVideo(IViewTracker viewTracker, VideoPlayerView videoPlayerView){
        SingleVideoPlayerManager.getInstance().playNewVideo(viewTracker, videoPlayerView);
    }

    public static void stopAnyPlayback(){
        SingleVideoPlayerManager.getInstance().stopAnyPlayback();
    }

    public static void resetMediaPlayer(){
        SingleVideoPlayerManager.getInstance().resetMediaPlayer();
    }

}

