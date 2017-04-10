package com.brucetoo.videoplayer;

import android.app.Activity;
import android.support.v4.util.ArrayMap;

/**
 * Created by Bruce Too
 * On 01/04/2017.
 * At 17:38
 */

public class Tracker {

    private static ArrayMap<Activity, IViewTracker> mViewTrackers = new ArrayMap<>();

    public static IViewTracker attach(Activity context) {
        IViewTracker iViewTracker = mViewTrackers.get(context);
        if (iViewTracker != null) {
            return iViewTracker.attach();
        }
        IViewTracker tracker = new ViewTracker(context).attach();
        mViewTrackers.put(context, tracker);
        return tracker;
    }

    public static IViewTracker detach(Activity context) {
        IViewTracker iViewTracker = mViewTrackers.remove(context);
        if (iViewTracker != null) {
            return iViewTracker.detach();
        }
        return null;
    }

    public static IViewTracker destroy(Activity context){
        IViewTracker iViewTracker = mViewTrackers.remove(context);
        if (iViewTracker != null) {
            return iViewTracker.destroy();
        }
        return null;
    }

    public static boolean isAttach(Activity context){
        IViewTracker iViewTracker = mViewTrackers.get(context);
        if (iViewTracker != null) {
            return iViewTracker.isAttach();
        }
        return false;
    }

    public static IViewTracker getViewTracker(Activity context){
        return mViewTrackers.get(context);
    }


}

