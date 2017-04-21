package com.brucetoo.videoplayer.tracker;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.FrameLayout;

import com.brucetoo.videoplayer.scrolldetector.IScrollDetector;
import com.brucetoo.videoplayer.videomanage.controller.IControllerView;
import com.brucetoo.videoplayer.videomanage.meta.MetaData;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 15:01
 * Helper class that bind a view rect area to another one,
 * And track origin location changed
 */

public interface IViewTracker{

    /**
     * Scroll edge of follower view
     */
    public static final int NONE_EDGE = 0;
    public static final int TOP_EDGE = 1;
    public static final int BOTTOM_EDGE = 2;
    public static final int LEFT_EDGE = 3;
    public static final int RIGHT_EDGE = 4;

    /**
     * Attach a follower view({@link #getFollowerView()} )
     * to DecorView {@link android.view.Window#ID_ANDROID_CONTENT}
     */
    IViewTracker attach();

    /**
     * Detach a follower view({@link #getFollowerView()} )
     * to DecorView {@link android.view.Window#ID_ANDROID_CONTENT}
     */
    IViewTracker detach();

    /**
     * Just hide current {@link #getFloatLayerView()},not delete it,and pause video
     * if exits
     */
    IViewTracker hide();

    /**
     * Just show current {@link #getFloatLayerView()},and start play video
     * if exits
     */
    IViewTracker show();

    /**
     * {@link #detach()} and release unnecessary resources
     * Normally call in Activity or Fragment destroyed.
     */
    IViewTracker destroy();

    /**
     * Offer a tracker view for follower view to track
     * Need Detach the old one and attach new one
     * @param trackView the view that be tracked scroll
     */
    IViewTracker trackView(View trackView);

    /**
     * Just simple bind current {@link #getFollowerView()} to new trackerView
     * Only {@link #getTrackerView()} changed. don't re-attach to screen
     * @param trackView new tracker view
     */
    IViewTracker changeTrackView(View trackView);

    /**
     * Bind a {@link IScrollDetector} of tracker view,in case we
     * can watch scroll state change to make something happen
     * @param scrollDetector tracker view's scroll detector
     */
    IViewTracker into(IScrollDetector scrollDetector);

    /**
     * Observe the visible rect change of tracker view when
     * tracker view scroll position changed
     * @param listener rect change listener
     */
    IViewTracker visibleListener(VisibleChangeListener listener);

    IViewTracker controller(IControllerView controllerView);

    /**
     * Check if the follower view is attach into DecorView {@link android.view.Window#ID_ANDROID_CONTENT}
     */
    boolean isAttach();

    /**
     * Get tracker view's current scroll edge
     */
    int getEdge();

    /**
     * Format edge to readable string
     */
    String getEdgeString();

    /**
     * The view that call scroll vertical and hold the tracker view
     * like {@link android.widget.ListView},{@link android.support.v7.widget.RecyclerView}
     */
    View getVerticalScrollView();

    /**
     * The view that position changed will be tracked
     * or need bind follower view on it.
     */
    View getTrackerView();

    /**
     * Get bound metadata
     */
    MetaData getMetaData();

    /**
     * The unique id of {@link #getTrackerView()}, use this to find next  {@link #getTrackerView()}
     * inside  {@link #getVerticalScrollView()}
     */
    int getTrackerViewId();

    /**
     * The view need bind onto {@link #getTrackerView()},and scroll follow it.
     */
    View getFollowerView();

    /**
     * The root view of {@link #getFollowerView()}, need add into
     * DecorView {@link android.view.Window#ID_ANDROID_CONTENT}
     */
    FloatLayerView getFloatLayerView();

    FrameLayout getVideoTopView();

    FrameLayout getVideoBottomView();

    /**
     * Get bound Activity instance
     */
    Context getContext();

    /**
     * Call this when activity configuration changed
     * @param newConfig new Configuration
     */
    void onConfigurationChanged(Configuration newConfig);

    /**
     * Check if current activity is landscape
     */
    boolean isFullScreen();

    /**
     * Switch current activity to landscape(full-screen)
     */
    void toFullScreen();

    /**
     * Switch current activity to portrait
     */
    void toNormalScreen();

    void muteVideo(boolean mute);

    void startVideo();

    void pauseVideo();

    IControllerView getControllerView();
}
