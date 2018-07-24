package com.brucetoo.videoplayer.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.brucetoo.videoplayer.Config;
import com.brucetoo.videoplayer.R;
import com.brucetoo.videoplayer.scrolldetector.IScrollDetector;
import com.brucetoo.videoplayer.utils.Logger;
import com.brucetoo.videoplayer.utils.Utils;
import com.brucetoo.videoplayer.utils.ViewAnimator;
import com.brucetoo.videoplayer.videomanage.controller.IControllerView;
import com.brucetoo.videoplayer.videomanage.meta.MetaData;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 15:38
 */
public class ViewTracker implements IViewTracker, ViewTreeObserver.OnScrollChangedListener {

    private static final String TAG = ViewTracker.class.getSimpleName();

    /**
     * A activity only has a single {@link ViewTracker} instance,for find decor view
     */
    protected Activity mContext;

    /**
     * {@link #mTrackView} visible changed listener
     */
    protected VisibleChangeListener mVisibleChangeListener;

    /**
     * View that need be tracked scroll changed,Normally inside ListView or RecyclerView
     */
    protected View mTrackView;

    /**
     * The root layer of video container,Normally contains video view
     */
    protected View mFollowerView;

    /**
     * The top layer of {@link #mFollowerView},we can add video controller view...
     */
    protected FrameLayout mVideoTopView;

    /**
     * The bottom layer of {@link #mFollowerView},we can add mask view or set drawable of {@link #getTrackerView()}
     */
    protected FrameLayout mVideoBottomView;

    /**
     * A view that can scroll vertical,Normally indicate ListView or RecyclerView
     */
    protected View mVerticalScrollView;

    /**
     * The whole root view that be added in decor,we can add View inside if needed
     */
    protected FloatLayerView mFloatLayerView;

    protected MetaData mMetaData;

    /**
     * {@link #mTrackView}'s current edge triggered
     */
    protected int mCurrentEdge = NONE_EDGE;

    /**
     * {@link #getTrackerView()} scroll state change detector
     */
    protected IScrollDetector mScrollDetector;

    protected IControllerView mControllerView;

    protected boolean mIsAttach;

    /**
     * Origin activity flag and system ui visibility
     */
    protected int mOriginActivityFlag;

    protected int mOriginSystemUIVisibility;

    /**
     * Origin location and width/height params about {@link #mFollowerView}
     */
    protected int mOriginX;

    protected int mOriginY;

    protected int mOriginWidth;

    protected int mOriginHeight;

