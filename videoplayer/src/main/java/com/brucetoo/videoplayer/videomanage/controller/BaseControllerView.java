package com.brucetoo.videoplayer.videomanage.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.brucetoo.videoplayer.tracker.IViewTracker;
import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;

/**
 * Created by Bruce Too
 * On 18/04/2017.
 * At 14:03
 * Base controller view that be added in {@link IViewTracker#getVideoTopView()}
 * to control {@link VideoPlayerView} play video logic.
 * Normally include
 * {@link IControllerView#normalScreenController(IViewTracker)}
 * {@link IControllerView#loadingController(IViewTracker)} (IViewTracker)}
 * {@link IControllerView#fullScreenController(IViewTracker)} (IViewTracker)}
 *
 * if care about detail screen,
 * {@link IControllerView#detailScreenController(IViewTracker)} (IViewTracker)} (IViewTracker)}
 * is suit for you.
 */

public abstract class BaseControllerView extends RelativeLayout {

    protected IViewTracker mViewTracker;
    protected VideoPlayerView mVideoPlayerView;

    public BaseControllerView(Context context) {
        super(context);
        initView();
    }

    public BaseControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BaseControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachWindow(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachWindow(false);
    }

    protected abstract void initView();

    protected abstract void attachWindow(boolean attach);

    public void setViewTracker(IViewTracker viewTracker){
        this.mViewTracker = viewTracker;
//        if(mViewTracker.getFloatLayerView() != null) {
            mVideoPlayerView = mViewTracker.getFloatLayerView().getVideoPlayerView();
//        }
    }

}
