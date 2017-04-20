package com.brucetoo.videoplayer.videomanage.player;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Bruce Too
 * On 13/04/2017.
 * At 15:20
 */

public class RatioImageView extends android.support.v7.widget.AppCompatImageView {

    private int mWidth;
    private int mHeight;

    public RatioImageView(Context context) {
        this(context, null);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRatio(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mWidth > 0 && mHeight > 0) {
            float ratio = (float) mWidth / mHeight;
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (width > 0) {
                height = (int) (width / ratio);
            } else if (height > 0) {
                width = (int) (height * ratio);
            }
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}