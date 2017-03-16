package com.brucetoo.listvideoplay.demo;

import android.os.Bundle;
import android.support.animation.SpringAnimation;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.brucetoo.listvideoplay.R;

/**
 * Created by Bruce Too
 * On 16/03/2017.
 * At 14:47
 */

public class SpringAnimationFragment extends Fragment {

    private View mViewBox;
    private View mRootView;
    private SeekBar mStiffness;
    private SeekBar mDampling;
    private VelocityTracker mVelocityTracker;
    private float mDownX, mDownY;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spring_animation,container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewBox = view.findViewById(R.id.view_box);
        mRootView = view.findViewById(R.id.layout_root);
        mStiffness = (SeekBar) view.findViewById(R.id.sb_stiffness);
        mDampling = (SeekBar) view.findViewById(R.id.sb_damping);

        mVelocityTracker = VelocityTracker.obtain();

        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        mDownY = event.getY();
                        mVelocityTracker.addMovement(event);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mViewBox.setTranslationX(event.getX() - mDownX);
                        mViewBox.setTranslationY(event.getY() - mDownY);
                        mVelocityTracker.addMovement(event);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mVelocityTracker.computeCurrentVelocity(1000);
                        if (mViewBox.getTranslationX() != 0) {
                            SpringAnimation animX = new SpringAnimation(mViewBox, SpringAnimation.TRANSLATION_X,0);
                            animX.getSpring().setStiffness(getStiffness());
                            animX.getSpring().setDampingRatio(getDamping());
                            animX.setStartVelocity(mVelocityTracker.getXVelocity());
                            animX.start();
                        }
                        if (mViewBox.getTranslationY() != 0) {
                            SpringAnimation animY = new SpringAnimation(mViewBox, SpringAnimation.TRANSLATION_Y, 0);
                            animY.getSpring().setStiffness(getStiffness());
                            animY.getSpring().setDampingRatio(getDamping());
                            animY.setStartVelocity(mVelocityTracker.getYVelocity());
                            animY.start();
                        }
                        mVelocityTracker.clear();
                        return true;
                }
                return false;
            }
        });
    }

    private float getStiffness() {
        return Math.max(mStiffness.getProgress(), 1f);
    }

    private float getDamping() {
        return mDampling.getProgress() / 100f;
    }
}
