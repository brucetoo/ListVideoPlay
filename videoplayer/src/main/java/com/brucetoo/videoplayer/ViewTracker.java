package com.brucetoo.videoplayer;

import android.app.Activity;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 15:38
 */
public class ViewTracker implements IViewTracker, ViewTreeObserver.OnScrollChangedListener {

    private static final String TAG = "ViewTracker";

    /**
     * A activity only has a single {@link ViewTracker} instance,for find decor view
     */
    private Activity mContext;

    /**
     * {@link #mTrackView} visible changed listener
     */
    private VisibleChangeListener mVisibleChangeListener;

    /**
     * View that need be tracked scroll changed,Normally inside ListView or RecyclerView
     */
    private View mTrackView;
    /**
     * Float top view in decor view,Normally indicate video root view
     */
    private View mFollowerView;
    /**
     * A view that can scroll vertical,Normally indicate ListView or RecyclerView
     */
    private View mVerticalScrollView;
    /**
     * The whole root view that be added in decor,we can add View inside if needed
     */
    private VideoLayerView mVideoLayerView;

    public ViewTracker(Activity context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null in ViewTracker!");
        }
        this.mContext = context;
    }

    @Override
    public IViewTracker attach(Activity context) {
        if (mVideoLayerView == null) {
            mVideoLayerView = new VideoLayerView(context);
            if (mVideoLayerView.getParent() == null) {
                getDecorView().addView(mVideoLayerView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                mFollowerView = mVideoLayerView.cover;
            }
        }
        return this;
    }

    @Override
    public IViewTracker detach() {
        if (mTrackView != null) {
            mTrackView.getViewTreeObserver().removeOnScrollChangedListener(this);
        }

        if (mVideoLayerView != null && mVideoLayerView.getParent() != null) {
            getDecorView().removeView(mVideoLayerView);
            mVideoLayerView = null;
        }
        mVisibleChangeListener = null;
        mContext = null;//prevent memory leak
        return this;
    }

    @Override
    public View getVerticalScrollView() {
        return mVerticalScrollView;
    }

    @Override
    public View getTrackerView() {
        return mTrackView;
    }

    @Override
    public View getFollowerView() {
        return mFollowerView;
    }

    @Override
    public View getVideoLayerView() {
        return mVideoLayerView;
    }

    @Override
    public IViewTracker trackView(@NonNull View trackView) {
        this.mTrackView = trackView;
        rebindViewToTracker(mFollowerView, mTrackView);
        trackView.getViewTreeObserver().addOnScrollChangedListener(this);
        return this;
    }

    @Override
    public IViewTracker into(@NonNull View verticalScrollView) {
        this.mVerticalScrollView = verticalScrollView;
        return this;
    }

    @Override
    public IViewTracker visibleListener(VisibleChangeListener listener) {
        this.mVisibleChangeListener = listener;
        return this;
    }

    @Override
    public void onScrollChanged() {
        //bind to tracker and move..
        if (mVideoLayerView != null) {// for test
            mVideoLayerView.show.setText(getCalculateValueByString(mTrackView));
        }
        moveCurrentView(mVerticalScrollView, mFollowerView, mTrackView);
    }

    private ViewGroup getDecorView() {
        return (ViewGroup) mContext.getWindow().getDecorView();
    }

    private void rebindViewToTracker(View fromView, View toView) {
        int[] locTo = new int[2];
        toView.getLocationOnScreen(locTo);
        View parent = (View) fromView.getParent();
        ViewAnimator.putOn(parent).translation(locTo[0], locTo[1]);
        fromView.getLayoutParams().width = toView.getWidth();
        fromView.getLayoutParams().height = toView.getHeight();
        fromView.requestLayout();
    }

    private void moveCurrentView(View scrollParent, View fromView, View toView) {
        int[] locScroll = new int[2];
        scrollParent.getLocationOnScreen(locScroll);

        View parent = ((View) fromView.getParent());

        int[] locTo = new int[2];
        toView.getLocationOnScreen(locTo);

        Rect rect = new Rect();
        toView.getLocalVisibleRect(rect);

        Log.e(TAG, "moveCurrentView: rect.top -> " + rect.top
            + " rect.bottom -> " + rect.bottom
            + " rect.left -> " + rect.left
            + " rect.right -> " + rect.right
            + " height ->" + toView.getHeight());

        if (rect.top != 0 || rect.bottom != toView.getHeight()
            || rect.left != 0 || rect.right != toView.getWidth()) { //reach top,bottom,left,right
            //move self
            Log.e(TAG, "moveCurrentView: move self");
            float moveX = 0;
            float moveY = 0;

            //top
            if (rect.top != 0) {
                moveX = -rect.left;
                moveY = -rect.top;
                //let the parent sticky to top
                ViewAnimator.putOn(parent).translation(0, locScroll[1]);
                if (mVisibleChangeListener != null && rect.bottom < 0) {
                    mVisibleChangeListener.onTopOut(this);
                }
            }

            //bottom
            if (rect.bottom != toView.getHeight()) {
                moveY = toView.getHeight() - rect.bottom;
                moveX = toView.getWidth() - rect.right;
                //let the parent sticky to bottom
                ViewAnimator.putOn(parent).translation(0, locScroll[1] + scrollParent.getHeight() - toView.getHeight());
                if (mVisibleChangeListener != null && rect.top != 0) {
                    mVisibleChangeListener.onBottomOut(this);
                }
            }

            //left
            if (rect.left != 0) {
                moveX = -rect.left;
                if (mVisibleChangeListener != null && rect.right != toView.getWidth()) {
                    mVisibleChangeListener.onLeftOut(this);
                }
            }

            //right
            if (rect.right != toView.getWidth()) {
                moveX = toView.getWidth() - rect.right;
                if (mVisibleChangeListener != null && rect.left != 0) {
                    mVisibleChangeListener.onRightOut(this);
                }
            }

            ViewAnimator.putOn(fromView).translation(moveX, moveY);

        } else {
            Log.e(TAG, "moveCurrentView: move parent");
            //move parent
            ViewAnimator.putOn(parent).translation(locTo[0], locTo[1])
                .andPutOn(fromView).translation(0, 0);
        }

        float v1 = (rect.bottom - rect.top) * 1.0f / toView.getHeight();
        float v2 = (rect.right - rect.left) * 1.0f / toView.getWidth();
        if (mVisibleChangeListener != null) {
            mVisibleChangeListener.onVisibleChange(v1 == 1 ? v2 : v1, this);
        }
    }

    private String getCalculateValueByString(View toView) {
        Rect rect = new Rect();
        toView.getLocalVisibleRect(rect);
        StringBuffer buffer = new StringBuffer();
        float v1 = (rect.bottom - rect.top) * 1.0f / toView.getHeight();
        float v2 = (rect.right - rect.left) * 1.0f / toView.getWidth();
        buffer.append("top:").append(rect.top)
            .append(" - ")
            .append("bottom:").append(rect.bottom)
            .append(" \n ")
            .append("left:").append(rect.left)
            .append(" - ")
            .append("right:").append(rect.right)
            .append(" \n ")
            .append("visible:").append(String.format("%.2f", v1 == 1 ? v2 : v1).toString());
        return buffer.toString();
    }

}
