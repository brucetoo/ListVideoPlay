package com.brucetoo.videoplayer;

import android.app.Activity;
import android.support.v4.util.ArrayMap;
import android.view.View;

/**
 * Created by Bruce Too
 * On 01/04/2017.
 * At 17:38
 */

public class Tracker {

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



}

