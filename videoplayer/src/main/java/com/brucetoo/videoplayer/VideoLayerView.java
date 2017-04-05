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
import android.widget.ImageView;
import android.widget.TextView;

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

    public View cover;
    public TextView show;

    private void init() {
        FrameLayout videoLayout = new FrameLayout(getContext());
        //For test
        cover = new ImageView(getContext());
        cover.setBackgroundColor(Color.parseColor("#FF0000"));
        videoLayout.addView(cover, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        show = new TextView(getContext());
        show.setText("100%");
        show.setTextColor(Color.parseColor("#00FF00"));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        videoLayout.addView(show, layoutParams);

        //videoLayout height must be WRAP_CONTENT!!!!! for inner move of cover view
        addView(videoLayout,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}
