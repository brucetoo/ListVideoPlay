package com.brucetoo.videoplayer.videomanage.controller;

import android.view.View;

import com.brucetoo.videoplayer.tracker.IViewTracker;
import com.brucetoo.videoplayer.tracker.VideoTracker;

/**
 * Created by Bruce Too
 * On 14/04/2017.
 * At 16:55
 *
 */

public interface IControllerView {

    /**
     * Normal video controller view added in {@link VideoTracker#mVideoTopView}
     * when attach to portrait(normal) tracker view
     */
    View normalScreenController(IViewTracker tracker);

    /**
     * Detail video controller view added in {@link VideoTracker#mVideoTopView}
     * when attach to detail tracker view
     */
    View detailScreenController(IViewTracker tracker);

    /**
     * Full screen video controller view added in {@link VideoTracker#mVideoTopView}
     * when attach to landscape screen
     */
    View fullScreenController(IViewTracker tracker);

    /**
     * Loading state controller view added in {@link VideoTracker#mVideoTopView}
     * when video is preparing
     */
    View loadingController(IViewTracker tracker);

    /**
     * Another controller view need be added in {@link VideoTracker#mVideoTopView}
     */
    View anotherController(IViewTracker tracker);

    /**
     * Control mute video or not, mute video default.
     * @return if mute video
     */
    boolean muteVideo();

    /**
     * Enable auto rotation screen if system "auto-rotate" switch is open
     * or when devices is landscape.Default enable auto rotate
     * @return enable rotate
     */
    boolean enableAutoRotation();
}
