package com.brucetoo.videoplayer.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Bruce Too
 * On 15/04/2017.
 * At 11:42
 * A async task to get cache drawable of view
 */

public class DrawableTask {

    public interface Callback {

        void done(Object key,BitmapDrawable drawable);
    }

    private View targetView;
    private Callback callback;
    private static ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public DrawableTask(Callback callback){
        this.callback = callback;
    }

    public DrawableTask(View targetView, Callback callback) {
        this.targetView = targetView;
        this.callback = callback;
    }

    public void execute(final Object key, final View target) {
        this.targetView = target;
        THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (targetView != null) {
                    //get bitmap from view
                    //http://stackoverflow.com/a/9595919/4519759
                    Bitmap bitmap = Bitmap.createBitmap(target.getWidth(), target.getHeight(),Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    Drawable bgDrawable =target.getBackground();
                    if (bgDrawable!=null)
                        bgDrawable.draw(canvas);
                    else
                        canvas.drawColor(Color.WHITE);
                    // draw the view on the canvas
                    target.draw(canvas);

                    final BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);

                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                callback.done(key, bitmapDrawable);
                            }
                        });
                    }
                }
            }
        });
    }
}
