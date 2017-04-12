package com.brucetoo.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.brucetoo.videoplayer.scrolldetector.IScrollDetector;
import com.brucetoo.videoplayer.utils.Utils;
import com.brucetoo.videoplayer.utils.ViewAnimator;
import com.brucetoo.videoplayer.videomanage.manager.SingleVideoPlayerManager;
import com.brucetoo.videoplayer.videomanage.manager.VideoPlayerManager;
import com.brucetoo.videoplayer.videomanage.meta.MetaData;

import static android.view.View.NO_ID;

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

    /**
     * {@link #mTrackView}'s current edge triggered
     */
    private int mCurrentEdge = NONE_EDGE;

    /**
     * {@link #getTrackerView()} scroll state change detector
     */
    private IScrollDetector mScrollDetector;

    private boolean mIsAttach;

    private int mTrackViewId = View.NO_ID;

    //TODO this is just for test
    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(null);

    public ViewTracker(Activity context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null in ViewTracker!");
        }
        this.mContext = context;
    }

    @Override
    public IViewTracker attach() {
        if (mVideoLayerView == null) {
            mVideoLayerView = new VideoLayerView(mContext);
            if (mVideoLayerView.getParent() == null) {
                getDecorView().addView(mVideoLayerView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                mFollowerView = mVideoLayerView.getVideoRootView();
                //TODO this is just demo to play,need reconstruct
                // FIXME: 12/04/2017  need reconstruct
                mVideoPlayerManager.playNewVideo(null, mVideoLayerView.getVideoPlayerView(),"http://wvideo.spriteapp.cn/video/2016/1016/e2b1aa68-93a0-11e6-bd25-90b11c479401_wpc.mp4");
            }
        }
        mIsAttach = true;
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
        mVideoPlayerManager.stopAnyPlayback();
        mIsAttach = false;
        return this;
    }

    @Override
    public IViewTracker destroy() {
        detach();
        mVisibleChangeListener = null;
        mContext = null;//prevent memory leak
        mScrollDetector.detach();
        mScrollDetector = null;
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
    public int getTrackerViewId() {
        return mTrackViewId;
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
    public Context getContext() {
        return mContext;
    }

    @Override
    public IViewTracker trackView(@NonNull View trackView) {
        if (mTrackView != null) {//not first
            detach();
            attach();
        }
        this.mTrackView = trackView;
        int id = mTrackView.getId();
        if (id == NO_ID) {
            throw new IllegalStateException("Tracked view must set ID before use !");
        }
        mTrackViewId = id;
        rebindViewToTracker(mFollowerView, mTrackView);
        trackView.getViewTreeObserver().addOnScrollChangedListener(this);
        return this;
    }

    @Override
    public IViewTracker into(@NonNull IScrollDetector scrollDetector) {
        this.mScrollDetector = scrollDetector;
        this.mVerticalScrollView = scrollDetector.getView();
        scrollDetector.setTracker(this);
        return this;
    }

    @Override
    public IViewTracker visibleListener(VisibleChangeListener listener) {
        this.mVisibleChangeListener = listener;
        return this;
    }

    @Override
    public boolean isAttach() {
        return mIsAttach;
    }

    @Override
    public int getEdge() {
        return mCurrentEdge;
    }

    @Override
    public String getEdgeString() {
        String edge = "";
        switch (mCurrentEdge) {
            case TOP_EDGE:
                edge = "TOP_EDGE";
                break;
            case BOTTOM_EDGE:
                edge = "BOTTOM_EDGE";
                break;
            case LEFT_EDGE:
                edge = "LEFT_EDGE";
                break;
            case RIGHT_EDGE:
                edge = "RIGHT_EDGE";
                break;
            case NONE_EDGE:
                edge = "NONE_EDGE";
                break;
        }
        return edge;
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
        Log.e(TAG, "rebindViewToTracker locTo[0] -> " + locTo[0] + " locTo[1] -> " + locTo[1]);
        ViewAnimator.putOn(parent).translation(locTo[0], locTo[1])
            .andPutOn(fromView).translation(0, 0);
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

        int[] locFrom = new int[2];
        fromView.getLocationOnScreen(locFrom);

        Rect rect = new Rect();
        toView.getLocalVisibleRect(rect);

        Log.e(TAG, "moveCurrentView: rect.top -> " + rect.top
            + " rect.bottom -> " + rect.bottom
            + " rect.left -> " + rect.left
            + " rect.right -> " + rect.right
            + " locTo[0] -> " + locTo[0]
            + " locTo[1] -> " + locTo[1]
            + " locFrom[0] -> " + locFrom[0]
            + " locFrom[1] -> " + locFrom[1]);

        //TODO 此判断有问题,不够严谨
        if (rect.top != 0 || rect.bottom != toView.getHeight()
            || rect.left != 0 || rect.right != toView.getWidth()) { //reach top,bottom,left,right
            //move self
            Log.e(TAG, "moveCurrentView: move self");
            float moveX = 0;
            float moveY = 0;

            //top
            if (rect.top > 0 && rect.top != 0) {
                moveX = -rect.left;
                moveY = -rect.top;
                //let the parent sticky to top
                ViewAnimator.putOn(parent).translation(getVerticalScrollView().getPaddingLeft() + getVerticalScrollView().getLeft(),
                    locScroll[1] + getVerticalScrollView().getPaddingTop());
                mCurrentEdge = TOP_EDGE;
            }

            //bottom
            if (rect.bottom > 0 && rect.bottom != toView.getHeight()) {
                moveY = toView.getHeight() - rect.bottom;
                moveX = toView.getWidth() - rect.right;
                //let the parent sticky to bottom
                ViewAnimator.putOn(parent).translation(getVerticalScrollView().getPaddingLeft() + getVerticalScrollView().getLeft(),
                    locScroll[1] + scrollParent.getHeight() - toView.getHeight() - getVerticalScrollView().getPaddingBottom());
                mCurrentEdge = BOTTOM_EDGE;
            }

            //left
            if (rect.left > 0 && rect.left != 0) {
                moveX = -rect.left;
                //let the parent sticky to left
                ViewAnimator.putOn(parent).translationX(0);
                mCurrentEdge = LEFT_EDGE;
            }

            //right
            if (rect.right > 0 && rect.right != toView.getWidth()) {
                moveX = toView.getWidth() - rect.right;
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getVerticalScrollView().getLayoutParams();
                //let the parent sticky to right
                ViewAnimator.putOn(parent).translationX(getVerticalScrollView().getPaddingRight() + getVerticalScrollView().getPaddingLeft()
                    + layoutParams.leftMargin + layoutParams.rightMargin);
                mCurrentEdge = RIGHT_EDGE;
            }

            if (rect.left < 0 && rect.right < 0) {
                mCurrentEdge = LEFT_EDGE;
            }

            if (rect.right >= Utils.getDeviceWidth(mContext)) {
                mCurrentEdge = RIGHT_EDGE;
            }


            ViewAnimator.putOn(fromView).translation(moveX, moveY);

            float v1 = (rect.bottom - rect.top) * 1.0f / toView.getHeight();
            float v2 = (rect.right - rect.left) * 1.0f / toView.getWidth();
            if (mVisibleChangeListener != null) {
                mVisibleChangeListener.onVisibleChange(v1 == 1 ? v2 : v1, this);
            }
        } else {

            if (locTo[0] != 0 || locTo[1] != 0) {
                Log.e(TAG, "moveCurrentView: move parent");
                //move parent
                ViewAnimator.putOn(parent).translation(locTo[0], locTo[1])
                    .andPutOn(fromView).translation(0, 0);
                mCurrentEdge = NONE_EDGE;
            } else {
                mCurrentEdge = TOP_EDGE;
            }
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
