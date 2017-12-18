package com.brucetoo.listvideoplay;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.brucetoo.listvideoplay.demo.ConstraintFragment;
import com.brucetoo.listvideoplay.demo.DetailFragment;
import com.brucetoo.listvideoplay.demo.ListViewFragment;
import com.brucetoo.listvideoplay.demo.ListViewMaskFragment;
import com.brucetoo.listvideoplay.demo.ListViewSmallScreenFragment;
import com.brucetoo.listvideoplay.demo.PagerSupportFragment;
import com.brucetoo.listvideoplay.demo.RecyclerViewFragment;
import com.brucetoo.listvideoplay.demo.RecyclerViewSmallScreenFragment;
import com.brucetoo.listvideoplay.demo.SpringAnimationFragment;
import com.brucetoo.videoplayer.tracker.Tracker;

public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        final ClipDrawable drawable = (ClipDrawable) (findViewById(R.id.clip_image)).getBackground();
//        drawable.setLevel(0);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                drawable.setLevel(drawable.getLevel() + 50);
//                handler.postDelayed(this, 500);
//            }
//        }, 500);

        final GradientDrawable gd = new GradientDrawable();
        gd.setColor(getResources().getColor(R.color.colorAccent));
        final float max = getResources().getDimension(R.dimen.dimen_max);
        float min = getResources().getDimension(R.dimen.dimen_min);
        float dp1 = getResources().getDimension(R.dimen.dimen_1);
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(min);
        final ImageView view = (ImageView) findViewById(R.id.clip_image);
        view.setImageDrawable(gd);

        ValueAnimator radius = ValueAnimator.ofFloat(min, max);
        radius.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                gd.setCornerRadius(value);
            }
        });
        radius.setRepeatMode(ValueAnimator.REVERSE);
        radius.setRepeatCount(ValueAnimator.INFINITE);

        ValueAnimator width = ValueAnimator.ofFloat(100 * dp1, 60 * dp1);
        width.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.getLayoutParams().width = (int) value;
                view.requestLayout();
            }
        });
        width.setRepeatMode(ValueAnimator.REVERSE);
        width.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(2000);
        set.playTogether(width, radius);
        set.start();
    }


    public void onListViewClick(View view) {

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.layout_container, new ListViewFragment(), "ListViewFragment")
            .addToBackStack(null)
            .commit();
    }

    public void onRecyclerViewClick(View view) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.layout_container, new RecyclerViewFragment(), "RecyclerViewFragment")
            .addToBackStack(null)
            .commit();
    }

    public void onSmallScreenClick(View view) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.layout_container, new RecyclerViewSmallScreenFragment(), "RecyclerViewSmallScreenFragment")
            .addToBackStack(null)
            .commit();
    }

    public void onSmallScreenClick1(View view) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.layout_container, new ListViewSmallScreenFragment(), "ListViewSmallScreenFragment")
            .addToBackStack(null)
            .commit();
    }

    public void onHighLightClick(View view) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.layout_container, new ListViewMaskFragment(), "ListViewMaskFragment")
            .addToBackStack(null)
            .commit();
    }

    public void onSpringAnimation(View view) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.layout_container, new SpringAnimationFragment(), "SpringAnimationFragment")
            .addToBackStack(null)
            .commit();
    }

    public void onVideoSupport(View view) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.layout_container, new PagerSupportFragment(), "PagerSupportFragment")
            .addToBackStack("PagerSupportFragment")
            .commit();
    }


    public void onConstraintLayout(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_container, new ConstraintFragment(), "ConstraintFragment")
                .addToBackStack("ConstraintFragment")
                .commit();
    }

    public void addDetailFragment() {
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.layout_container, new DetailFragment(), "DetailFragment")
            .addToBackStack("DetailFragment")
            .commitAllowingStateLoss();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Tracker.onConfigurationChanged(this, newConfig);
    }

    @Override
    public void onBackPressed() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            // Get the current fragment using the method from the second step above...
            Fragment currentFragment = getCurrentFragment();

            // Determine whether or not this fragment implements Backable
            // Do a null check just to be safe
            if (currentFragment != null && currentFragment instanceof Backable) {

                if (((Backable) currentFragment).onBackPressed()) {
                    // If the onBackPressed override in your fragment
                    // did absorb the back event (returned true), return
                    return;
                } else {
                    // Otherwise, call the super method for the default behavior
                    super.onBackPressed();
                }
            }

            // Any other logic needed...
            // call super method to be sure the back button does its thing...
            super.onBackPressed();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public Fragment getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            String lastFragmentName = fragmentManager.getBackStackEntryAt(
                fragmentManager.getBackStackEntryCount() - 1).getName();
            return fragmentManager.findFragmentByTag(lastFragmentName);
        }
        return null;
    }
}
