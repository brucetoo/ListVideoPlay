package com.brucetoo.videoplayer;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 17:47
 */

public interface VisibleChangeListener {

    void onVisibleChange(float visibleRatio,IViewTracker tracker);

    void onTopOut(IViewTracker tracker);

    void onBottomOut(IViewTracker tracker);

    void onLeftOut(IViewTracker tracker);

    void onRightOut(IViewTracker tracker);
}
