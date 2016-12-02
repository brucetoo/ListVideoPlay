package com.brucetoo.listvideoplay.videomanage.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.brucetoo.listvideoplay.demo.Utils;

/**
 * Created by Bruce Too
 * On 03/11/2016.
 * At 10:26
 */

public class HighLightMaskView extends View {

    private int mStartY = -1;
    private int mEndY = -1;
    private int mMaskColor = 0xdd000000;
    private int mCurrentMaskColor = mMaskColor;
    private Paint mColorPaint;
    private ValueAnimator mAnim;

    public HighLightMaskView(Context context) {
        this(context, null);
    }

    public HighLightMaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HighLightMaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HighLightMaskView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mColorPaint = new Paint();
        mColorPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(Utils.getDeviceWidth(getContext()), MeasureSpec.EXACTLY));
        int height = MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(Utils.getDeviceHeight(getContext()), MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mStartY != -1 && mEndY != -1) {

            mColorPaint.setColor(Color.TRANSPARENT);
            canvas.drawRect(0, mStartY, getWidth(), mEndY, mColorPaint);

            mColorPaint.setColor(mCurrentMaskColor);
            canvas.drawRect(0, 0, getWidth(), mStartY, mColorPaint);
            canvas.drawRect(0, mEndY, getWidth(), getHeight(), mColorPaint);

        }
    }

    public void setMaskColor(int maskColor) {
        this.mMaskColor = maskColor;
    }

    public void startAlpha2NormalLight() {
        Log.e("HighLightMaskView","startAlpha2NormalLight");
        startAnimator(mCurrentMaskColor, mMaskColor);
    }

    public void startAlpha2HighLight(){
         Log.e("HighLightMaskView","startAlpha2HighLight");
         startAnimator(mCurrentMaskColor, 0x00000000);
    }

    public void updateStartAndEndY(int scrollDelta) {
        this.mStartY += scrollDelta;
        this.mEndY += scrollDelta;
        Log.e("setStartAndEndY", "update startY:" + mStartY + " endY:" + mEndY);
        invalidate();
    }

    public void setStartAndEndY(View currentItemView) {

        int[] loc = new int[2];
        currentItemView.getLocationOnScreen(loc);
        //must minus status bar height
        loc[1] = loc[1] - Utils.getStatusBarHeight(getContext());
        this.mStartY = loc[1];
        this.mEndY = loc[1] + currentItemView.getHeight();
        Log.e("setStartAndEndY", "startY:" + mStartY + " endY:" + mEndY);

        startAnimator(0x00000000, mMaskColor);

    }

    private void startAnimator(int from, int to) {

        if(mAnim != null){
            mAnim.removeAllUpdateListeners();
            mAnim.removeAllListeners();
            mAnim = null;
        }
        mAnim = ValueAnimator.ofArgb(from, to);
        mAnim.setDuration(1500);
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentMaskColor = (int) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        mAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.e("HighLightMaskView","onAnimationEnd");
            }
        });
        mAnim.start();
    }
}
