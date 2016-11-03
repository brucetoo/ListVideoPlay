package com.brucetoo.listvideoplay;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.brucetoo.listvideoplay.demo.ListViewFragment;
import com.brucetoo.listvideoplay.demo.ListViewMaskFragment;
import com.brucetoo.listvideoplay.demo.ListViewSmallScreenFragment;
import com.brucetoo.listvideoplay.demo.RecyclerViewFragment;
import com.brucetoo.listvideoplay.demo.RecyclerViewSmallScreenFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


    @Override
    public void onBackPressed() {
        if(getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            super.onBackPressed();
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
