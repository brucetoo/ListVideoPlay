package com.brucetoo.videoplayer;

import android.app.Activity;
import android.view.View;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 15:01
 * Helper class that bind a view rect area to another one,
 * And track origin location changed
 */

public interface IViewTracker {

    IViewTracker attach(Activity context);

    IViewTracker detach();

    IViewTracker trackView(View trackView);

    IViewTracker into(View verticalScrollView);

    IViewTracker visibleListener(VisibleChangeListener listener);

    View getVerticalScrollView();

    View getTrackerView();

    View getFollowerView();

    View getVideoLayerView();
}
