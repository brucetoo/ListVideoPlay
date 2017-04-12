package com.brucetoo.videoplayer;

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

import com.brucetoo.videoplayer.videomanage.ui.VideoPlayerView;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 16:27
 */

public class VideoLayerView extends FrameLayout {

    public VideoLayerView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoLayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoLayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public View videoBackView;
    public TextView show;
    private VideoPlayerView mVideoPlayerView;

    private void init() {
        FrameLayout videoLayout = new FrameLayout(getContext());
        //TODO this is just for demo test,need reconstruct
        mVideoPlayerView = new VideoPlayerView(getContext());

        FrameLayout videoRoot = new FrameLayout(getContext());
        videoBackView = new View(getContext());
        videoBackView.setBackgroundColor(Color.parseColor("#000000"));
        videoRoot.addView(videoBackView);
        videoRoot.addView(mVideoPlayerView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        videoLayout.addView(videoRoot);

        show = new TextView(getContext());
        show.setText("100%");
        show.setTextColor(Color.parseColor("#00FF00"));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        videoLayout.addView(show, layoutParams);

        //videoLayout height must be WRAP_CONTENT!!!!! for inner move of cover view
        addView(videoLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public View getVideoRootView() {
        return (View) mVideoPlayerView.getParent();
    }

    public VideoPlayerView getVideoPlayerView(){
        return mVideoPlayerView;
    }

}
