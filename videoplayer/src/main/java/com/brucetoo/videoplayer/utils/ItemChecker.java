package com.brucetoo.videoplayer.utils;

import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.brucetoo.videoplayer.tracker.IViewTracker;

/**
 * Created by Bruce Too
 * On 14/03/2017.
 * At 15:33
 */

public class ItemChecker {

    private static final String TAG = "ItemChecker";

    /**
     * Get next available tracker view in current listView
     *
     * @param listView current listView
     */
    public static View getNextTrackerView(ListView listView, IViewTracker tracker) {

        if (listView == null) {
            return null;
        }
        int childCount = listView.getChildCount();

        switch (tracker.getEdge()) {
            case IViewTracker.TOP_EDGE:
            case IViewTracker.RIGHT_EDGE:
            case IViewTracker.LEFT_EDGE:
                for (int i = 0; i < childCount; i++) {
                    View itemView = listView.getChildAt(i);
                    if (itemView == null) {
                        return null;
                    }
                    View container = itemView.findViewById(tracker.getTrackerViewId());
                    if (container == null) {
                        continue;
                    }
                    //only care about cover rect, not itemView
                    Rect rect = new Rect();
                    container.getLocalVisibleRect(rect);
                    Log.e(TAG, "getNextTrackerView Bottom: item = " + i
                        + " rectLocal : " + rect);
                    if (rect.bottom >= 0 && rect.left == 0 && rect.top == 0) {
                        if (rect.bottom - rect.top == container.getHeight()) {
                            return container;
                        }
                    }
                }
                break;
            case IViewTracker.BOTTOM_EDGE:
                for (int i = childCount - 1; i >= 0; i--) {
                    View itemView = listView.getChildAt(i);
                    if (itemView == null) {
                        return null;
                    }
                    View container = itemView.findViewById(tracker.getTrackerViewId());
                    if (container == null) {
                        continue;
                    }
                    //only care about cover rect, not itemView
                    Rect rect = new Rect();
                    container.getLocalVisibleRect(rect);
                    Log.e(TAG, "getNextTrackerView Bottom: item = " + i
                        + " rectLocal : " + rect);
                    if (rect.left == 0 && rect.top == 0) {
                        if (rect.bottom - rect.top == container.getHeight()) {
                            return container;
                        }
                    }
                }
                break;
        }
        return null;
    }

    /**
     * Get current most visible height item view of ListView
     *
     * @param listView current listView
     * @param coverId  video thumbnail view id for video layer to take place
     * @return item view
     */
    public static View getRelativeMostVisibleItemView(ListView listView, @IdRes int coverId) {
        if (listView == null) {
            return null;
        }
        int childCount = listView.getChildCount();
        int mostVisibleItemIndex = -1;
        int maxVisibleHeight = 0;
        for (int i = 0; i < childCount; i++) {
            View itemView = listView.getChildAt(i);
            if (itemView == null) {
                return null;
            }
            View container = itemView.findViewById(coverId);
            if (container == null) {
                continue;
            }
            //only care about cover rect, not itemView
            Rect rect = new Rect();
            container.getLocalVisibleRect(rect);
            if (rect.bottom >= 0 && rect.left == 0 && rect.top == 0) {
                int visibleHeight = rect.bottom - rect.top;
                if (maxVisibleHeight < visibleHeight) {
                    maxVisibleHeight = visibleHeight;
                    mostVisibleItemIndex = i;
                }
            }
        }
        return listView.getChildAt(mostVisibleItemIndex);
    }
}
