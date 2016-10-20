package com.brucetoo.listvideoplay.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by Bruce Too
 * On 10/20/16.
 * At 19:47
 */

public class DisableListView extends ListView {

    private boolean mEnableScroll = true;

    public DisableListView(Context context) {
        super(context);
    }

    public DisableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DisableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DisableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!mEnableScroll)
            return true;
        return super.dispatchTouchEvent(ev);
    }

    public void setEnableScroll(boolean enableScroll) {
        this.mEnableScroll = enableScroll;
    }
}