    public ViewTracker(Activity context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null in ViewTracker!");
        }
        this.mContext = context;
    }

    @Override
    public IViewTracker attach() {
        //remove FloatLayerView from decorView if exits before add new one
        for (int i = 0; i < getDecorView().getChildCount(); i++) {
            View child = getDecorView().getChildAt(i);
            if(child instanceof FloatLayerView){
                getDecorView().removeView(child);
            }
        }
        if (mFloatLayerView == null) {//first time
            mFloatLayerView = new FloatLayerView(mContext);
            mFollowerView = mFloatLayerView.getVideoRootView();
            mVideoTopView = mFloatLayerView.getVideoTopView();
            mVideoBottomView = mFloatLayerView.getVideoBottomView();
            restoreActivityFlag();
        }
        if(mFloatLayerView.getParent() == null) {
            getDecorView().addView(mFloatLayerView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
        mIsAttach = true;
        return this;
    }

    @Override
    public IViewTracker detach() {
        if (mTrackView != null) {
            mTrackView.getViewTreeObserver().removeOnScrollChangedListener(this);
        }

        if (mFloatLayerView != null) {
            getDecorView().removeView(mFloatLayerView);
        }
        mIsAttach = false;
        return this;
    }

    @Override
    public IViewTracker hide() {
        if(mFloatLayerView != null){
            mFloatLayerView.setVisibility(View.INVISIBLE);
        }
        return this;
    }

    @Override
    public IViewTracker show() {
        if(mFloatLayerView != null){
            mFloatLayerView.setVisibility(View.VISIBLE);
        }
        return this;
    }

    @Override
    public IViewTracker destroy() {
        detach();
        mVisibleChangeListener = null;
//        mContext = null;//prevent memory leak?
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
    public MetaData getMetaData() {
        return mMetaData;
    }

    @Override
    public int getTrackerViewId() {
        return R.id.view_tracker;
    }

    @Override
    public View getFollowerView() {
        return mFollowerView;
    }

    @Override
    public FloatLayerView getFloatLayerView() {
        return mFloatLayerView;
    }

    @Override
    public FrameLayout getVideoTopView() {
        return mVideoTopView;
    }

    @Override
    public FrameLayout getVideoBottomView() {
        return mVideoBottomView;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        if (newConfig.getLayoutDirection() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        if (isFullScreen()) {
            Window window = mContext.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }

            rebindTrackerView(0, 0, Utils.getDeviceWidth(mContext), Utils.getDeviceHeight(mContext));
        } else {
            Window window = mContext.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(mOriginActivityFlag);
            window.getDecorView().setSystemUiVisibility(mOriginSystemUIVisibility);

            rebindTrackerView(mOriginX, mOriginY, mOriginWidth, mOriginHeight);
        }
    }

    @Override
    public boolean isFullScreen() {
        int orientation = mContext.getRequestedOrientation();
        return orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    }

    @Override
    public void toFullScreen() {
        mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @Override
    public void toNormalScreen() {
        mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void muteVideo(boolean mute) {

    }

    @Override
    public void startVideo() {

    }

    @Override
    public void pauseVideo() {

    }

    @Override
    public IControllerView getControllerView() {
        return mControllerView;
    }

    @Override
    public IViewTracker trackView(@NonNull View trackView) {
        if (mTrackView != null) {//not first
            detach();
            attach();
        }
        this.mTrackView = trackView;
        int id = mTrackView.getId();
        if (id != R.id.view_tracker) {
            throw new IllegalStateException("Tracker view id must be R.id.view_tracker !");
        }
        rebindViewToTracker(mFollowerView, mTrackView);
        trackView.getViewTreeObserver().addOnScrollChangedListener(this);
        return this;
    }

    @Override
    public IViewTracker changeTrackView(View trackView) {
        //clear old track new
        this.mTrackView.getViewTreeObserver().removeOnScrollChangedListener(this);
        this.mTrackView = trackView;
        int id = mTrackView.getId();
        if (id != R.id.view_tracker) {
            throw new IllegalStateException("Tracker view id must be R.id.view_tracker !");
        }
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
    public IViewTracker controller(IControllerView controllerView) {
        this.mControllerView = controllerView;
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
        if (mFloatLayerView != null && Config.SHOW_DEBUG_RECT) {// for test
            mFloatLayerView.testView.setText(getCalculateValueByString(mTrackView));
        }
        //only move follower view in portrait screen
        if (!isFullScreen()) {
            moveCurrentView(mVerticalScrollView, mFollowerView, mTrackView);
        }
    }

    private ViewGroup getDecorView() {
        return (ViewGroup) mContext.getWindow().getDecorView();
    }

    private void rebindTrackerView(int x, int y, int width, int height) {
        View parent = (View) mFollowerView.getParent();
        ViewAnimator.putOn(parent).translation(x, y)
            .andPutOn(mFloatLayerView).translation(0, 0);
        mFollowerView.getLayoutParams().width = width;
        mFollowerView.getLayoutParams().height = height;
        mFollowerView.requestLayout();
    }

    private void rebindViewToTracker(View fromView, View toView) {
        int[] locTo = new int[2];
        toView.getLocationOnScreen(locTo);
        View parent = (View) fromView.getParent();
        Logger.i(TAG, "rebindViewToTracker locTo[0] -> " + locTo[0] + " locTo[1] -> " + locTo[1]);
        ViewAnimator.putOn(parent).translation(locTo[0], locTo[1])
            .andPutOn(fromView).translation(0, 0);
        mOriginX = locTo[0];
        mOriginY = locTo[1];
        mOriginWidth = toView.getWidth();
        mOriginHeight = toView.getHeight();
        Logger.i(TAG, "rebindViewToTracker mOriginX:" + mOriginX + " mOriginY:" + mOriginY + " mOriginWidth:" + mOriginWidth + " mOriginHeight:" + mOriginHeight);
        fromView.getLayoutParams().width = mOriginWidth;
        fromView.getLayoutParams().height = mOriginHeight;
        fromView.requestLayout();
    }


    private int[] locScroll = new int[2];
    private int[] locTo = new int[2];
    private int[] locFrom = new int[2];
    private Rect toViewR = new Rect();
    private Rect scrollViewR = new Rect();

    private void moveCurrentView(View scrollParent, View fromView, View toView) {

        scrollParent.getLocationOnScreen(locScroll);

        View parent = ((View) fromView.getParent());

        toView.getLocationOnScreen(locTo);

        fromView.getLocationOnScreen(locFrom);

        toView.getLocalVisibleRect(toViewR);

        scrollParent.getLocalVisibleRect(scrollViewR);

        Logger.w(TAG, "moveCurrentView: toViewR.top -> " + toViewR.top
            + " toViewR.bottom -> " + toViewR.bottom
            + " toViewR.left -> " + toViewR.left
            + " toViewR.right -> " + toViewR.right
            + " locTo[0] -> " + locTo[0]
            + " locTo[1] -> " + locTo[1]
            + " locFrom[0] -> " + locFrom[0]
            + " locFrom[1] -> " + locFrom[1]);

        if (toViewR.top != 0 || toViewR.bottom != toView.getHeight()
            || toViewR.left != 0 || toViewR.right != toView.getWidth()) { //reach top,bottom,left,right
            //move self
            Logger.v(TAG, "moveCurrentView: move self");
            float moveX = 0;
            float moveY = 0;

            //top
            if (toViewR.top > 0 && toViewR.top != 0) {
                moveX = -toViewR.left;
                moveY = -toViewR.top;
                //let the parent sticky to top
                ViewAnimator.putOn(parent).translation(locTo[0],
                        locScroll[1] + scrollParent.getPaddingTop());
                mCurrentEdge = TOP_EDGE;
            }

            //bottom
            if (toViewR.bottom > 0 && toViewR.bottom != toView.getHeight()) {
                moveY = toView.getHeight() - toViewR.bottom;
                moveX = toView.getWidth() - toViewR.right;
                //let the parent sticky to bottom
                ViewAnimator.putOn(parent).translation(locTo[0],
                        locScroll[1] + scrollViewR.bottom - scrollViewR.top - toView.getMeasuredHeight());
                mCurrentEdge = BOTTOM_EDGE;
            }

            //left
            if (toViewR.left > 0 && toViewR.left != 0) {
                moveX = -toViewR.left;
                //let the parent sticky to left
                ViewAnimator.putOn(parent).translationX(0);
                mCurrentEdge = LEFT_EDGE;
            }

            //right
            if (toViewR.right > 0 && toViewR.right != toView.getWidth()) {
                moveX = toView.getWidth() - toViewR.right;
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getVerticalScrollView().getLayoutParams();
                //let the parent sticky to right
                ViewAnimator.putOn(parent).translationX(getVerticalScrollView().getPaddingRight() + getVerticalScrollView().getPaddingLeft()
                    + layoutParams.leftMargin + layoutParams.rightMargin);
                mCurrentEdge = RIGHT_EDGE;
            }

            if (toViewR.left < 0 && toViewR.right < 0) {
                mCurrentEdge = LEFT_EDGE;
            }

            if (toViewR.right >= Utils.getDeviceWidth(mContext)) {
                mCurrentEdge = RIGHT_EDGE;
            }

            //TODO mCurrentEdge is not accurate,fix it!

            ViewAnimator.putOn(fromView).translation(moveX, moveY);

            //if tracker view still in current screen
            if(!isViewOutOfListRect(toViewR,scrollViewR)) {
                float v1 = (toViewR.bottom - toViewR.top) * 1.0f / toView.getHeight();
                float v2 = (toViewR.right - toViewR.left) * 1.0f / toView.getWidth();
                if (mVisibleChangeListener != null) {
                    mVisibleChangeListener.onVisibleChange(v1 == 1 ? v2 : v1, this);
                }
            }
        } else {
                Logger.v(TAG, "moveCurrentView: move parent");
                //move parent
                ViewAnimator.putOn(parent).translation(locTo[0], locTo[1])
                    .andPutOn(fromView).translation(0, 0);
        }
    }

    private boolean isViewOutOfListRect(Rect viewR, Rect scrollViewR) {
        return viewR.top > scrollViewR.bottom || // bottom
                viewR.bottom < 0 ||// top
                viewR.left > scrollViewR.right - scrollViewR.left ||//right
                viewR.right < 0;//left
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

    protected void restoreActivityFlag() {
        Window window = mContext.getWindow();
        mOriginActivityFlag = window.getAttributes().flags;
        mOriginSystemUIVisibility = window.getDecorView().getSystemUiVisibility();
    }

    protected void keepScreenOn(boolean on) {
        if (on) {
            mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

}
