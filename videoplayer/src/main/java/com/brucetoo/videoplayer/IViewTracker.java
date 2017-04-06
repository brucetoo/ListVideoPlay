package com.brucetoo.videoplayer;

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

    public static final int NONE_EDGE = 0;
    public static final int TOP_EDGE = 1;
    public static final int BOTTOM_EDGE = 2;
    public static final int LEFT_EDGE = 3;
    public static final int RIGHT_EDGE = 4;

    IViewTracker attach();

    IViewTracker detach();

    IViewTracker destroy();

    IViewTracker trackView(View trackView);

    IViewTracker into(IScrollDetector scrollDetector);

    IViewTracker visibleListener(VisibleChangeListener listener);

    boolean isAttach();

    int getEdge();

    String getEdgeString();

    View getVerticalScrollView();

    View getTrackerView();

    int getTrackerViewId();

    View getFollowerView();

    View getVideoLayerView();
}
