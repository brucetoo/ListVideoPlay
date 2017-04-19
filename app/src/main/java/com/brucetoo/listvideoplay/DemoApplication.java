package com.brucetoo.listvideoplay;

import android.app.Application;

import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;

/**
 * Created by Bruce Too
 * On 19/04/2017.
 * At 21:46
 */

public class DemoApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        BlockCanary.install(this, new BlockCanaryContext()).start();
    }
}
