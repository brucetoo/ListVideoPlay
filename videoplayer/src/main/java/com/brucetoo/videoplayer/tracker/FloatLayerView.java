package com.brucetoo.videoplayer.tracker;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.brucetoo.videoplayer.videomanage.player.VideoPlayerView;


/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 16:27
 * This is used to added into {@link android.view.Window#ID_ANDROID_CONTENT} decor view
 * It contains:
 * 1. rootLayout which hold all views to be added in {@link FloatLayerView},height must be WRAP_CONTENT
 * 2. {@link #mVideoBottomView} in bottom layer of {@link #mVideoPlayerView}, which can be used to add some mask view...
 * 3. {@link #mVideoTopView} in top layer of {@link #mVideoPlayerView},which can add some video controller view...
 */

public class FloatLayerView extends FrameLayout {

    public FloatLayerView(@NonNull Context context) {
        super(context);
        init();
    }

    public FloatLayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatLayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //test view always lay in top
    public TextView testView;
    private FrameLayout mVideoBottomView;
    //TODO Find a way to make VideoPlayerView configurable,put it inside IControllerView?
    private VideoPlayerView mVideoPlayerView;
    //TODO overdraw problems??
    private FrameLayout mVideoTopView;

    private void init() {
        mVideoBottomView = new FrameLayout(getContext());
        mVideoTopView = new FrameLayout(getContext());
        mVideoPlayerView = new VideoPlayerView(getContext());

        FrameLayout videoRoot = new FrameLayout(getContext());
        videoRoot.addView(mVideoBottomView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        videoRoot.addView(mVideoPlayerView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        videoRoot.addView(mVideoTopView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        testView = new TextView(getContext());
        testView.setTextColor(Color.parseColor("#00FF00"));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        videoRoot.addView(testView, layoutParams);

        FrameLayout rootLayout = new FrameLayout(getContext());
        rootLayout.addView(videoRoot,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //NOTE: root layout'heihgt must be WRAP_CONTENT
        addView(rootLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public View getVideoRootView() {
        return (View) mVideoPlayerView.getParent();
    }

    public FrameLayout getVideoBottomView(){
        return mVideoBottomView;
    }

    public FrameLayout getVideoTopView(){
        return mVideoTopView;
    }

    public VideoPlayerView getVideoPlayerView(){
        return mVideoPlayerView;
    }

}
