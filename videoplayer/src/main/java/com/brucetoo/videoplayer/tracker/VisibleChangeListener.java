package com.brucetoo.videoplayer.tracker;

import com.brucetoo.videoplayer.tracker.IViewTracker;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 17:47
 */

public interface VisibleChangeListener {

    void onVisibleChange(float visibleRatio, IViewTracker tracker);

}
