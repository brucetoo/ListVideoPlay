package com.brucetoo.listvideoplay.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brucetoo.listvideoplay.Backable;
import com.brucetoo.listvideoplay.MainActivity;
import com.brucetoo.listvideoplay.R;
import com.brucetoo.videoplayer.Tracker;
import com.brucetoo.videoplayer.VideoLayerView;

/**
 * Created by Bruce Too
 * On 10/04/2017.
 * At 16:24
 */

public class DetailFragment extends Fragment implements Backable {

    public static final String TAG = "DetailFragment";
    private ImageView mImageCover;
    private TextView mTextDetail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextDetail = (TextView) view.findViewById(R.id.txt_detail);
        mImageCover = (ImageView) view.findViewById(R.id.img_cover);
        mImageCover.post(new Runnable() {
            @Override
            public void run() {
                startMoveInside();
            }
        });
    }

    private float transX;
    private float transY;
    private float deltaW;
    private float deltaH;
    private View cover;
    private View coverParent;

    private void startMoveInside() {
        cover = ((VideoLayerView) Tracker.getViewTracker(getActivity()).getVideoLayerView()).cover;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        int[] loc = new int[2];
        mImageCover.getLocationOnScreen(loc);
        coverParent = (View) cover.getParent();
        final float originX = coverParent.getTranslationX();
        final float originY = coverParent.getTranslationY();
        final int originW = cover.getWidth();
        final int originH = cover.getHeight();
        transX = originX - loc[0];
        transY = originY - loc[1];
        deltaW = originW - mImageCover.getMeasuredWidth();
        deltaH = originH - mImageCover.getMeasuredHeight();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                ViewAnimator.putOn(coverParent).translation(originX - transX * value, originY - transY * value);
                ViewGroup.LayoutParams params = cover.getLayoutParams();
                params.width = (int) (originW - deltaW * value);
                params.height = (int) (originH - deltaH * value);
                cover.requestLayout();
            }
        });
        animator.setDuration(500);
        animator.start();

        ViewAnimator.putOn(mTextDetail).animate().translationY(-mTextDetail.getHeight() / 4, 0)
        .alpha(0,1);
    }

    private void startMoveOutside(final Animator.AnimatorListener listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        final int[] loc = new int[2];
        mImageCover.getLocationOnScreen(loc);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                ViewAnimator.putOn(coverParent).translation(loc[0] + transX * value, loc[1] + transY * value);
                ViewGroup.LayoutParams params = cover.getLayoutParams();
                params.width = (int) (mImageCover.getWidth() + deltaW * value);
                params.height = (int) (mImageCover.getHeight() + deltaH * value);
                cover.requestLayout();
            }
        });
        animator.setDuration(500);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onAnimationEnd(animation);
            }
        });
        animator.start();


        ViewAnimator.putOn(mTextDetail).animate().translationY(-mTextDetail.getHeight() / 4)
        .alpha(1,0);
    }

    @Override
    public boolean onBackPressed() {
        startMoveOutside(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ((MainActivity) getActivity()).removeDetailFragment(DetailFragment.this);
            }
        });
        return true;
    }
}
