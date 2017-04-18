package com.brucetoo.videoplayer.videomanage.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brucetoo.videoplayer.R;

/**
 * Created by Bruce Too
 * On 14/04/2017.
 * At 17:05
 */

public class LoadingControllerView extends BaseControllerView {

    public LoadingControllerView(Context context) {
        super(context);
    }

    public LoadingControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private RotateAnimation animation;
    private ImageView loading;

    @Override
    protected void initView() {

        loading = new ImageView(getContext());
        loading.setImageResource(R.drawable.video_loading);
        RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_IN_PARENT);
        addView(loading, params);

        animation = new RotateAnimation(0,361,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(700);
    }

    @Override
    protected void attachWindow(boolean attach) {
        if(attach){
            loading.startAnimation(animation);
        }else {
            loading.clearAnimation();
        }
    }
}
