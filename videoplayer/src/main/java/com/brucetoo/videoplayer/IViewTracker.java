package com.brucetoo.videoplayer;

import android.content.Context;
import android.view.View;

import com.brucetoo.videoplayer.scrolldetector.IScrollDetector;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 15:01
 * Helper class that bind a view rect area to another one,
 * And track origin location changed
 */

public interface IViewTracker {

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
     * {@link #detach()} and release unnecessary resources
     */
    IViewTracker destroy();

    /**
     * Offer a tracker view for follower view to track
     * @param trackView the view that be tracked scroll
     */
    IViewTracker trackView(View trackView);

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
    View getVideoLayerView();

    Context getContext();
}
