package com.brucetoo.videoplayer.utils;

import android.content.Context;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

/**
 * Created by Bruce Too
 * On 10/20/16.
 * At 11:49
 */

public class Utils {


    public static int dp2px(Context context, double dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public static int getDeviceWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(mDisplayMetrics);
        return mDisplayMetrics.widthPixels;
    }

    public static int getDeviceHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(mDisplayMetrics);
        return mDisplayMetrics.heightPixels;
    }

    public static int getStatusBarHeight(Context context) {

        int statusBarHeight = 0;

        try {
            int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = context.getResources()
                    .getDimensionPixelSize(resourceId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * Get view at position of ListView
     *
     * @param pos      position
     * @param listView ListView
     * @return item view
     */
    public static View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public static boolean isSystemRotationEnabled(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

}
