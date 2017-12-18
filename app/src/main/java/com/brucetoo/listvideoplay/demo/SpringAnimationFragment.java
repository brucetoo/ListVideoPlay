package com.brucetoo.listvideoplay.demo;

import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.FloatValueHolder;
import android.support.animation.SpringAnimation;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.brucetoo.listvideoplay.R;

/**
 * Created by Bruce Too
 * On 16/03/2017.
 * At 14:47
 */

public class SpringAnimationFragment extends Fragment {

    private View mViewTop;
    private View mViewMiddle;
    private View mViewBottom;
    private View mRootView;
    private SeekBar mStiffness;
    private SeekBar mDamping;
    private VelocityTracker mVelocityTracker;
    private float mDownX, mDownY;
    private TextView mTextStiffness;
    private TextView mTextDamping;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spring_animation,container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewTop = view.findViewById(R.id.view_top);
        mViewMiddle = view.findViewById(R.id.view_middle);
        mViewBottom = view.findViewById(R.id.view_bottom);
        mRootView = view.findViewById(R.id.layout_root);
        mStiffness = (SeekBar) view.findViewById(R.id.sb_stiffness);
        mDamping = (SeekBar) view.findViewById(R.id.sb_damping);
        mTextStiffness = (TextView) view.findViewById(R.id.txt_stiffness);
        mTextDamping = (TextView) view.findViewById(R.id.txt_damping);

//        final SpringForce springForce = new SpringForce();
//        springForce.setDampingRatio(getDamping());
//        springForce.setStiffness(getStiffness());

        FlingAnimation flingAnimation = new FlingAnimation(new FloatValueHolder(1));
        flingAnimation.setStartVelocity(-2000);//设置动能开始时的速率
        flingAnimation.setStartValue(500);//开始位置，对应最终需要执行的属性(TranslationY)
        flingAnimation.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                Log.i("flingAnimation", "onAnimationUpdate: value -> " + value + " velocity -> " + velocity);
                mViewTop.setTranslationY(value);
            }
        });
        flingAnimation.start();

        final SpringAnimation anim1X = new SpringAnimation(mViewTop, SpringAnimation.TRANSLATION_X);
        final SpringAnimation anim1Y = new SpringAnimation(mViewTop, SpringAnimation.TRANSLATION_Y);
//        anim1X.setSpring(springForce);
//        anim1Y.setSpring(springForce);

        final SpringAnimation anim2X = new SpringAnimation(mViewMiddle, SpringAnimation.TRANSLATION_X);
        final SpringAnimation anim2Y = new SpringAnimation(mViewMiddle, SpringAnimation.TRANSLATION_Y);
//        anim2X.setSpring(springForce);
//        anim2Y.setSpring(springForce);

        final SpringAnimation anim3X = new SpringAnimation(mViewBottom, SpringAnimation.TRANSLATION_X);
        final SpringAnimation anim3Y = new SpringAnimation(mViewBottom, SpringAnimation.TRANSLATION_Y);
//        anim3X.setSpring(springForce);
//        anim3Y.setSpring(springForce);

        anim1X.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, final float value, float velocity) {
                runDelay(50, new Runnable() {
                    @Override
                    public void run() {
                        anim2X.animateToFinalPosition(value);
                    }
                });

                runDelay(100, new Runnable() {
                    @Override
                    public void run() {
                        anim3X.animateToFinalPosition(value);
                    }
                });
            }
        });

        anim1Y.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation,final float value, float velocity) {
                runDelay(50, new Runnable() {
                    @Override
                    public void run() {
                        anim2Y.animateToFinalPosition(value);
                    }
                });

                runDelay(100, new Runnable() {
                    @Override
                    public void run() {
                        anim3Y.animateToFinalPosition(value);
                    }
                });
            }
        });
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
                        float finalX = event.getX() - mDownX;
                        float finalY = event.getY() - mDownY;
//                        mViewBox.setTranslationX(event.getX() - mDownX);
//                        mViewBox.setTranslationY(event.getY() - mDownY);
//                        mVelocityTracker.addMovement(event);
                        anim1X.animateToFinalPosition(finalX);
                        anim1Y.animateToFinalPosition(finalY);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        anim1X.animateToFinalPosition(0);
                        anim1Y.animateToFinalPosition(0);
//                        mVelocityTracker.computeCurrentVelocity(1000);
//                        if (mViewBox.getTranslationX() != 0) {
//                            SpringAnimation animX = new SpringAnimation(mViewBox, SpringAnimation.TRANSLATION_X,0);
//                            animX.getSpring().setStiffness(getStiffness());
//                            animX.getSpring().setDampingRatio(getDamping());
//                            animX.setStartVelocity(mVelocityTracker.getXVelocity());
//                            animX.start();
//                        }
//                        if (mViewBox.getTranslationY() != 0) {
//                            SpringAnimation animY = new SpringAnimation(mViewBox, SpringAnimation.TRANSLATION_Y, 0);
//                            animY.getSpring().setStiffness(getStiffness());
//                            animY.getSpring().setDampingRatio(getDamping());
//                            animY.setStartVelocity(mVelocityTracker.getYVelocity());
//                            animY.start();
//                        }
                        mVelocityTracker.clear();
                        return true;
                }
                return false;
            }
        });
    }

    public void runDelay(long delay,Runnable runnable){
        mViewTop.postDelayed(runnable, delay);
    }

    private float getStiffness() {
        float stiffness = Math.max(mStiffness.getProgress(), 1f);
        mTextStiffness.setText("Stiffness " + stiffness);
        return stiffness;
    }

    private float getDamping() {
        float dampingRatio = mDamping.getProgress() / 100f;
        mTextDamping.setText("Damping Ratio " + dampingRatio);
        return dampingRatio;
    }
}
