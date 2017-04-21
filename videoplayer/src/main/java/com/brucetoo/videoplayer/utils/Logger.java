package com.brucetoo.videoplayer.utils;

import android.util.Log;

import com.brucetoo.videoplayer.Config;

public class Logger {

    public static void e(final String TAG, final String message) {
        if (Config.SHOW_LOGS)
            Log.e(TAG, attachThreadId(message));
    }

    public static void e(final String TAG, final String message, Throwable throwable) {
        if (Config.SHOW_LOGS)
            Log.e(TAG, attachThreadId(message), throwable);
    }

    public static void w(final String TAG, final String message) {
        if (Config.SHOW_LOGS)
            Log.w(TAG, attachThreadId(message));
    }

    public static void i(final String TAG, final String message) {
        if (Config.SHOW_LOGS)
            Log.i(TAG, attachThreadId(message));
    }

    public static void d(final String TAG, final String message) {
        if (Config.SHOW_LOGS)
            Log.d(TAG, attachThreadId(message));
    }

    public static void v(final String TAG, final String message) {
        if (Config.SHOW_LOGS)
            Log.v(TAG, attachThreadId(message));
    }

    private static String attachThreadId(String str) {
        return Thread.currentThread().getId() + " " + str;
    }

}
