package com.brucetoo.videoplayer.videomanage.controller;

import android.view.View;

import com.brucetoo.videoplayer.IViewTracker;

/**
 * Created by Bruce Too
 * On 14/04/2017.
 * At 16:55
 */

public interface IControllerView {

    View normalScreenController(IViewTracker tracker);

    View fullScreenController(IViewTracker tracker);

    View loadingController(IViewTracker tracker);
}
